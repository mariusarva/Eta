package io.github.mangi.eta.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.agent.ask.PendingAsk
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 会话内的提问卡：模型通过 `ask_user` 提出问题，这里渲染成可点选的选项。
 *
 * 单选：点一下立即提交，不需要再按确认。
 * 多选：勾选若干后按「确认（N 项）」一次性提交。
 */
@Composable
internal fun AskUserCard(
    ask: PendingAsk,
    onChoose: (List<String>) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 换一张卡就重置勾选，避免把上一题的答案带过来。
    var picked by remember(ask.token) { mutableStateOf(emptySet<String>()) }
    val multiple = ask.allowMultiple

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                // 必须用不透明合成色：Miuix 的 surfaceContainer 系列在深色下带 alpha，
                // 直接当底色会透出后面的聊天正文（实测卡片与选项糊成一片灰）。
                color = opaqueThemedSurface(),
                shape = RoundedCornerShape(18.dp),
            )
            .border(
                width = 0.5.dp,
                color = MiuixTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(18.dp),
            )
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 10.dp),
    ) {
        Text(
            text = ask.question,
            style = MiuixTheme.textStyles.body2,
            fontWeight = FontWeight.Medium,
            color = MiuixTheme.colorScheme.onSurface,
        )
        if (ask.body.isNotBlank()) {
            Text(
                text = ask.body,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        ask.options.forEach { option ->
            val selected = option.id in picked
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .background(
                        // 选项底同样必须不透明；未选中时取 surfaceContainer 合成到卡片上，
                        // 与卡片形成一档层次，选中时叠主题色。
                        color = if (selected) {
                            MiuixTheme.colorScheme.primary.copy(alpha = 0.22f)
                                .compositeOver(opaqueThemedSurface())
                        } else {
                            opaqueThemedSurface(MiuixTheme.colorScheme.surfaceContainer)
                        },
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clickable {
                        if (multiple) {
                            picked = if (selected) picked - option.id else picked + option.id
                        } else {
                            onChoose(listOf(option.id))
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = option.label,
                        style = MiuixTheme.textStyles.body2,
                        color = if (selected) {
                            MiuixTheme.colorScheme.primary
                        } else {
                            MiuixTheme.colorScheme.onSurface
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (option.description.isNotBlank()) {
                        Text(
                            text = option.description,
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (selected) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MiuixTheme.colorScheme.primary,
                    )
                }
            }
        }
        // 底部动作行：多选时是「跳过 + 确认」，单选时只有「跳过」。
        // 跳过不产生任何回答消息，只收起卡片——用户不想选时不该被迫二选一。
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                text = "跳过",
                onClick = onSkip,
                minWidth = 0.dp,
                minHeight = 34.dp,
                insideMargin = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.textButtonColors(),
            )
            if (multiple) {
                Spacer(Modifier.width(4.dp))
                TextButton(
                    text = if (picked.isEmpty()) "确认" else "确认（${picked.size} 项）",
                    onClick = { if (picked.isNotEmpty()) onChoose(picked.toList()) },
                    enabled = picked.isNotEmpty(),
                    minWidth = 0.dp,
                    minHeight = 34.dp,
                    insideMargin = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}
