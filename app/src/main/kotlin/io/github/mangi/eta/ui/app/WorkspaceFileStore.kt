package io.github.mangi.eta.ui.app

import android.content.Context
import android.net.Uri
import io.github.mangi.eta.agent.device.AgentFileReferenceGateway
import io.github.mangi.eta.agent.device.RootAccess
import io.github.mangi.eta.agent.terminal.FileToolBackend
import io.github.mangi.eta.agent.terminal.FileToolKind
import io.github.mangi.eta.agent.terminal.LocalFileToolBackend
import io.github.mangi.eta.agent.terminal.ShellFileToolBackend
import io.github.mangi.eta.agent.terminal.ShellProcessSupervisor
import io.github.mangi.eta.agent.terminal.WorkspaceRoot
import io.github.mangi.eta.agent.terminal.runOneShotShell
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

internal data class WorkspaceEntry(val path: String, val name: String, val directory: Boolean, val size: Long)

internal class WorkspaceFileStore(
    context: Context,
    private val backendFor: (File) -> FileToolBackend = ::workspaceBackend,
) {
    private val appContext = context.applicationContext
    // 支持自定义工作目录（可指向 SDcard）；未设置时回落到 App 私有 workspace。
    private val root: File get() = WorkspaceRoot.resolve(appContext).canonicalFile

    /**
     * 复用同一个 backend。
     *
     * 之前每次 [list] / [exportFile] 都调 backendFor(root)，而 workspaceBackend
     * 每次都 new 一个 ShellProcessSupervisor 并现拼 root shell 命令。
     * 一次目录列举内部要跑 4 次 shell（两次 stat + find + statPage），
     * 分页时还要翻倍，软链再逐个 stat —— 于是滑到文件页要等好几秒。
     * supervisor 本身无状态，缓存下来就能省掉反复构造的开销。
     */
    private var cachedBackend: FileToolBackend? = null
    private var cachedBackendRoot: File? = null

    private fun backend(): FileToolBackend {
        val currentRoot = root
        val existing = cachedBackend
        if (existing != null && cachedBackendRoot == currentRoot) return existing
        return backendFor(currentRoot).also {
            cachedBackend = it
            cachedBackendRoot = currentRoot
        }
    }

    suspend fun list(path: String): List<WorkspaceEntry> = withContext(Dispatchers.IO) {
        ensureActive()
        if (!root.mkdirs() && !root.isDirectory) throw java.io.IOException("WORKSPACE_DIRECTORY_UNAVAILABLE")
        val directory = resolveFile(path)
        // 直读优先：File.listFiles() 是本地调用，毫秒级。
        // 之前无条件走 backend，而 backend 每条路径都要起一次 root shell，
        // 一个目录 4 次、分页翻倍、软链再逐个 stat —— 滑到文件页要等好几秒。
        // App UID 读不到时（受权限限制的目录）再回退到 backend。
        val children = runCatching { directory.listFiles() }.getOrNull()
        if (children == null) return@withContext listWithBackend(path)

        val rootPath = root.toPath()
        children.asSequence()
            .mapNotNull { child ->
                // 符号链接不跟随：跟随可能绕出工作区，也可能在循环链接上打转。
                if (java.nio.file.Files.isSymbolicLink(child.toPath())) return@mapNotNull null
                val isDirectory = child.isDirectory
                if (!isDirectory && !child.isFile) return@mapNotNull null
                val relative = runCatching { child.relativeTo(root).path }.getOrNull() ?: return@mapNotNull null
                if (!child.toPath().startsWith(rootPath)) return@mapNotNull null
                WorkspaceEntry(
                    path = relative,
                    name = child.name,
                    directory = isDirectory,
                    size = if (isDirectory) 0L else child.length(),
                )
            }
            .sortedWith(compareByDescending<WorkspaceEntry> { it.directory }.thenBy { it.name.lowercase() })
            .toList()
    }

    /** App UID 读不到时的兜底：走 root backend 分页列举。 */
    private suspend fun listWithBackend(path: String): List<WorkspaceEntry> {
        val backend = backend()
        val directory = resolve(backend, path)
        val entries = mutableListOf<WorkspaceEntry>()
        val rootPath = root.toPath()
        var offset = 0
        var revision: String? = null
        do {
            ensureActive()
            val page = backend.list(directory, offset, 200, revision)
            revision = page.revision
            page.entries.filter { it.kind == FileToolKind.FILE || it.kind == FileToolKind.DIRECTORY }
                .forEach { entry ->
                    if (File(entry.path).toPath().startsWith(rootPath)) {
                        entries += WorkspaceEntry(
                            path = File(entry.path).relativeTo(root).path,
                            name = entry.name,
                            directory = entry.kind == FileToolKind.DIRECTORY,
                            size = entry.sizeBytes,
                        )
                    }
                }
            if (!page.hasMore) break
            check(page.nextOffset > offset) { "WORKSPACE_INVALID_PAGE" }
            offset = page.nextOffset
        } while (true)
        return entries.sortedWith(
            compareByDescending<WorkspaceEntry> { it.directory }.thenBy { it.name.lowercase() },
        )
    }

    /** 相对路径 → root 内的真实 File，并校验没有越出 root。 */
    private fun resolveFile(path: String): File {
        val candidate = if (path.isBlank()) root else File(root, path)
        val canonical = candidate.canonicalFile
        require(canonical.toPath().startsWith(root.toPath())) { "WORKSPACE_PATH_OUTSIDE_ROOT" }
        return canonical
    }

    suspend fun importFile(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        AgentFileReferenceGateway.importDocumentUri(appContext, uri) is AgentFileReferenceGateway.Resolution.Success
    }

    suspend fun exportFile(path: String, destination: Uri) = withContext(Dispatchers.IO) {
        val backend = backend()
        val source = resolve(backend, path)
        val stat = backend.stat(source)
        check(stat.kind == FileToolKind.FILE) { "WORKSPACE_NOT_FILE" }
        val output = appContext.contentResolver.openOutputStream(destination, "wt")
            ?: throw java.io.IOException("EXPORT_UNAVAILABLE")
        output.use {
            val file = File(source)
            if (file.canRead()) {
                file.inputStream().use { input ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                    }
                }
            } else {
                var offset = 0L
                while (offset < stat.sizeBytes) {
                    ensureActive()
                    val bytes = backend.readBytes(source, offset, 16 * 1024)
                    check(bytes.isNotEmpty()) { "WORKSPACE_FILE_CHANGED" }
                    output.write(bytes)
                    offset += bytes.size
                }
            }
        }
    }

    private fun resolve(backend: FileToolBackend, path: String): String = backend.resolve(path).also {
        require(File(it).toPath().startsWith(root.toPath())) { "WORKSPACE_PATH_OUTSIDE_ROOT" }
    }
}

private fun workspaceBackend(root: File): FileToolBackend {
    if (!RootAccess.isGranted) return LocalFileToolBackend(root)
    val supervisor = ShellProcessSupervisor()
    return ShellFileToolBackend("android", "root", root.path, root.path) { command, input, limit ->
        runOneShotShell(supervisor, "root", command, timeoutSeconds = 20, stdin = input, maxOutputBytes = limit)
    }
}
