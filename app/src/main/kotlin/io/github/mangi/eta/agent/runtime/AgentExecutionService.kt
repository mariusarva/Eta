package io.github.mangi.eta.agent.runtime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import io.github.mangi.eta.R
import io.github.mangi.eta.agent.overlay.AgentOverlayStatus
import io.github.mangi.eta.agent.overlay.localizedText
import io.github.mangi.eta.core.AndroidAgentLogger
import io.github.mangi.eta.core.safeLogType
import io.github.mangi.eta.ui.MainActivity
import java.util.concurrent.atomic.AtomicLong

/** 只在用户任务存活期间持有前台执行生命周期；进程被系统停止后不重放任务。 */
internal class AgentExecutionService : Service() {
    private val stopQueue = ExecutionStopQueue { failure ->
        AndroidAgentLogger.warn("Execution task stop failed: type=${failure.safeLogType()}")
    }
    /**
     * 暂停/继续的投递线程。
     *
     * 必须离开主线程：onStartCommand 跑在主线程，而 AgentRuntimeClient 底层的
     * AgentRuntimeConnection.acquire 有主线程断言，直接调用会抛 IllegalStateException
     * 并被 withRuntimeMessenger 静默吞掉 —— 暂停指令根本没送到运行进程。
     */
    private val pauseDispatch = java.util.concurrent.Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "eta-execution-pause").apply { isDaemon = true }
    }
    private val owner = ownerSequence.incrementAndGet()
    private var foregroundActive = false
    @Volatile private var startRejected = false

    override fun onCreate() {
        super.onCreate()
        instance = this
        leases.attachOwner(owner)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, getString(R.string.execution_channel), NotificationManager.IMPORTANCE_LOW),
        )
        ensureForeground()
    }

    private fun ensureForeground() {
        if (foregroundActive || startRejected) return
        leases.attachOwner(owner)
        try {
            startForeground(NOTIFICATION_ID, notification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            foregroundActive = true
        } catch (failure: RuntimeException) {
            startRejected = true
            AndroidAgentLogger.warn("Execution service foreground failed: type=${failure.safeLogType()}")
            stopTasks(startFailed = true)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopTasks()
        } else if (intent?.action == ACTION_TOGGLE_PAUSE) {
            val targetRunId = intent.getStringExtra(EXTRA_TOGGLE_RUN_ID).orEmpty()
            val pausedNow = runPaused
            if (targetRunId.isNotBlank()) {
                // 必须离开主线程：onStartCommand 在主线程，而 AgentRuntimeClient 底层的
                // AgentRuntimeConnection.acquire 有主线程断言，直接调会抛
                // IllegalStateException 并被 withRuntimeMessenger 静默吞掉，
                // 表现为「点了暂停但运行没停」（随后运行跑完，看起来像被中断）。
                pauseDispatch.execute {
                    runCatching {
                        val client = AgentRuntimeClient(this, AndroidAgentLogger)
                        if (pausedNow) client.resumeRun() else client.pauseRun(targetRunId)
                    }.onFailure { throwable ->
                        AndroidAgentLogger.warn(
                            "Execution pause dispatch failed: type=${throwable.safeLogType()}",
                        )
                    }
                }
                // 乐观翻转本地暂停态再刷新：真正状态会由 RuntimeService 的
                // publishExecutionState 回写，但这里先翻一次，避免点完按钮后
                // 通知还停在旧文案上（看起来像点了没反应）。
                runPaused = !pausedNow
            }
            refreshNotification()
        } else {
            ensureForeground()
            refreshNotification()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        if (instance === this) instance = null
        runCatching { pauseDispatch.shutdownNow() }
        // 销毁时同样收回本服务拥有的任务。回收在独立有界工作线程上完成，不阻塞 Main。
        stopQueue.close(leases.drainOwner(owner))
        super.onDestroy()
    }

    private fun stopTasks(startFailed: Boolean = false) {
        val callbacks = leases.drain(startFailed)
        stopQueue.submit(callbacks) {
            mainHandler.post { if (instance === this) refreshNotification() }
        }
    }

    private fun refreshNotification() {
        if (leases.closeOwnerIfIdle(owner)) {
            foregroundActive = false
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } else {
            getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification())
        }
    }

    private fun notification(): Notification {
        val status = runStatus
        val conversationIdSnapshot = currentConversationId
        val archivedSourceSnapshot = currentArchivedSource
        val archivedKeySnapshot = currentArchivedKey
        val startedAtSnapshot = currentStartedAtEpochMs

        val openIntent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .apply {
                when {
                    conversationIdSnapshot.isNotBlank() -> {
                        action = MainActivity.ACTION_OPEN_CONVERSATION_ID
                        putExtra(MainActivity.EXTRA_CONVERSATION_ID, conversationIdSnapshot)
                    }
                    archivedSourceSnapshot.isNotBlank() && archivedKeySnapshot.isNotBlank() -> {
                        action = MainActivity.ACTION_OPEN_ARCHIVED_CONVERSATION
                        putExtra(MainActivity.EXTRA_HANDOFF_SOURCE, archivedSourceSnapshot)
                        putExtra(MainActivity.EXTRA_CONVERSATION_KEY, archivedKeySnapshot)
                    }
                }
            }
        val open = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, AgentExecutionService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val taskCount = if (activeRunCount > 0) activeRunCount else leases.count()
        val headline = executionHeadline(this, status, taskCount)
        val builder = Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(BRAND_COLOR)
            .setContentTitle(headline)
            // 第二行必须有内容：只有标题时系统按紧凑单行卡片排版，按钮行会压到图标与标题上
            // （HyperOS 的通知中心与灵动岛展开态都能复现）。
            .setContentText(resources.getQuantityString(R.plurals.execution_summary, taskCount.coerceAtLeast(1), taskCount.coerceAtLeast(1)))
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        if (status != null) {
            val runId = primaryRunId
            // Android 通知最多渲染 3 个 action，超出的直接不显示（HyperOS 上还会把卡片撑破、
            // 按钮条压到卡片下沿）。有 run 时三个位置分给 暂停/继续、插入、停止，
            // 「打开」交给 contentIntent——点卡片正文同样能进来。
            if (runId == null) {
                builder.addAction(
                    Notification.Action.Builder(null, getString(R.string.execution_action_open), open).build(),
                )
            }
            if (runId != null) {
                // 暂停 / 继续：与 App 内运行键同一套控制通道（MSG_PAUSE / MSG_RESUME）。
                val pauseIntent = PendingIntent.getService(
                    this, 3,
                    Intent(this, AgentExecutionService::class.java)
                        .setAction(ACTION_TOGGLE_PAUSE)
                        .putExtra(EXTRA_TOGGLE_RUN_ID, runId),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                // icon 传 null：从 Android 7.0 起 AOSP 的通知动作条不再渲染 Action 图标，
                // 只显示文字标签，HyperOS 同样如此，给图标也不会画出来。
                builder.addAction(
                    Notification.Action.Builder(
                        null,
                        if (runPaused) "继续" else "暂停",
                        pauseIntent,
                    ).build(),
                )
                // 插入对话：独立栈的透明输入层，交出输入后自动关闭。
                val supplement = PendingIntent.getActivity(
                    this, 4,
                    Intent(this, SupplementActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra(SupplementActivity.EXTRA_RUN_ID, runId),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                builder.addAction(
                    Notification.Action.Builder(null, "插入", supplement).build(),
                )
            }
            builder
                .addAction(
                    Notification.Action.Builder(
                        null,
                        getString(R.string.execution_action_stop),
                        stop,
                    ).build(),
                )
        } else {
            builder
                .setContentText(resources.getQuantityString(R.plurals.execution_summary, taskCount, taskCount))
                .addAction(
                    Notification.Action.Builder(
                        null,
                        getString(R.string.execution_stop),
                        stop,
                    ).build(),
                )
        }

        // 只有 Agent run 在跑、且当前未由 GUI 悬浮窗接管顶部视觉位时才申请提升为 Live Update；
        // GUI 运行或虚拟屏微缩镜显示期间退回普通前台通知，避免顶部同时出现两个胶囊冲突。
        if (status != null && !suppressLiveUpdate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            builder
                .setStyle(Notification.BigTextStyle().bigText(""))
                .setShortCriticalText(status.shortText())
            if (startedAtSnapshot > 0L) {
                builder
                    .setWhen(startedAtSnapshot)
                    .setShowWhen(true)
                    .setUsesChronometer(true)
            }
            // 请求方法从 API 36.1 才公开；标准 extra 也可供支持该协议的 API 36 系统读取。
            builder.extras.putBoolean("android.requestPromotedOngoing", true)
        } else if (status != null) {
            builder.setContentText(resources.getQuantityString(R.plurals.execution_summary, taskCount, taskCount))
        }
        return builder.build()
    }

    /** 状态栏胶囊约 7 个字符可完整显示；更长时只取动作，具体目标留在应用内。 */
    private fun AgentOverlayStatus.shortText(): String = when (this) {
        is AgentOverlayStatus.RunningTool, is AgentOverlayStatus.HostedToolRunning -> getString(R.string.execution_chip_running)
        AgentOverlayStatus.Paused -> getString(R.string.overlay_paused)
        AgentOverlayStatus.Stopping -> getString(R.string.overlay_stopping)
        AgentOverlayStatus.GeneratingAnswer -> getString(R.string.execution_chip_answering)
        else -> getString(R.string.overlay_reasoning)
    }

    companion object {
        private const val CHANNEL = "eta_execution"
        private const val NOTIFICATION_ID = 1107
        private const val ACTION_STOP = "io.github.mangi.eta.action.STOP_USER_EXECUTION"
        private const val ACTION_TOGGLE_PAUSE = "io.github.mangi.eta.action.TOGGLE_RUN_PAUSE"
        private const val EXTRA_TOGGLE_RUN_ID = "io.github.mangi.eta.extra.TOGGLE_RUN_ID"
        private const val BRAND_COLOR = 0xFF4285F4.toInt()
        private val leases = ExecutionLeaseRegistry()
        private val ownerSequence = AtomicLong()
        private val mainHandler = Handler(Looper.getMainLooper())
        @Volatile private var instance: AgentExecutionService? = null
        private var runStatus: AgentOverlayStatus? = null
        private var activeRunCount = 0
        /** 当前主运行的 runId 与暂停态：通知上的暂停按钮按它投递。 */
        private var primaryRunId: String? = null
        private var runPaused = false
        private var currentConversationId: String = ""
        private var currentArchivedSource: String = ""
        private var currentArchivedKey: String = ""
        private var currentStartedAtEpochMs: Long = 0L
        private var suppressLiveUpdate: Boolean = false
        private var publishedSignature: String? = null

        /**
         * Agent run 的当前状态，供通知与状态栏胶囊展示；null 表示没有 Agent run 在跑。
         * 只在离散展示字段变化时刷新通知，避免流式事件把通知更新频率推到系统限流。
         */
        fun updateRunStatus(
            context: Context,
            status: AgentOverlayStatus?,
            activeRunCount: Int,
            conversationId: String = "",
            archivedSource: String = "",
            archivedKey: String = "",
            startedAtEpochMs: Long = 0L,
            suppressLiveUpdate: Boolean = false,
            primaryRunId: String? = null,
            isPaused: Boolean = false,
        ) {
            mainHandler.post {
                runStatus = status
                this.activeRunCount = activeRunCount
                this.primaryRunId = primaryRunId
                this.runPaused = isPaused
                currentConversationId = conversationId
                currentArchivedSource = archivedSource
                currentArchivedKey = archivedKey
                currentStartedAtEpochMs = startedAtEpochMs
                this.suppressLiveUpdate = suppressLiveUpdate
                val count = if (activeRunCount > 0) activeRunCount else leases.count()
                val headline = executionHeadline(context, status, count)
                // isPaused 必须参与签名：暂停只改按钮文案与图标，标题和计数都不变，
                // 漏掉它时通知不会重建，暂停键会一直停在「暂停」而看起来像没生效。
                val signature = "$headline|$conversationId|$archivedSource|$archivedKey|$count|" +
                    "${status != null}|$suppressLiveUpdate|$primaryRunId|$isPaused"
                if (signature == publishedSignature) return@post
                publishedSignature = signature
                instance?.takeIf { it.foregroundActive }?.refreshNotification()
            }
        }

        private fun executionHeadline(
            context: Context,
            status: AgentOverlayStatus?,
            taskCount: Int,
        ): String {
            if (status == null) return context.getString(R.string.execution_title)
            val statusLabel = status.localizedText(context.resources)
            return if (taskCount > 1) "$statusLabel · $taskCount" else statusLabel
        }

        /** 必须从有效的用户入口取得引用，再创建会话或子进程；失败时调用方不启动任务。 */
        fun acquire(
            context: Context,
            id: String,
            allowBoundFallback: Boolean = false,
            onStop: () -> Unit,
        ): Boolean {
            if (instance?.startRejected == true) return false
            if (!leases.acquire(id, allowBoundFallback, onStop)) return true
            return try {
                context.applicationContext.startForegroundService(Intent(context, AgentExecutionService::class.java))
                true
            } catch (failure: RuntimeException) {
                leases.release(id)
                AndroidAgentLogger.warn("Execution service start rejected: type=${failure.safeLogType()}")
                false
            }
        }

        fun release(id: String) {
            leases.release(id)
            mainHandler.post { instance?.refreshNotification() }
        }
    }
}
