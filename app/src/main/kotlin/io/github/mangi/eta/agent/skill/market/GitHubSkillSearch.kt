package io.github.mangi.eta.agent.skill.market

import io.github.mangi.eta.agent.model.AgentHttpClient
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

/**
 * 在 GitHub 上搜技能。
 *
 * 两条通道：
 * 1. 仓库搜索（`/search/repositories`）：未登录也能用，按名字/描述/话题找可能含技能的仓库；
 * 2. 代码搜索（`/search/code`）：GitHub 要求必须认证，登录后可用，能直接定位到 SKILL.md 所在目录。
 *
 * 都只读取公开数据，不写、不 clone；安装仍走 SkillPackageInstaller。
 */
internal object GitHubSkillSearch {

    private const val API = "https://api.github.com"
    private const val PER_PAGE = 20
    private const val MAX_REPO_ARCHIVE_BYTES = 64L * 1024L * 1024L

    private val client by lazy {
        AgentHttpClient.client.newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    internal sealed interface Result {
        data class Ok(val entries: List<SkillMarketEntry>, val mode: Mode) : Result
        data class Failed(val message: String, val needsAuth: Boolean = false) : Result
    }

    internal enum class Mode { REPOSITORY, CODE }

    /**
     * @param token GitHub token；为空时只走仓库搜索。
     */
    suspend fun search(query: String, token: String?): Result = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext Result.Failed("关键词至少 2 个字符")

        // 有 token 先试代码搜索：能精确找到 SKILL.md 目录，结果直接可装。
        if (!token.isNullOrBlank()) {
            when (val codeResult = searchCode(trimmed, token)) {
                is Result.Ok -> if (codeResult.entries.isNotEmpty()) return@withContext codeResult
                is Result.Failed -> if (codeResult.needsAuth) {
                    // token 失效就退回仓库搜索，不把用户卡在错误里。
                }
            }
        }
        searchRepositories(trimmed, token)
    }

    private fun searchCode(query: String, token: String): Result {
        val url = buildString {
            append(API).append("/search/code")
            append("?q=").append(encode("filename:SKILL.md $query"))
            append("&per_page=").append(PER_PAGE)
        }
        return runCatching {
            val body = get(url, token) ?: return Result.Failed("代码搜索无响应")
            val root = JSONObject(body)
            if (root.has("message")) {
                val message = root.optString("message")
                val needsAuth = root.optInt("status", 0) == 401 || message.contains("authentication", true)
                return Result.Failed(message, needsAuth)
            }
            val items = root.optJSONArray("items") ?: return Result.Ok(emptyList(), Mode.CODE)
            val entries = buildList {
                for (i in 0 until items.length()) {
                    parseCodeItem(items.optJSONObject(i))?.let(::add)
                }
            }.distinctBy { it.repo + "#" + it.path }
            Result.Ok(entries, Mode.CODE)
        }.getOrElse { Result.Failed(it.message ?: "代码搜索失败") }
    }

    private fun searchRepositories(query: String, token: String?): Result {
        val url = buildString {
            append(API).append("/search/repositories")
            append("?q=").append(encode("$query skill"))
            append("&sort=stars&order=desc&per_page=").append(PER_PAGE)
        }
        return runCatching {
            val body = get(url, token) ?: return Result.Failed("仓库搜索无响应")
            val root = JSONObject(body)
            if (root.has("message")) return Result.Failed(root.optString("message"))
            val items = root.optJSONArray("items") ?: return Result.Ok(emptyList(), Mode.REPOSITORY)
            val entries = buildList {
                for (i in 0 until items.length()) {
                    parseRepositoryItem(items.optJSONObject(i))?.let(::add)
                }
            }
            Result.Ok(entries, Mode.REPOSITORY)
        }.getOrElse { Result.Failed(it.message ?: "仓库搜索失败") }
    }

    private fun parseCodeItem(item: JSONObject?): SkillMarketEntry? {
        if (item == null) return null
        val repoFull = item.optJSONObject("repository")?.optString("full_name").orEmpty()
        if (!repoPattern.matches(repoFull)) return null
        val ref = item.optJSONObject("repository")?.optString("default_branch").orEmpty().ifBlank { "main" }
        if (!refPattern.matches(ref)) return null
        val filePath = item.optString("path").trim()
        // path 形如 skills/pdf/SKILL.md，技能根目录是它的父目录。
        if (!filePath.endsWith("/SKILL.md") && filePath != "SKILL.md") return null
        val dir = filePath.removeSuffix("/SKILL.md").removeSuffix("SKILL.md").trimEnd('/')
        val path = dir.ifBlank { "." }
        if (!pathPattern.matches(path)) return null
        val name = path.substringAfterLast('/')
        return SkillMarketEntry(
            id = (repoFull.replace('/', '-') + "-" + name).lowercase().take(64),
            title = name,
            summary = "来自 $repoFull 的技能（$path）",
            repo = repoFull,
            ref = ref,
            path = path,
            author = repoFull.substringBefore('/'),
            tags = listOf("GitHub"),
            homepage = "https://github.com/$repoFull/tree/$ref/$path",
        )
    }

    private fun parseRepositoryItem(item: JSONObject?): SkillMarketEntry? {
        if (item == null) return null
        val repoFull = item.optString("full_name").trim()
        if (!repoPattern.matches(repoFull)) return null
        val ref = item.optString("default_branch").trim().ifBlank { "main" }
        if (!refPattern.matches(ref)) return null
        val name = item.optString("name").trim().ifBlank { repoFull.substringAfterLast('/') }
        val description = item.optString("description").orEmpty().trim()
        val topics = buildList {
            item.optJSONArray("topics")?.let { array ->
                for (i in 0 until minOf(array.length(), 6)) {
                    val tag = array.optString(i).trim()
                    if (tag.isNotBlank() && tag.length <= 24) add(tag)
                }
            }
        }
        return SkillMarketEntry(
            id = repoFull.replace('/', '-').lowercase().take(64),
            title = name,
            summary = description.ifBlank { "GitHub 仓库 $repoFull" }.take(600),
            repo = repoFull,
            ref = ref,
            // 仓库根：安装时由安装器在归档里按 SKILL.md 自动发现候选。
            path = ".",
            author = repoFull.substringBefore('/'),
            tags = topics.ifEmpty { listOf("GitHub") },
            homepage = item.optString("html_url").takeIf { it.startsWith("https://") },
        )
    }

    private fun get(url: String, token: String?): String? {
        val builder = Request.Builder()
            .url(url)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "Eta-Skill-Market")
            .get()
        if (!token.isNullOrBlank()) builder.header("Authorization", "Bearer $token")
        client.newCall(builder.build()).execute().use { response ->
            val text = response.body.string()
            if (!response.isSuccessful) {
                val message = runCatching { JSONObject(text).optString("message") }.getOrNull()
                error(message?.takeIf { it.isNotBlank() } ?: "HTTP ${response.code}")
            }
            return text
        }
    }

    private fun encode(value: String): String =
        java.net.URLEncoder.encode(value, "UTF-8")

    private val repoPattern = Regex("^[A-Za-z0-9][A-Za-z0-9-]{0,38}/[A-Za-z0-9._-]{1,100}$")
    private val refPattern = Regex("^[A-Za-z0-9][A-Za-z0-9._/-]{0,120}$")
    private val pathPattern = Regex("^(\\.|[A-Za-z0-9._-]{1,80}(/[A-Za-z0-9._-]{1,80}){0,6})$")
}
