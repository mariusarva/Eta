package io.github.mangi.eta.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.mangi.eta.ui.components.SettingsTextField
import io.github.mangi.eta.ui.components.coui.CouiPageBottomSpacer
import io.github.mangi.eta.ui.components.coui.CouiPageDefaults
import io.github.mangi.eta.ui.components.coui.CouiPreference
import io.github.mangi.eta.ui.components.coui.CouiPreferenceGroup
import io.github.mangi.eta.ui.components.coui.CouiScaffoldPage
import io.github.mangi.eta.ui.components.coui.CouiSettingsIcon
import io.github.mangi.eta.ui.model.ConversationCategoryUi
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val CategoryIconColor = Color(0xFF4285F4)

/**
 * 会话分类管理：独立整页，不再用弹窗。
 *
 * 侧栏只有 200dp 出头，弹窗里排「名称 + 上移 + 下移 + 改名 + 删除」这一行本来就挤，
 * 再加上遮罩，操作起来比整页更别扭。
 */
@Composable
internal fun ConversationCategoriesScreen(
    categories: List<ConversationCategoryUi>,
    onBack: () -> Unit,
    onCreate: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onMove: (String, Int) -> Unit,
) {
    val scrollState = rememberScrollState()
    var creating by remember { mutableStateOf(false) }
    var renamingId by remember { mutableStateOf<String?>(null) }
    var renamingName by remember { mutableStateOf("") }

    CouiScaffoldPage(title = "会话分类", onBack = onBack, scrollState = scrollState) {
        CouiPreferenceGroup(title = "新建") {
            item {
                CouiPreference(
                    title = "新建分类",
                    summary = "分类只决定侧栏里的归属，不改变会话内容。",
                    icon = { CouiSettingsIcon(Icons.Rounded.Add, CategoryIconColor) },
                    onClick = { creating = true },
                )
            }
        }

        if (categories.isEmpty()) {
            CouiPreferenceGroup {
                item {
                    CouiPreference(
                        title = "还没有分类",
                        summary = "点上面的「新建分类」开始。",
                        showChevron = false,
                    )
                }
            }
        }

        categories.forEachIndexed { index, category ->
            CouiPreferenceGroup(title = if (index == 0) "全部分类" else null) {
                item {
                    CouiPreference(
                        title = category.name,
                        summary = "${category.conversationCount} 个会话",
                        icon = { CouiSettingsIcon(Icons.Outlined.Folder, CategoryIconColor) },
                        showChevron = false,
                        trailing = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                IconAction(
                                    enabled = index > 0,
                                    description = "上移",
                                    onClick = { onMove(category.id, -1) },
                                ) { tint ->
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowUp,
                                        contentDescription = "上移",
                                        modifier = Modifier.size(18.dp),
                                        tint = tint,
                                    )
                                }
                                IconAction(
                                    enabled = index < categories.lastIndex,
                                    description = "下移",
                                    onClick = { onMove(category.id, 1) },
                                ) { tint ->
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "下移",
                                        modifier = Modifier.size(18.dp),
                                        tint = tint,
                                    )
                                }
                                IconAction(
                                    enabled = true,
                                    description = "改名",
                                    onClick = {
                                        renamingId = category.id
                                        renamingName = category.name
                                    },
                                ) { tint ->
                                    Text(
                                        text = "改名",
                                        style = MiuixTheme.textStyles.footnote1.copy(fontSize = 12.sp),
                                        color = tint,
                                    )
                                }
                                IconAction(
                                    enabled = true,
                                    description = "删除",
                                    onClick = { onDelete(category.id) },
                                ) { tint ->
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "删除",
                                        modifier = Modifier.size(17.dp),
                                        tint = tint,
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }

        CouiPageBottomSpacer()
    }

    if (creating) {
        CategoryNameDialog(
            title = "新建分类",
            initial = "",
            onDismiss = { creating = false },
            onConfirm = { name ->
                onCreate(name)
                creating = false
            },
        )
    }
    renamingId?.let { id ->
        CategoryNameDialog(
            title = "改名",
            initial = renamingName,
            onDismiss = { renamingId = null },
            onConfirm = { name ->
                onRename(id, name)
                renamingId = null
            },
        )
    }
}

/** 分类行右侧的小动作位：统一 30dp 命中，禁用时淡化。 */
@Composable
private fun IconAction(
    enabled: Boolean,
    description: String,
    onClick: () -> Unit,
    content: @Composable (Color) -> Unit,
) {
    val colors = CouiPageDefaults.themeColors()
    val tint = if (enabled) colors.accent else colors.disabled
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content(tint)
    }
}

@Composable
private fun CategoryNameDialog(
    title: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val colors = CouiPageDefaults.themeColors()
    var value by remember { mutableStateOf(initial) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.card)
                .padding(18.dp),
        ) {
            Text(
                text = title,
                style = MiuixTheme.textStyles.title4,
                color = colors.title,
            )
            Column(modifier = Modifier.padding(top = 12.dp)) {
                SettingsTextField(
                    label = "分类名称",
                    value = value,
                    onChange = { value = it },
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DialogTextButton(text = "取消", enabled = true, onClick = onDismiss)
                DialogTextButton(
                    text = "保存",
                    enabled = value.isNotBlank(),
                    onClick = { onConfirm(value.trim()) },
                )
            }
        }
    }
}

@Composable
private fun DialogTextButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = CouiPageDefaults.themeColors()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.button,
            color = if (enabled) colors.accent else colors.disabled,
        )
    }
}
