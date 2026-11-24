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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.R
import io.github.mangi.eta.agent.skill.market.SkillMarketCatalog
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
) {
    var tag by rememberSaveable { mutableStateOf(ALL_TAG) }
    var query by rememberSaveable { mutableStateOf("") }
    val searchState = rememberDraftTextFieldState(query) { query = it }
    val filtered = remember(catalog.entries, query, tag) { catalog.entries.matching(query, tag) }

    CouiNoticeDialog(
        visible = notice != null,
        title = notice?.title,
        message = notice?.message,
        confirmText = stringResource(R.string.ui_knew_cb63c6),
        onDismissRequest = onDismissNotice,
    )

    CouiScaffoldPage(
        title = stringResource(R.string.route_skill_market),
        onBack = onBack,
        modifier = modifier,
    ) {
        CouiTextField(
            state = searchState,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            placeholder = "搜索技能名或说明",
            singleLine = true,
        )

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
                        CouiPreference(
                            title = entry.title,
                            summary = if (entry.tags.isEmpty()) {
                                entry.summary
                            } else {
                                entry.summary + "  ·  " + entry.tags.joinToString(" / ")
                            },
                            value = entry.author,
                            enabled = installingId == null,
                            onClick = { onInstall(entry) },
                            showChevron = false,
                            trailing = {
                                when {
                                    installingId == entry.id -> InfiniteProgressIndicator(size = 20.dp)
                                    installingId != null -> Unit
                                    else -> Row {
                                        Spacer(Modifier.width(4.dp))
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
