package io.github.mangi.eta.ui.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.mangi.eta.agent.model.AgentHttpClient
import io.github.mangi.eta.core.AndroidAgentLogger
import io.github.mangi.eta.core.safeLogType
import io.github.mangi.eta.agent.skill.SkillInstallConflict
import io.github.mangi.eta.agent.skill.SkillInstallErrorCode
import io.github.mangi.eta.agent.skill.SkillInstallResult
import io.github.mangi.eta.agent.skill.SkillRuntime
import io.github.mangi.eta.agent.skill.market.GitHubSkillSearch
import io.github.mangi.eta.data.repository.GitHubAccountStore
import io.github.mangi.eta.agent.skill.market.SkillMarketCatalog
import io.github.mangi.eta.agent.skill.market.SkillMarketEntry
import io.github.mangi.eta.agent.skill.market.SkillMarketParser
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

/**
 * 技能市场：内置目录 + 从 GitHub 仓库归档安装。
 *
 * 安装复用 SkillPackageInstaller.installRepositoryZip，按条目里的 path 选技能，
 * 归档先落到应用 cache，安装完成即删除。不执行任何仓库脚本。
 */
internal class SkillMarketStore(
    context: Context,
    private val scope: CoroutineScope,
) {
    private val appContext = context.applicationContext
    private val installer by lazy { SkillRuntime.createPackageInstaller(appContext) }
    private val httpClient by lazy {
        AgentHttpClient.client.newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    var catalog by mutableStateOf(SkillMarketCatalog.EMPTY)
        private set
    var loading by mutableStateOf(false)
        private set
    var installingId by mutableStateOf<String?>(null)
        private set
    var notice by mutableStateOf<SkillMarketNotice?>(null)
        private set
    var installedVersion by mutableStateOf(0)
        private set

    /** GitHub 搜索结果（与内置目录分开显示）。 */
    var searchResults by mutableStateOf<List<SkillMarketEntry>>(emptyList())
        private set
    var searching by mutableStateOf(false)
        private set
    var searchMode by mutableStateOf<GitHubSkillSearch.Mode?>(null)
        private set
    var searchQuery by mutableStateOf("")
        private set
    var searchNeedsAuth by mutableStateOf(false)
        private set

    /**
     * 在 GitHub 上搜技能。已登录 GitHub 时用代码搜索直接定位 SKILL.md；
     * 未登录退回仓库搜索（GitHub 的代码搜索强制要求认证）。
     */
    fun search(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            searchResults = emptyList()
            searchMode = null
            searchNeedsAuth = false
            return
        }
        if (searching) return
        searching = true
        searchQuery = trimmed
        searchNeedsAuth = false
        scope.launch(Dispatchers.IO) {
            val token = runCatching { GitHubAccountStore(appContext).load()?.token }.getOrNull()
            val result = GitHubSkillSearch.search(trimmed, token)
            withContext(Dispatchers.Main) {
                searching = false
                when (result) {
                    is GitHubSkillSearch.Result.Ok -> {
                        searchResults = result.entries
                        searchMode = result.mode
                        if (result.entries.isEmpty()) {
                            notice = SkillMarketNotice("没有找到技能", "换个关键词试试", isError = false)
                        }
                    }
                    is GitHubSkillSearch.Result.Failed -> {
                        searchResults = emptyList()
                        searchMode = null
                        searchNeedsAuth = result.needsAuth
                        notice = SkillMarketNotice(
                            "搜索失败",
                            if (result.needsAuth) {
                                "GitHub 代码搜索需要登录。到「GitHub 账号」连接后可以精确搜到 SKILL.md；当前已退回仓库搜索。"
                            } else {
                                result.message
                            },
                            isError = true,
                        )
                    }
                }
            }
        }
    }

    fun clearSearch() {
        searchResults = emptyList()
        searchMode = null
        searchNeedsAuth = false
        searchQuery = ""
        expandedRepo = null
        expanding = false
    }

    /** 当前展开的仓库（仓库搜索结果的「展开」态）。 */
    var expandedRepo by mutableStateOf<String?>(null)
        private set
    var expanding by mutableStateOf(false)
        private set

    /**
     * 展开仓库：列出它内部所有技能。
     *
     * 仓库搜索只知道仓库名，展开后用 `git/trees?recursive=1` 拿到全部 SKILL.md 路径，
     * 逐个可装，不依赖必须认证的代码搜索。
     */
    fun expandRepository(entry: SkillMarketEntry) {
        if (expanding) return
        expanding = true
        expandedRepo = entry.repo
        scope.launch(Dispatchers.IO) {
            val token = runCatching { GitHubAccountStore(appContext).load()?.token }.getOrNull()
            val result = GitHubSkillSearch.expandRepository(entry.repo, entry.ref, token)
            withContext(Dispatchers.Main) {
                expanding = false
                when (result) {
                    is GitHubSkillSearch.Result.Ok -> {
                        if (result.entries.isEmpty()) {
                            expandedRepo = null
                            notice = SkillMarketNotice(
                                "这个仓库里没有技能",
                                "${entry.repo} 中没有找到 SKILL.md",
                                isError = false,
                            )
                        } else {
                            // 展开结果替换搜索列表，并标记为精确路径（可直接安装）。
                            searchResults = result.entries
                            searchMode = GitHubSkillSearch.Mode.CODE
                            notice = SkillMarketNotice(
                                "找到 ${result.entries.size} 个技能",
                                "来自 ${entry.repo}，点任意一项即可安装",
                                isError = false,
                            )
                        }
                    }
                    is GitHubSkillSearch.Result.Failed -> {
                        expandedRepo = null
                        notice = SkillMarketNotice("展开失败", result.message, isError = true)
                    }
                }
            }
        }
    }

    fun loadCatalog() {
        if (catalog.entries.isNotEmpty() || loading) return
        loading = true
        scope.launch(Dispatchers.IO) {
            val parsed = runCatching {
                appContext.assets.open(ASSET_NAME).use { stream ->
                    SkillMarketParser.parse(stream.readBytes().toString(Charsets.UTF_8))
                }
            }.getOrElse { SkillMarketCatalog.EMPTY }
            withContext(Dispatchers.Main) {
                catalog = parsed
                loading = false
                if (parsed.entries.isEmpty()) {
                    notice = SkillMarketNotice("技能目录为空", "内置目录读取失败，请重装应用", isError = true)
                }
            }
        }
    }

    fun install(entry: SkillMarketEntry) {
        if (installingId != null) return
        installingId = entry.id
        notice = null
        AndroidAgentLogger.info("Skill market install start: id=${entry.id}, path=${entry.path}, url=${entry.archiveUrl}")
        scope.launch(Dispatchers.IO) {
            val result = runCatching {
                val archive = downloadArchive(entry)
                AndroidAgentLogger.info("Skill market archive downloaded: id=${entry.id}, bytes=${archive.length()}")
                try {
                    installer.installRepositoryZip(
                        openStream = { archive.inputStream() },
                        selectedPaths = listOf(entry.path),
                    )
                } finally {
                    archive.delete()
                }
            }.getOrElse { failure ->
                AndroidAgentLogger.warn(
                    "Skill market install threw: id=${entry.id}, type=${failure.safeLogType()}, msg=${failure.message}",
                )
                withContext(Dispatchers.Main) {
                    installingId = null
                    notice = SkillMarketNotice(
                        "安装失败",
                        failure.message ?: "下载或解压技能包时出错",
                        isError = true,
                    )
                }
                return@launch
            }
            AndroidAgentLogger.info("Skill market install result: id=${entry.id}, result=${result::class.simpleName}")
            withContext(Dispatchers.Main) {
                installingId = null
                notice = when (result) {
                    is SkillInstallResult.Success -> {
                        installedVersion++
                        val names = result.installed.joinToString("、") { it.name }
                        SkillMarketNotice("已安装", "$names 已加入技能列表，返回技能页即可启用", isError = false)
                    }
                    is SkillInstallResult.Conflict -> SkillMarketNotice(
                        "已存在同名技能",
                        conflictMessage(result.conflicts),
                        isError = true,
                    )
                    is SkillInstallResult.Failure -> SkillMarketNotice(
                        "安装失败",
                        failureMessage(result.error.code, result.error.message),
                        isError = true,
                    )
                }
            }
        }
    }

    fun dismissNotice() {
        notice = null
    }

    private suspend fun downloadArchive(entry: SkillMarketEntry): File {
        val request = Request.Builder()
            .url(entry.archiveUrl)
            .header("User-Agent", "Eta-Skill-Market (+https://github.com/Mari0us/Eta)")
            .get()
            .build()
        val directory = File(appContext.cacheDir, "skill-market").also { it.mkdirs() }
        val target = File(directory, "${entry.id}.zip")
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("下载技能包失败：HTTP ${response.code}")
            val body = response.body
            if (body.contentLength() > MAX_ARCHIVE_BYTES) error("技能包超过 64 MB 限制")
            target.outputStream().use { output ->
                var total = 0L
                body.byteStream().use { input ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > MAX_ARCHIVE_BYTES) error("技能包超过 64 MB 限制")
                        output.write(buffer, 0, count)
                    }
                }
                if (total == 0L) error("技能包为空")
            }
        }
        return target
    }

    private fun conflictMessage(conflicts: List<SkillInstallConflict>): String {
        val names = conflicts.joinToString("、") { it.name }
        return "$names 已经在技能列表里。先到技能页删除同名技能，再回来安装。"
    }

    private fun failureMessage(code: SkillInstallErrorCode, message: String): String = when (code) {
        SkillInstallErrorCode.NO_SKILL_FOUND -> "仓库归档里没有找到 SKILL.md"
        SkillInstallErrorCode.MULTIPLE_SKILLS_FOUND -> "仓库归档含多个技能，需要在条目里指定具体路径"
        SkillInstallErrorCode.INVALID_SELECTION -> "条目里的技能路径与归档内容不一致"
        SkillInstallErrorCode.ARCHIVE_TOO_LARGE,
        SkillInstallErrorCode.TOO_MANY_ENTRIES,
        SkillInstallErrorCode.ENTRY_TOO_LARGE,
        SkillInstallErrorCode.EXTRACTED_CONTENT_TOO_LARGE,
        SkillInstallErrorCode.ENTRY_PATH_TOO_DEEP -> "技能包超出体积或条目限制"
        SkillInstallErrorCode.UNSAFE_ENTRY_PATH,
        SkillInstallErrorCode.DUPLICATE_ENTRY -> "技能包路径不安全，已拒绝安装"
        SkillInstallErrorCode.INVALID_ARCHIVE -> "下载到的文件不是有效 ZIP"
        SkillInstallErrorCode.INVALID_SKILL -> "SKILL.md 内容不合规：$message"
        SkillInstallErrorCode.IO_ERROR -> "读写技能目录失败"
        else -> message
    }

    private companion object {
        const val ASSET_NAME = "skill_market.json"
        const val MAX_ARCHIVE_BYTES = 64L * 1024L * 1024L
    }
}

internal data class SkillMarketNotice(
    val title: String,
    val message: String,
    val isError: Boolean,
)
