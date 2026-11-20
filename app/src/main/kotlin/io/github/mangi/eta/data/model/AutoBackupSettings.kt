package io.github.mangi.eta.data.model

import kotlinx.serialization.Serializable

/**
 * 自动备份设置。
 *
 * [lastAt] / [lastFileName] / [lastError] 是运行状态，不是用户选项：
 * 界面靠它们显示「上次什么时候备份成」「为什么没备份成」。
 */
@Serializable
data class AutoBackupSettings(
    val enabled: Boolean = false,
    /** 间隔小时数；[INTERVAL_OPTIONS] 是界面上的预设档位。 */
    val intervalHours: Int = 24,
    /** 保留最近多少份，超出的按文件名（时间戳）倒序删除。 */
    val keepCount: Int = 7,
    val lastAt: Long = 0L,
    val lastFileName: String = "",
    val lastError: String = "",
    val lastErrorAt: Long = 0L,
    /** 上次备份时的内容指纹，用来跳过「数据没变化」的那一次。 */
    val fingerprint: String = "",
    /** 用户自选的备份文件夹（SAF 目录树 URI）；空表示用默认目录。 */
    val directoryUri: String = "",
) {
    fun normalized(): AutoBackupSettings = copy(
        intervalHours = intervalHours.coerceIn(MIN_INTERVAL_HOURS, MAX_INTERVAL_HOURS),
        keepCount = keepCount.coerceIn(MIN_KEEP_COUNT, MAX_KEEP_COUNT),
    )

    companion object {
        val INTERVAL_OPTIONS = listOf(1, 6, 12, 24, 72, 168)
        val KEEP_OPTIONS = listOf(3, 7, 14, 30)
        const val MIN_INTERVAL_HOURS = 1
        const val MAX_INTERVAL_HOURS = 720
        const val MIN_KEEP_COUNT = 1
        const val MAX_KEEP_COUNT = 60

        /** 间隔不超过这个值时不看内容指纹，每次都写一份。 */
        const val ALWAYS_WRITE_MAX_HOURS = 1

        fun intervalMillis(hours: Int): Long =
            hours.coerceIn(MIN_INTERVAL_HOURS, MAX_INTERVAL_HOURS) * 3_600_000L
    }
}
