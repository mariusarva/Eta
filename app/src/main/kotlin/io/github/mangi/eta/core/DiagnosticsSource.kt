package io.github.mangi.eta.core

import android.content.Context
import io.github.mangi.eta.data.datastore.SettingsDataStore
import io.github.mangi.eta.data.db.EtaDatabase
import java.io.File

/**
 * 自检的数据来源适配层。
 *
 * 上游 `Oo0520/Eta-por` 的 `DiagnosticsPatrol` 直接读 `Settings` 数据模型里的
 * `autoBackup*` / `diagPatrol*` 字段，但本仓库的 `Settings` 只有 provider/model/
 * memory/appearance 四项，那些字段分散在 `AutoBackupSettings` 与 `Prefs` 里。
 *
 * 这里做一层收敛：自检代码只依赖本对象，不直接碰具体存储。
 */
internal object DiagnosticsSource {

    /** 自动备份的当前状态。 */
    data class BackupState(
        val enabled: Boolean,
        val intervalMinutes: Int,
        val lastAt: Long,
    )

    suspend fun backupState(context: Context): BackupState {
        val settings = runCatching { SettingsDataStore.autoBackupSettings() }.getOrNull()
        return BackupState(
            enabled = settings?.enabled == true,
            intervalMinutes = (settings?.intervalHours ?: 24) * 60,
            lastAt = settings?.lastAt ?: 0L,
        )
    }

    /** 保留份数。 */
    fun patrolKeepCount(context: Context): Int =
        (readPrefs(context)?.getInt(KEY_KEEP_COUNT, 30) ?: 30).coerceIn(1, 60)

    fun setPatrolKeepCount(context: Context, count: Int) {
        readPrefs(context)?.edit()?.putInt(KEY_KEEP_COUNT, count.coerceIn(1, 60))?.apply()
    }

    /** 只在异常时通知。 */
    fun patrolNotifyOnlyAbnormal(context: Context): Boolean =
        readPrefs(context)?.getBoolean(KEY_ONLY_ABNORMAL, true) ?: true

    fun setPatrolNotifyOnlyAbnormal(context: Context, onlyAbnormal: Boolean) {
        readPrefs(context)?.edit()?.putBoolean(KEY_ONLY_ABNORMAL, onlyAbnormal)?.apply()
    }

    /** 上次巡检结论摘要（一行）。 */
    fun lastPatrolSummary(context: Context): String =
        readPrefs(context)?.getString(KEY_LAST_SUMMARY, "").orEmpty()

    fun setLastPatrolSummary(context: Context, summary: String) {
        readPrefs(context)?.edit()?.putString(KEY_LAST_SUMMARY, summary)?.apply()
    }

    /** 上次巡检时间戳（毫秒）；0 表示从未跑过。 */
    fun lastPatrolAt(context: Context): Long =
        readPrefs(context)?.getLong(KEY_LAST_PATROL_AT, 0L) ?: 0L

    fun setLastPatrolAt(context: Context, at: Long) {
        readPrefs(context)?.edit()?.putLong(KEY_LAST_PATROL_AT, at)?.apply()
    }

    /** 定时巡检开关。 */
    fun patrolEnabled(context: Context): Boolean =
        readPrefs(context)?.getBoolean(KEY_PATROL_ENABLED, false) ?: false

    fun setPatrolEnabled(context: Context, enabled: Boolean) {
        readPrefs(context)?.edit()?.putBoolean(KEY_PATROL_ENABLED, enabled)?.apply()
    }

    /** 巡检周期（分钟）。 */
    fun patrolIntervalMinutes(context: Context): Int {
        val raw = readPrefs(context)?.getInt(KEY_PATROL_INTERVAL, DEFAULT_INTERVAL) ?: DEFAULT_INTERVAL
        return raw.coerceIn(5, 30 * 24 * 60)
    }

    fun setPatrolIntervalMinutes(context: Context, minutes: Int) {
        readPrefs(context)?.edit()
            ?.putInt(KEY_PATROL_INTERVAL, minutes.coerceIn(5, 30 * 24 * 60))
            ?.apply()
    }

    /** 上次巡检的基线（用于增量判定）。 */
    fun readBaseline(context: Context): PatrolBaseline? {
        val prefs = readPrefs(context) ?: return null
        val at = prefs.getLong(KEY_BASELINE_AT, 0L)
        if (at <= 0L) return null
        return PatrolBaseline(
            at = at,
            crashCount = prefs.getInt(KEY_BASELINE_CRASH_COUNT, 0),
            crashLatestAt = prefs.getLong(KEY_BASELINE_CRASH_LATEST, 0L),
            dbBytes = prefs.getLong(KEY_BASELINE_DB_BYTES, 0L),
        )
    }

    fun writeBaseline(context: Context, baseline: PatrolBaseline) {
        readPrefs(context)?.edit()
            ?.putLong(KEY_BASELINE_AT, baseline.at)
            ?.putInt(KEY_BASELINE_CRASH_COUNT, baseline.crashCount)
            ?.putLong(KEY_BASELINE_CRASH_LATEST, baseline.crashLatestAt)
            ?.putLong(KEY_BASELINE_DB_BYTES, baseline.dbBytes)
            ?.apply()
    }

    /**
     * 数据库文件总体积。
     *
     * 上游用 `TextStoreMaintenance.inspect` 逐字段统计字符数，那需要
     * `ConversationDao` 的 `chunkUsage` / `trimToTail` 等 8 个本仓库没有的方法。
     * 这里退一步只看文件体积——够判断"库在膨胀"，也不需要动 DAO。
     */
    fun databaseBytes(context: Context): Long {
        val dir = context.getDatabasePath("eta.db").parentFile ?: return 0L
        if (!dir.isDirectory) return 0L
        return dir.listFiles()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L
    }

    fun databasePath(context: Context): String =
        context.getDatabasePath("eta.db").parentFile?.absolutePath ?: "—"

    /** 巡检报告与诊断包的落盘目录。 */
    fun reportDir(): File = File("/storage/emulated/0/ai共享文件夹/诊断包备份/巡检")

    private fun readPrefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    private const val FILE = "eta_diagnostics_patrol"
    private const val KEY_PATROL_ENABLED = "patrol_enabled"
    private const val KEY_KEEP_COUNT = "keep_count"
    private const val KEY_ONLY_ABNORMAL = "notify_only_abnormal"
    private const val KEY_LAST_SUMMARY = "last_summary"
    private const val KEY_LAST_PATROL_AT = "last_patrol_at"
    private const val KEY_PATROL_INTERVAL = "patrol_interval_minutes"
    private const val KEY_BASELINE_AT = "baseline_at"
    private const val KEY_BASELINE_CRASH_COUNT = "baseline_crash_count"
    private const val KEY_BASELINE_CRASH_LATEST = "baseline_crash_latest"
    private const val KEY_BASELINE_DB_BYTES = "baseline_db_bytes"
    private const val DEFAULT_INTERVAL = 24 * 60
}

/** 上一次巡检留下的基线，用于算增量。 */
internal data class PatrolBaseline(
    val at: Long,
    val crashCount: Int,
    val crashLatestAt: Long,
    val dbBytes: Long,
)
