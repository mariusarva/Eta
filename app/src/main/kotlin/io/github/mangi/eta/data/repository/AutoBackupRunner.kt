package io.github.mangi.eta.data.repository

import android.content.Context
import android.os.Environment
import io.github.mangi.eta.core.AndroidAgentLogger
import io.github.mangi.eta.core.safeLogType
import io.github.mangi.eta.data.datastore.SettingsDataStore
import io.github.mangi.eta.data.db.EtaDatabase
import io.github.mangi.eta.data.model.AutoBackupSettings
import java.io.File
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

/**
 * 定期自动备份：把 Eta 数据导出成 `.eta-backup.json`，放共享目录的 `Eta备份/` 下，按保留份数删最旧的。
 *
 * 复用「数据备份」页同一个导出实现（[EtaBackupRepository.export]），产物与手动导出完全一致，
 * 需要还原时走「数据备份 → 导入」即可。
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
    private const val TEMP_SUFFIX = ".part"

    /** 同一时刻只允许一次备份在跑。 */
    private val mutex = Mutex()

    /**
     * 备份目录：有「所有文件访问权限」时用共享存储根目录下的 [DIR_NAME]，
     * 否则回落到应用外部私有目录，路径会显示在界面上，用户仍可手动拷出。
     */
    fun directory(context: Context): File {
        val shared = runCatching { Environment.isExternalStorageManager() }.getOrDefault(false)
        return if (shared) {
            File(Environment.getExternalStorageDirectory(), DIR_NAME)
        } else {
            File(context.getExternalFilesDir(null) ?: context.filesDir, DIR_NAME)
        }
    }

    fun isUsingSharedStorage(context: Context): Boolean =
        runCatching { Environment.isExternalStorageManager() }.getOrDefault(false)

    /** 已存在的自动备份文件，最新在前。 */
    fun backups(context: Context): List<File> = directory(context)
        .listFiles { file: File ->
            file.isFile && file.name.startsWith(FILE_PREFIX) && file.name.endsWith(FILE_SUFFIX)
        }
        ?.sortedByDescending { it.name }
        .orEmpty()

    fun latestBackup(context: Context): File? = backups(context).firstOrNull()

    /** 目录里残留的 `.part`（上次导出被杀留下的），下次跑之前顺手清掉。 */
    private fun staleTempFiles(context: Context): List<File> = directory(context)
        .listFiles { file: File -> file.isFile && file.name.endsWith(TEMP_SUFFIX) }
        .orEmpty()
        .toList()

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
        val app = context.applicationContext
        if (!force && !settings.enabled) return AutoBackupResult.Skipped("未开启自动备份")

        val now = System.currentTimeMillis()
        val intervalMs = AutoBackupSettings.intervalMillis(settings.intervalHours)
        if (!force && settings.lastAt > 0L && now - settings.lastAt < intervalMs) {
            return AutoBackupResult.Skipped("未到备份周期")
        }

        val fingerprint = fingerprint(app)
        val alwaysWrite = settings.intervalHours <= AutoBackupSettings.ALWAYS_WRITE_MAX_HOURS
        if (!force && !alwaysWrite && fingerprint.isNotEmpty() && fingerprint == settings.fingerprint) {
            runCatching {
                SettingsDataStore.setAutoBackupSettings(
                    settings.copy(lastAt = now, fingerprint = fingerprint)
                )
            }
            return AutoBackupResult.Skipped("数据没有变化")
        }
        return runNow(app, settings, fingerprint, now)
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
        val dir = directory(context)
        if (!dir.exists() && !dir.mkdirs()) return fail(settings, now, "无法创建备份目录 " + dir.absolutePath)
        if (!dir.isDirectory || !dir.canWrite()) return fail(settings, now, "备份目录不可写 " + dir.absolutePath)
        staleTempFiles(context).forEach { it.deleteQuietly() }

        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date(now))
        val target = File(dir, "$FILE_PREFIX$stamp$FILE_SUFFIX")
        val temp = File(dir, target.name + TEMP_SUFFIX)
        return try {
            val conversationCount = temp.outputStream().use { stream ->
                EtaBackupRepository.export(context, stream).conversationCount
            }
            if (!commit(temp, target)) {
                temp.deleteQuietly()
                return fail(settings, now, "备份文件落盘失败 " + target.absolutePath)
            }
            val kept = prune(context, settings.keepCount)
            runCatching {
                SettingsDataStore.setAutoBackupSettings(
                    settings.copy(
                        lastAt = System.currentTimeMillis(),
                        lastFileName = target.name,
                        fingerprint = fingerprint,
                        lastError = "",
                        lastErrorAt = 0L,
                    )
                )
            }
            AutoBackupResult.Done(target.name, target.length(), conversationCount, kept)
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            temp.deleteQuietly()
            AndroidAgentLogger.warn("Auto backup failed: type=${error.safeLogType()}")
            fail(settings, now, error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName)
        }
    }

    /**
     * 把临时文件改名成正式备份。
     *
     * 同目录 rename 在共享存储上是原子的；个别 ROM 的 FUSE 实现会拒绝跨 UID 改名
     * （应用重装后 UID 变了，旧文件属主还是上一个 UID），这时退化成复制一份。
     */
    private fun commit(temp: File, target: File): Boolean {
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

    private suspend fun fail(settings: AutoBackupSettings, now: Long, message: String): AutoBackupResult {
        // 失败也推进检查时间，否则每次打开应用都立刻重试同一个失败；下一个周期再试。
        runCatching {
            SettingsDataStore.setAutoBackupSettings(
                settings.copy(lastAt = now, lastError = message, lastErrorAt = now)
            )
        }
        return AutoBackupResult.Failed(message)
    }

    private fun prune(context: Context, keep: Int): Int {
        val pool = backups(context)
        pool.drop(keep.coerceAtLeast(1)).forEach { stale ->
            if (!stale.delete()) {
                AndroidAgentLogger.warn("Auto backup prune skipped: ~" + stale.length() + "B")
            }
        }
        return backups(context).size
    }

    private fun File.deleteQuietly() {
        runCatching { if (exists()) delete() }
    }
}
