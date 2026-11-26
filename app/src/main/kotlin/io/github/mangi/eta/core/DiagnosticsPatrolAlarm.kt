package io.github.mangi.eta.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 自动排查的定时器：按周期排**一次**闹钟，到点巡检一次，跑完再排下一次。
 *
 * 与自动备份同一套做法：优先精确闹钟，系统没给权限时退回
 * [AlarmManager.setAndAllowWhileIdle]，不报错、不弹窗；没有任何常驻进程或前台服务。
 *
 * 移植自 `Oo0520/Eta-por`，开关与周期改由 [DiagnosticsSource] 读取
 * （原版读 `Settings.diagPatrolEnabled`，本仓库的 `Settings` 没有这个字段）。
 */
internal object DiagnosticsPatrolAlarm {

    private const val REQUEST_CODE = 0x0E7C

    /** 最短延迟，避免到期时间已过导致立刻连环触发。 */
    private const val MIN_DELAY_MS = 60_000L

    suspend fun refresh(context: Context) = withContext(Dispatchers.IO) {
        val app = context.applicationContext
        if (!DiagnosticsSource.patrolEnabled(app)) {
            cancel(app)
            return@withContext
        }
        val intervalMs = DiagnosticsPatrolInterval.millis(DiagnosticsSource.patrolIntervalMinutes(app))
        val now = System.currentTimeMillis()
        val lastAt = DiagnosticsSource.lastPatrolAt(app)
        val due = if (lastAt > 0L) lastAt + intervalMs else now
        val triggerAt = maxOf(due, now + MIN_DELAY_MS)
        runCatching {
            val manager = app.getSystemService(AlarmManager::class.java)
            val exact = runCatching { manager.canScheduleExactAlarms() }.getOrDefault(false)
            if (exact) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(app))
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(app))
            }
        }.onFailure {
            android.util.Log.w("EtaPatrol", "alarm unavailable: ${it::class.java.simpleName}")
        }
    }

    fun cancel(context: Context) {
        val app = context.applicationContext
        runCatching { app.getSystemService(AlarmManager::class.java).cancel(pendingIntent(app)) }
    }

    /**
     * 闹钟是否真的排着。
     *
     * API 34 起没有直接查询接口，这里退回「排完之后自己的巡检是启用的」这一保守判据，
     * 避免误报成已重排。
     */
    fun isScheduled(context: Context): Boolean = runCatching {
        DiagnosticsSource.patrolEnabled(context.applicationContext)
    }.getOrDefault(false)

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, DiagnosticsPatrolReceiver::class.java).setAction(DiagnosticsPatrolReceiver.ACTION_PATROL),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

/** 巡检闹钟到点后触发一次巡检，并排下一次。 */
internal class DiagnosticsPatrolReceiver : android.content.BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_PATROL) return
        val pending = goAsync()
        Thread {
            try {
                kotlinx.coroutines.runBlocking {
                    runCatching {
                        DiagnosticsPatrol.runOnce(context, reason = "定时巡检", exportBundle = true)
                    }
                }
            } finally {
                runCatching { pending.finish() }
            }
        }.apply { isDaemon = true }.start()
    }

    companion object {
        const val ACTION_PATROL = "io.github.mangi.eta.action.DIAGNOSTICS_PATROL"
    }
}
