package io.github.mangi.eta.agent.skill.market

import org.json.JSONObject

/*
 * Eta 技能市场目录。
 *
 * 数据源是随 APK 内置的 assets/skill_market.json（schema 1），条目指向公开 GitHub
 * 仓库里 SKILL.md 所在的子目录，安装时下载仓库归档并交给 SkillPackageInstaller
 * 按路径安装，格式与 SkillArchiveCandidate.relativePath 一致（仓库根用 "."）。
 *
 * 读取保持宽松：单条不合规只跳过该条，整份文件仍可用。
 */

internal enum class SkillMarketSource { EMPTY, ASSETS }

internal data class SkillMarketEntry(
    val id: String,
    val title: String,
    val summary: String,
    val repo: String,
    val ref: String,
    val path: String,
    val author: String?,
    val tags: List<String>,
    val homepage: String?,
) {
    /** 仓库归档地址；SkillPackageInstaller 只接受 ZIP。 */
    val archiveUrl: String get() = "https://codeload.github.com/$repo/zip/refs/heads/$ref"
}

internal data class SkillMarketCatalog(
    val entries: List<SkillMarketEntry>,
    val source: SkillMarketSource,
) {
    val tags: List<String> get() = entries.asSequence()
        .flatMap { it.tags.asSequence() }
        .distinct()
        .sorted()
        .toList()

    companion object {
        val EMPTY = SkillMarketCatalog(emptyList(), SkillMarketSource.EMPTY)
    }
}

internal object SkillMarketParser {
    private const val MAX_ENTRIES = 500
    private val repoPattern = Regex("^[A-Za-z0-9][A-Za-z0-9-]{0,38}/[A-Za-z0-9._-]{1,100}$")
    private val refPattern = Regex("^[A-Za-z0-9][A-Za-z0-9._/-]{0,120}$")
    private val idPattern = Regex("^[a-z0-9][a-z0-9._-]{0,63}$")
    private val pathPattern = Regex("^(\\.|[A-Za-z0-9._-]{1,80}(/[A-Za-z0-9._-]{1,80}){0,6})$")

    fun parse(raw: String): SkillMarketCatalog {
        val root = JSONObject(raw)
        require(root.optInt("schema", -1) == 1) { "skill_market.json schema 必须为 1" }
        val array = root.optJSONArray("skills") ?: return SkillMarketCatalog.EMPTY
        val entries = buildList {
            for (i in 0 until minOf(array.length(), MAX_ENTRIES)) {
                parseEntry(array.optJSONObject(i), i)?.let(::add)
            }
        }
        return SkillMarketCatalog(entries, SkillMarketSource.ASSETS)
    }

    private fun parseEntry(item: JSONObject?, index: Int): SkillMarketEntry? {
        if (item == null) return null
        val id = item.optString("id").trim().lowercase()
        if (!idPattern.matches(id)) return null
        val repo = item.optString("repo").trim()
        if (!repoPattern.matches(repo)) return null
        val ref = item.optString("ref").trim().ifBlank { "main" }
        if (!refPattern.matches(ref) || ref.contains("..")) return null
        val path = item.optString("path").trim().ifBlank { "." }
        if (!pathPattern.matches(path) || path.contains("..")) return null
        val title = item.optString("title").trim().takeIf { it.isNotBlank() }?.take(120) ?: id
        val summary = item.optString("summary").trim().take(600)
        val tags = buildList {
            val array = item.optJSONArray("tags") ?: return@buildList
            for (i in 0 until array.length()) {
                val tag = array.optString(i).trim()
                if (tag.isNotBlank() && tag.length <= 24) add(tag)
            }
        }.distinct().take(6)
        val homepage = item.optString("homepage").trim()
            .takeIf { it.startsWith("https://") && it.length <= 2048 }
        return SkillMarketEntry(
            id = id,
            title = title,
            summary = summary,
            repo = repo,
            ref = ref,
            path = path,
            author = item.optString("author").trim().takeIf { it.isNotBlank() }?.take(80),
            tags = tags,
            homepage = homepage,
        )
    }
}
