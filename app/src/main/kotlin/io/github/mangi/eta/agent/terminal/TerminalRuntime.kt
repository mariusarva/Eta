package io.github.mangi.eta.agent.terminal

import android.content.Context
import io.github.mangi.eta.core.AndroidAgentLogger
import io.github.mangi.eta.agent.device.RootAccess
import io.github.mangi.eta.agent.runtime.AgentExecutionService
import java.io.File

internal enum class LinuxExecutionBackend(val wireName: String) {
    CHROOT("chroot"),
    PROOT("proot"),
}

/** App 进程的终端运行条件；读取能力不会触发 su 授权。 */
internal object TerminalRuntime {
    /**
     * root 身份的**默认工作目录**，可被用户自定义覆盖（见 [rootWorkspacePath]）。
     *
     * 这个常量本身不再被直接消费——它只作为兜底值保留，
     * 因为 `.busybox` applet 目录曾经挂在它下面（编译期常量，不能运行时改）。
     * 现在 `.busybox` 用 [BUSYBOX_APPLET_DIR_FALLBACK]，与用户目录彻底解耦。
     */
    const val ROOT_WORKSPACE_PATH = "/data/local/tmp/eta"

    /**
     * `.busybox` applet 安装目录的固定位置。
     *
     * 必须固定：它由 `ShellProcessSupervisor` 在 shell 启动脚本里以编译期常量拼进
     * `busybox --install -s`，一旦跟随用户自定义目录变化，终端 console 会因为
     * 找不到 applet 而无法启动。用户改工作目录时不动这里。
     */
    const val BUSYBOX_APPLET_DIR_FALLBACK = "$ROOT_WORKSPACE_PATH/.busybox"
    const val LINUX_WORKSPACE_PATH = "/workspace"

    @Volatile private var appContext: Context? = null
    private var backgroundJobs: BackgroundTerminalJobs? = null

    @Synchronized
    fun backgroundJobs(context: Context): BackgroundTerminalJobs {
        val app = context.applicationContext
        return backgroundJobs ?: BackgroundTerminalJobs(
            supervisor = DetachedTaskSupervisor(AndroidAgentLogger, DetachedTaskSupervisor.defaultRecordsFile(app)),
            logger = AndroidAgentLogger,
            acquireLease = { onStop -> AgentExecutionService.acquire(app, "terminal-background-commands", onStop = onStop) },
            releaseLease = { AgentExecutionService.release("terminal-background-commands") },
            onCompleted = { BackgroundCommandNotification.show(app, it) },
            completionNotificationsEnabled = { app.getSystemService(android.app.NotificationManager::class.java).areNotificationsEnabled() },
        ).also { backgroundJobs = it }
    }

    fun acquireUserTask(id: String, onStop: () -> Unit): Boolean =
        appContext?.let { AgentExecutionService.acquire(it, id, onStop = onStop) } ?: true

    fun releaseUserTask(id: String) {
        if (appContext != null) AgentExecutionService.release(id)
    }

    fun initialize(context: Context) {
        appContext = context.applicationContext
        File(userWorkspacePath).mkdirs()
    }

    val rootAvailable: Boolean get() = RootAccess.isGranted
    val publicStorageGranted: Boolean get() = appContext != null && android.os.Environment.isExternalStorageManager()
    val nativeLibraryDir: File? get() = appContext?.applicationInfo?.nativeLibraryDir?.let(::File)
    val userWorkspacePath: String get() = appContext?.let {
        // 与文件区同一份解析：用户自定义目录优先，否则回落到 App 私有 workspace。
        WorkspaceRoot.resolve(it).absolutePath
    } ?: File(System.getProperty("java.io.tmpdir"), "eta-terminal-workspace").absolutePath
    val temporaryDirectory: File get() = appContext?.let { File(it.cacheDir, "terminal/proot") }
        ?: File(System.getProperty("java.io.tmpdir"), "eta-proot")

    fun defaultIdentity(environment: TerminalEnvironment, rootfsPath: String? = null): String = when {
        environment.isLinux -> if (LinuxEnvironmentPaths.backendOf(rootfsPath) == LinuxExecutionBackend.PROOT) "user" else "root"
        rootAvailable -> "root"
        else -> "user"
    }

    /**
     * root 身份实际使用的默认工作目录。
     *
     * 用户可在设置里自定义（与文件区同一套 UI），未设置时回落到
     * [ROOT_WORKSPACE_PATH]。只影响「进 shell 时的 cwd」，
     * 不影响 `.busybox` 安装位置，也不影响 su 授权与任何系统路径。
     */
    val rootWorkspacePath: String get() = appContext?.let {
        WorkspaceRoot.resolveRoot(it).absolutePath
    } ?: ROOT_WORKSPACE_PATH

    fun workspace(identity: String): String =
        if (identity == "root") rootWorkspacePath else userWorkspacePath

    fun nativeExecutable(name: String): File? = nativeLibraryDir?.let { File(it, name) }
        ?.takeIf { it.isFile && it.canExecute() }
}
