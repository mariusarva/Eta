package io.github.mangi.eta.ui.screens.market

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.R
import io.github.mangi.eta.agent.skill.market.SkillMarketCatalog
import io.github.mangi.eta.agent.skill.market.GitHubSkillSearch
import io.github.mangi.eta.agent.skill.market.SkillMarketEntry
import io.github.mangi.eta.ui.app.SkillMarketNotice
import io.github.mangi.eta.ui.components.ListEmptyState
import io.github.mangi.eta.ui.components.coui.CouiButton
import io.github.mangi.eta.ui.components.coui.CouiChipTabs
import io.github.mangi.eta.ui.components.coui.CouiButtonRole
import io.github.mangi.eta.ui.components.coui.CouiNoticeDialog
import io.github.mangi.eta.ui.components.coui.CouiPageBottomSpacer
import io.github.mangi.eta.ui.components.coui.CouiPreference
import io.github.mangi.eta.ui.components.coui.CouiPreferenceGroup
import io.github.mangi.eta.ui.components.coui.CouiScaffoldPage
import io.github.mangi.eta.ui.components.coui.CouiText
import io.github.mangi.eta.ui.components.coui.CouiTextKind
import io.github.mangi.eta.ui.components.coui.CouiTextField
import io.github.mangi.eta.ui.components.rememberDraftTextFieldState
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val ALL_TAG = "all"

private fun List<SkillMarketEntry>.matching(query: String, tag: String): List<SkillMarketEntry> {
    val trimmed = query.trim()
    return filter { entry ->
        val tagOk = tag == ALL_TAG || tag in entry.tags
        if (!tagOk) return@filter false
        if (trimmed.isEmpty()) return@filter true
        entry.title.contains(trimmed, ignoreCase = true) ||
            entry.summary.contains(trimmed, ignoreCase = true) ||
            entry.id.contains(trimmed, ignoreCase = true) ||
            entry.tags.any { it.contains(trimmed, ignoreCase = true) }
    }
}

/**
 * 技能市场：浏览内置目录并从公开仓库安装技能。
 *
 * 安装走 SkillPackageInstaller.installRepositoryZip，只按条目路径取技能，
 * 不执行仓库里的任何脚本。
 */
@Composable
internal fun SkillMarketScreen(
    catalog: SkillMarketCatalog,
    loading: Boolean,
    installingId: String?,
    notice: SkillMarketNotice?,
    onBack: () -> Unit,
    onInstall: (SkillMarketEntry) -> Unit,
    onDismissNotice: () -> Unit,
    modifier: Modifier = Modifier,
    searchResults: List<SkillMarketEntry> = emptyList(),
    searching: Boolean = false,
    searchMode: GitHubSkillSearch.Mode? = null,
    searchNeedsAuth: Boolean = false,
    onSearch: (String) -> Unit = {},
    onClearSearch: () -> Unit = {},
    expanding: Boolean = false,
    expandedRepo: String? = null,
    onExpand: (SkillMarketEntry) -> Unit = {},
) {
    var tag by rememberSaveable { mutableStateOf(ALL_TAG) }
    var query by rememberSaveable { mutableStateOf("") }
    val searchState = rememberDraftTextFieldState(query) { query = it }
    val filtered = remember(catalog.entries, query, tag) { catalog.entries.matching(query, tag) }

    CouiScaffoldPage(
        title = stringResource(R.string.route_skill_market),
        onBack = onBack,
        modifier = modifier,
        overlay = {
            // 弹窗必须挂在页面 overlay 槽里：写在 CouiScaffoldPage 外面时没有 overlay 宿主，
            // 安装结果提示不会出现，表现为「点了安装什么都不发生」。
            CouiNoticeDialog(
                visible = notice != null,
                title = notice?.title,
                message = notice?.message,
                confirmText = stringResource(R.string.ui_knew_cb63c6),
                onDismissRequest = onDismissNotice,
            )
        },
    ) {
        CouiTextField(
            state = searchState,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            placeholder = "搜索技能名或说明",
            singleLine = true,
        )

        // GitHub 搜索入口：内置目录搜不到时，直接去 GitHub 上找。
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CouiButton(
                text = if (searching) "搜索中…" else "在 GitHub 搜索",
                onClick = { onSearch(query) },
                enabled = query.trim().length >= 2 && !searching,
                role = CouiButtonRole.Primary,
                minHeight = 34.dp,
            )
            if (searchResults.isNotEmpty() || searchNeedsAuth) {
                Spacer(Modifier.width(8.dp))
                CouiButton(
                    text = "清除",
                    onClick = onClearSearch,
                    role = CouiButtonRole.Secondary,
                    minHeight = 34.dp,
                )
            }
        }
        if (searchNeedsAuth) {
            CouiText(
                text = "代码搜索需要登录 GitHub；未登录时按仓库搜索，只能找到仓库，安装时自动在仓库里找 SKILL.md。",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
                kind = CouiTextKind.GroupFooter,
                color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        if (searching) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                InfiniteProgressIndicator(size = 22.dp)
            }
        }

        if (searchResults.isNotEmpty()) {
            CouiPreferenceGroup(
                title = when (searchMode) {
                    GitHubSkillSearch.Mode.CODE -> "GitHub 找到 ${searchResults.size} 个技能"
                    GitHubSkillSearch.Mode.REPOSITORY,
                    GitHubSkillSearch.Mode.TOPIC,
                    -> "GitHub 找到 ${searchResults.size} 个仓库"
                    null -> "GitHub 结果"
                },
            ) {
                searchResults.forEach { entry ->
                    item(key = "gh_" + entry.id) {
                        SkillMarketRow(
                            entry = entry,
                            installingId = installingId,
                            onInstall = onInstall,
                            expanding = expanding,
                            expandedRepo = expandedRepo,
                            onExpand = onExpand,
                        )
                    }
                }
            }
        }

        val tabs = remember(catalog.tags) {
            buildList {
                add(ALL_TAG to "全部")
                catalog.tags.forEach { add(it to it) }
            }
        }
        CouiChipTabs(
            items = tabs,
            selectedKey = tag,
            onSelectedKeyChange = { tag = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        )
        Spacer(Modifier.height(4.dp))

        CouiText(
            text = if (query.isBlank()) "共 ${filtered.size} 个技能" else "搜索到 ${filtered.size} 个技能",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
            kind = CouiTextKind.GroupFooter,
            maxLines = 1,
        )

        if (filtered.isNotEmpty()) {
            CouiPreferenceGroup(title = "可用技能") {
                filtered.forEach { entry ->
                    item(key = entry.id) {
                        SkillMarketRow(
                            entry = entry,
                            installingId = installingId,
                            onInstall = onInstall,
                        )
                    }
                }
            }
        }

        when {
            loading && catalog.entries.isEmpty() -> Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                InfiniteProgressIndicator(size = 24.dp)
            }

            catalog.entries.isEmpty() -> ListEmptyState(
                title = "技能目录为空",
                summary = "内置目录未加载，请重装应用或稍后重试",
            )

            filtered.isEmpty() -> ListEmptyState(
                title = if (query.isBlank()) "该分类下暂无技能" else "没有匹配「$query」的技能",
                summary = "换个关键词或分类再试",
            )
        }
        CouiPageBottomSpacer()
    }
}

/** 技能条目行：内置目录与 GitHub 搜索结果共用。 */
@Composable
private fun SkillMarketRow(
    entry: SkillMarketEntry,
    installingId: String?,
    onInstall: (SkillMarketEntry) -> Unit,
    expanding: Boolean = false,
    expandedRepo: String? = null,
    onExpand: ((SkillMarketEntry) -> Unit)? = null,
) {
    // 仓库模式的条目只知道仓库名（path = "."），先展开成具体技能再装更可靠。
    val canExpand = onExpand != null && entry.path == "." && entry.repo.isNotBlank()
    val isExpanding = expanding && expandedRepo == entry.repo
    CouiPreference(
        title = entry.title,
        summary = if (entry.tags.isEmpty()) {
            entry.summary
        } else {
            entry.summary + "  ·  " + entry.tags.joinToString(" / ")
        },
        value = entry.author,
        enabled = installingId == null && !expanding,
        onClick = { onInstall(entry) },
        showChevron = false,
        trailing = {
            when {
                installingId == entry.id || isExpanding -> InfiniteProgressIndicator(size = 20.dp)
                installingId != null -> Unit
                else -> Row {
                    if (canExpand) {
                        CouiButton(
                            text = "展开",
                            onClick = { onExpand?.invoke(entry) },
                            role = CouiButtonRole.Secondary,
                            minHeight = 30.dp,
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    CouiButton(
                        text = "安装",
                        onClick = { onInstall(entry) },
                        role = CouiButtonRole.Primary,
                        minHeight = 30.dp,
                    )
                }
            }
        },
    )
}
