package io.github.mangi.eta.data.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import io.github.mangi.eta.core.AndroidAgentLogger
import io.github.mangi.eta.core.safeLogType
import io.github.mangi.eta.data.datastore.SettingsDataStore
import io.github.mangi.eta.data.db.EtaDatabase
import io.github.mangi.eta.data.model.AutoBackupSettings
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** 自动备份的结果，供界面提示与日志使用。 */
internal sealed interface AutoBackupResult {
    data class Done(
        val fileName: String,
        val sizeBytes: Long,
        val conversationCount: Int,
        val kept: Int,
    ) : AutoBackupResult

    data class Skipped(val reason: String) : AutoBackupResult
    data class Failed(val message: String) : AutoBackupResult
}

/** 一份已存在的备份文件，[delete] 负责按当前目标（普通目录 / SAF 目录树）删掉它。 */
internal class AutoBackupFile(
    val name: String,
    val length: Long,
    private val deleteAction: () -> Boolean,
) {
    fun delete(): Boolean = runCatching { deleteAction() }.getOrDefault(false)
}

/**
 * 定期自动备份：把 Eta 数据导出成 `.eta-backup.json`，按保留份数删最旧的。
 *
 * 目标目录有两种：
 * 1. 用户用系统文件夹选择器选过的 SAF 目录树（[AutoBackupSettings.directoryUri]），可写任意文件夹；
 * 2. 没选时用默认目录——有「所有文件访问权限」写共享存储的 `Eta备份/`，否则回落到应用私有目录。
 *
 * 复用「数据备份」页同一个导出实现（[EtaBackupRepository.export]），产物与手动导出完全一致。
 *
 * 可靠性约定：
 * 1. 全流程串行（[mutex]）：闹钟、应用启动、界面「立即备份」不会两份同时写；
 * 2. 先写 `.part` 再改名：中途被杀只留临时文件，不会出现半截备份占保留名额；
 * 3. 失败落状态：失败原因与时间写进设置，界面能显示为什么没备份成，成功时清空。
 */
internal object AutoBackupRunner {

    const val DIR_NAME = "Eta备份"
    private const val FILE_PREFIX = "Eta-自动备份-"
    private const val FILE_SUFFIX = ".eta-backup.json"
    private const val FULL_PREFIX = "Eta-全量备份-"
    private const val FULL_SUFFIX = EtaFullBackupArchive.FULL_SUFFIX
    private const val TEMP_SUFFIX = ".part"
    private const val MIME_JSON = "application/json"
    private const val MIME_ZIP = "application/zip"

    /** 同一时刻只允许一次备份在跑。 */
    private val mutex = Mutex()

    private fun isBackupName(name: String?): Boolean =
        name != null && ((name.startsWith(FILE_PREFIX) && name.endsWith(FILE_SUFFIX)) ||
            (name.startsWith(FULL_PREFIX) && name.endsWith(FULL_SUFFIX)))

    /** 默认目录：有「所有文件访问权限」用共享存储，否则用应用外部私有目录。 */
    fun defaultDirectory(context: Context): File {
        val shared = runCatching { Environment.isExternalStorageManager() }.getOrDefault(false)
        return if (shared) {
            File(Environment.getExternalStorageDirectory(), DIR_NAME)
        } else {
            File(context.getExternalFilesDir(null) ?: context.filesDir, DIR_NAME)
        }
    }

    fun isUsingSharedStorage(context: Context): Boolean =
        runCatching { Environment.isExternalStorageManager() }.getOrDefault(false)

    /** 用户自选目录树；没选或权限已失效时返回 null。 */
    fun treeDirectory(context: Context, settings: AutoBackupSettings): DocumentFile? {
        val raw = settings.directoryUri.trim()
        if (raw.isEmpty()) return null
        return runCatching {
            val uri = Uri.parse(raw)
            val dir = DocumentFile.fromTreeUri(context, uri) ?: return null
            if (dir.isDirectory && dir.canWrite()) dir else null
        }.getOrNull()
    }

    /** 界面上显示的当前目标位置。 */
    fun locationLabel(context: Context, settings: AutoBackupSettings): String {
        val tree = treeDirectory(context, settings)
        if (tree != null) {
            val name = tree.name?.takeIf { it.isNotBlank() } ?: "所选文件夹"
            return "$name（自定义文件夹）"
        }
        if (settings.directoryUri.isNotBlank()) {
            return defaultDirectory(context).absolutePath + "（所选文件夹已失效，回落到默认目录）"
        }
        return if (isUsingSharedStorage(context)) {
            defaultDirectory(context).absolutePath
        } else {
            defaultDirectory(context).absolutePath + "（未授予所有文件访问权限，暂存应用私有目录）"
        }
    }

    /** 已存在的自动备份，最新在前。 */
    fun backups(context: Context, settings: AutoBackupSettings): List<AutoBackupFile> {
        val tree = treeDirectory(context, settings)
        if (tree != null) {
            return tree.listFiles()
                .filter { it.isFile && isBackupName(it.name) }
                .map { doc ->
                    AutoBackupFile(
                        name = doc.name.orEmpty(),
                        length = doc.length(),
                        deleteAction = { doc.delete() },
                    )
                }
                .sortedByDescending { it.name }
        }
        return defaultDirectory(context)
            .listFiles { file: File -> file.isFile && isBackupName(file.name) }
            ?.map { file ->
                AutoBackupFile(file.name, file.length(), deleteAction = { file.delete() })
            }
            ?.sortedByDescending { it.name }
            .orEmpty()
    }

    fun latestBackup(context: Context, settings: AutoBackupSettings): AutoBackupFile? =
        backups(context, settings).firstOrNull()

    /**
     * 到点且内容有变化才执行；[force] 为 true 时忽略开关、周期与指纹（「立即备份一次」）。
     * 任何失败都只记录状态与日志，不抛出，避免影响启动或闹钟回调。
     */
    suspend fun maybeRun(context: Context, force: Boolean = false): AutoBackupResult =
        withContext(Dispatchers.IO) {
            mutex.withLock { runLocked(context.applicationContext, force) }
        }

    private suspend fun runLocked(context: Context, force: Boolean): AutoBackupResult {
        val settings = runCatching { SettingsDataStore.autoBackupSettings() }.getOrNull()
            ?: return AutoBackupResult.Skipped("设置不可读")
        if (!force && !settings.enabled) return AutoBackupResult.Skipped("未开启自动备份")

        val now = System.currentTimeMillis()
        val intervalMs = AutoBackupSettings.intervalMillis(settings.intervalHours)
        if (!force && settings.lastAt > 0L && now - settings.lastAt < intervalMs) {
            return AutoBackupResult.Skipped("未到备份周期")
        }

        val fingerprint = fingerprint(context)
        val alwaysWrite = settings.intervalHours <= AutoBackupSettings.ALWAYS_WRITE_MAX_HOURS
        if (!force && !alwaysWrite && fingerprint.isNotEmpty() && fingerprint == settings.fingerprint) {
            runCatching {
                SettingsDataStore.setAutoBackupSettings(
                    settings.copy(lastAt = now, fingerprint = fingerprint)
                )
            }
            return AutoBackupResult.Skipped("数据没有变化")
        }
        return runNow(context, settings, fingerprint, now)
    }

    /** 轻量指纹：只读两个计数，不碰数据库文件本身。取不到时返回空串（空串不做跳过判断）。 */
    private suspend fun fingerprint(context: Context): String = runCatching {
        val database = EtaDatabase.get(context)
        val conversations = database.conversationDao().conversationTotal()
        val messages = database.conversationDao().messageTotal()
        "c=$conversations;m=$messages"
    }.getOrDefault("")

    private suspend fun runNow(
        context: Context,
        settings: AutoBackupSettings,
        fingerprint: String,
        now: Long,
    ): AutoBackupResult {
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date(now))
        val full = settings.format == AutoBackupSettings.FORMAT_FULL
        val fileName = if (full) "$FULL_PREFIX$stamp$FULL_SUFFIX" else "$FILE_PREFIX$stamp$FILE_SUFFIX"
        val tree = treeDirectory(context, settings)
        return if (tree != null) {
            runIntoTree(context, settings, tree, fileName, fingerprint, now, full)
        } else {
            runIntoDirectory(context, settings, fileName, fingerprint, now, full)
        }
    }

    private suspend fun runIntoDirectory(
        context: Context,
        settings: AutoBackupSettings,
        fileName: String,
        fingerprint: String,
        now: Long,
        full: Boolean,
    ): AutoBackupResult {
        val dir = defaultDirectory(context)
        if (!dir.exists() && !dir.mkdirs()) return fail(settings, now, "无法创建备份目录 " + dir.absolutePath)
        if (!dir.isDirectory || !dir.canWrite()) return fail(settings, now, "备份目录不可写 " + dir.absolutePath)
        dir.listFiles { file: File -> file.isFile && file.name.endsWith(TEMP_SUFFIX) }
            ?.forEach { it.deleteQuietly() }

        val target = File(dir, fileName)
        val temp = File(dir, fileName + TEMP_SUFFIX)
        return try {
            val conversationCount = temp.outputStream().use { stream ->
                if (full) {
                    EtaFullBackupArchive.export(context, stream).conversationCount
                } else {
                    EtaBackupRepository.export(context, stream).conversationCount
                }
            }
            if (!commitFile(temp, target)) {
                temp.deleteQuietly()
                return fail(settings, now, "备份文件落盘失败 " + target.absolutePath)
            }
            val kept = prune(context, settings)
            succeed(settings, now, fingerprint, fileName, target.length(), conversationCount, kept)
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            temp.deleteQuietly()
            failLogged(settings, now, error)
        }
    }

    private suspend fun runIntoTree(
        context: Context,
        settings: AutoBackupSettings,
        tree: DocumentFile,
        fileName: String,
        fingerprint: String,
        now: Long,
        full: Boolean,
    ): AutoBackupResult {
        tree.listFiles()
            .filter { it.isFile && it.name?.contains(TEMP_SUFFIX) == true }
            .forEach { runCatching { it.delete() } }

        // MIME 必须和实际内容一致：SAF 会按 MIME 自动补扩展名，
        // 全量包用 application/json 会被存成 `xxx.zip.part.json`，后面 renameTo 按名字找不到源文件。
        val mime = if (full) MIME_ZIP else MIME_JSON
        val temp = runCatching { tree.createFile(mime, fileName + TEMP_SUFFIX) }.getOrNull()
            ?: return fail(settings, now, "无法在所选文件夹创建备份文件")
        return try {
            val conversationCount = context.contentResolver.openOutputStream(temp.uri)?.use { stream ->
                if (full) {
                    EtaFullBackupArchive.export(context, stream).conversationCount
                } else {
                    EtaBackupRepository.export(context, stream).conversationCount
                }
            } ?: throw IOException("无法写入所选文件夹")
            // SAF 可能给临时文件补过扩展名，用实际名字做源，避免 renameTo 找不到。
            val actualName = temp.name ?: (fileName + TEMP_SUFFIX)
            val renamed = runCatching { temp.renameTo(fileName) }.getOrDefault(false)
            if (!renamed) {
                AndroidAgentLogger.warn("Auto backup rename failed: actual=$actualName target=$fileName")
                runCatching { temp.delete() }
                return fail(settings, now, "备份文件改名失败")
            }
            val kept = prune(context, settings)
            succeed(settings, now, fingerprint, fileName, temp.length(), conversationCount, kept)
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            runCatching { temp.delete() }
            failLogged(settings, now, error)
        }
    }

    private suspend fun succeed(
        settings: AutoBackupSettings,
        now: Long,
        fingerprint: String,
        fileName: String,
        sizeBytes: Long,
        conversationCount: Int,
        kept: Int,
    ): AutoBackupResult {
        runCatching {
            SettingsDataStore.setAutoBackupSettings(
                settings.copy(
                    lastAt = System.currentTimeMillis(),
                    lastFileName = fileName,
                    fingerprint = fingerprint,
                    lastError = "",
                    lastErrorAt = 0L,
                )
            )
        }
        return AutoBackupResult.Done(fileName, sizeBytes, conversationCount, kept)
    }

    /**
     * 把临时文件改名成正式备份。
     *
     * 同目录 rename 在共享存储上是原子的；个别 ROM 的 FUSE 实现会拒绝跨 UID 改名
     * （应用重装后 UID 变了，旧文件属主还是上一个 UID），这时退化成复制一份。
     */
    private fun commitFile(temp: File, target: File): Boolean {
        if (target.exists()) target.deleteQuietly()
        if (temp.renameTo(target)) return true
        return runCatching {
            temp.inputStream().use { input ->
                target.outputStream().use { output -> input.copyTo(output, DEFAULT_BUFFER_SIZE * 8) }
            }
            temp.deleteQuietly()
            target.length() > 0L
        }.getOrDefault(false)
    }

    private suspend fun failLogged(settings: AutoBackupSettings, now: Long, error: Throwable): AutoBackupResult {
        AndroidAgentLogger.warn("Auto backup failed: type=${error.safeLogType()}")
        return fail(
            settings,
            now,
            error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName,
        )
    }

    private suspend fun fail(settings: AutoBackupSettings, now: Long, message: String): AutoBackupResult {
        // 失败也推进检查时间，否则每次打开应用都立刻重试同一个失败；下一个周期再试。
        runCatching {
            SettingsDataStore.setAutoBackupSettings(
                settings.copy(lastAt = now, lastError = message, lastErrorAt = now)
            )
        }
        return AutoBackupResult.Failed(message)
    }

    private fun prune(context: Context, settings: AutoBackupSettings): Int {
        val pool = backups(context, settings)
        pool.drop(settings.keepCount.coerceAtLeast(1)).forEach { stale ->
            if (!stale.delete()) {
                AndroidAgentLogger.warn("Auto backup prune skipped: ~" + stale.length + "B")
            }
        }
        return backups(context, settings).size
    }

    private fun File.deleteQuietly() {
        runCatching { if (exists()) delete() }
    }
}
