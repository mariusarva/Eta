package io.github.mangi.eta.agent.terminal

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import io.github.mangi.eta.agent.device.AgentFileReferenceGateway
import io.github.mangi.eta.config.Prefs
import java.io.File

/**
 * 工作目录的统一解析入口。
 *
 * 三处消费者必须拿到同一个答案，否则「文件区看到的」和「终端能进去的」会错位：
 * - [io.github.mangi.eta.ui.app.WorkspaceFileStore] 的文件区浏览
 * - [TerminalRuntime.userWorkspacePath] 的 user 身份默认 cwd
 * - 设置页的「工作目录」展示
 *
 * root 身份走 [resolveRoot]：兜底是 [TerminalRuntime.ROOT_WORKSPACE_PATH]，
 * 但用户自定义时同样跟随。`.busybox` 安装位置不在这里，见
 * [TerminalRuntime.BUSYBOX_APPLET_DIR_FALLBACK]。
 */
internal object WorkspaceRoot {

    /**
     * 用户自定义目录；空串表示未设置，回落到 App 私有 workspace。
     *
     * 只接受绝对路径且真实存在的目录，避免 Prefs 里存了失效路径（比如外置 SD 卡拔出）
     * 之后整个文件区打不开。
     */
    fun custom(context: Context): String {
        val configured = Prefs.workspaceRoot()
        if (configured.isBlank()) return ""
        if (!configured.startsWith('/')) return ""
        val dir = File(configured)
        // 只校验「存在且是目录」。不能查 canRead()：文件区实际以 root 身份读写，
        // 而这里跑在 App 进程里，普通 UID 对 /storage/emulated/0 的 canRead() 是 false，
        // 拿它当判据会让已保存的自定义目录每次都被判失效并静默回落默认。
        return if (dir.isDirectory) configured else ""
    }

    /** 实际生效的工作目录（user 身份与文件区）。 */
    fun resolve(context: Context): File {
        val custom = custom(context)
        if (custom.isNotBlank()) return File(custom)
        return TerminalPrivateStorage.workspace(context.applicationContext.filesDir)
    }

    /**
     * root 身份实际使用的默认工作目录。
     *
     * 与 [resolve] 分开是因为两者兜底值不同：user 兜底到 App 私有目录，
     * root 兜底到 `/data/local/tmp/eta`。但自定义时读同一个 Prefs 键——
     * 用户在文件区设了目录，root 进 shell 也就落在那里。
     */
    fun resolveRoot(context: Context): File {
        val custom = custom(context)
        if (custom.isNotBlank()) return File(custom)
        return File(TerminalRuntime.ROOT_WORKSPACE_PATH)
    }

    /** 展示用：当前生效目录的绝对路径。 */
    fun displayPath(context: Context): String = resolve(context).absolutePath

    /** 是否正在使用自定义目录。 */
    fun isCustom(context: Context): Boolean = custom(context).isNotBlank()

    /**
     * 把 SAF 目录 URI 落成绝对路径并持久化授权。
     *
     * 只处理能映射到真实文件系统的 provider：
     * - 主存储 `primary:xxx` → /storage/emulated/0/xxx
     * - 外置卡 `XXXX-XXXX:xxx` → /storage/XXXX-XXXX/xxx
     *
     * 其他 provider（网盘、压缩包等）没有对应的绝对路径，返回 null 由调用方提示。
     */
    fun fromTreeUri(context: Context, uri: Uri): String? {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        }
        val documentId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull() ?: return null
        val path = mapDocumentId(uri.authority, documentId) ?: return null
        val dir = File(path)
        // 同 custom()：不查 canRead()，权限由 backend 在真正读写时判定。
        return if (dir.isDirectory) path else null
    }

    private fun mapDocumentId(authority: String?, documentId: String): String? {
        if (authority != AgentFileReferenceGateway.EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY) return null
        // 主存储走现成实现，保持与文件引用同一套路径规则。
        AgentFileReferenceGateway.mapPrimaryStorageDocument(authority, documentId)?.let { return it }
        // 外置 SD 卡：documentId 形如 "1234-5678:some/dir"，卷 ID 直接是 /storage 下的目录名。
        val separator = documentId.indexOf(':')
        if (separator <= 0) return null
        val volume = documentId.substring(0, separator)
        val relative = documentId.substring(separator + 1)
        if (volume.equals("primary", ignoreCase = true)) return null
        if (relative.startsWith('/') || relative.split('/').any { it == "." || it == ".." }) return null
        val root = File(Environment.getExternalStorageDirectory().parentFile, volume)
        return if (relative.isEmpty()) root.absolutePath else File(root, relative).absolutePath
    }

    /**
     * 校验一个手输的绝对路径能否作为工作目录。
     *
     * 手动输入是必要的：Android 11+ 的 SAF 不允许把存储根目录
     * （/storage/emulated/0 本身）选成 tree，只能选它的子目录。
     * 想直接用存储根当工作区，只能手输。
     *
     * 返回 null 表示可用，否则返回失败原因（直接给用户看）。
     */
    fun validateManualPath(path: String): String? {
        val trimmed = path.trim()
        if (trimmed.isEmpty()) return "EMPTY"
        if (!trimmed.startsWith('/')) return "NOT_ABSOLUTE"
        if (trimmed.split('/').any { it == ".." }) return "HAS_PARENT_REF"
        val dir = File(trimmed)
        if (!dir.exists()) return "NOT_EXIST"
        if (!dir.isDirectory) return "NOT_DIRECTORY"
        // 这里不能查 canRead()：文件区在 Root 已授权时走 ShellFileToolBackend（root 身份），
        // 而本函数跑在 App 进程里。App 是普通 UID，对 /storage/emulated/0 的
        // File.canRead() 会返回 false —— 拿它当判据会把本来完全可用的路径直接拒掉
        // （实测：输入 /storage/emulated/0 点确认报「路径无效或不可读写」）。
        // 真正的读写能力由 backend 在列目录/写文件时校验，失败会走既有错误码。
        return null
    }

    /** 写入设置；传空串表示恢复默认。 */
    fun save(context: Context, path: String) {
        runCatching {
            val editor = Prefs.localAgentPreferences()?.edit() ?: return@runCatching
            editor.putString(Prefs.Keys.AGENT_WORKSPACE_ROOT, path.trim())
            editor.apply()
        }
    }
}
