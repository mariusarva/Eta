package io.github.mangi.eta.ui.components

import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.ui.semantics.Role
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * 单选 / 多选标识。
 *
 * 之前两种模式的差别只体现在「有没有确认按钮」和「选项右侧有没有勾」，
 * 用户点到一半才发现规则不同。这里在标题旁直接标出来。
 */
@Composable
private fun AskModeBadge(multiple: Boolean) {
    Text(
        text = if (multiple) "多选" else "单选",
        style = MiuixTheme.textStyles.footnote2,
        color = if (multiple) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (multiple) {
                    MiuixTheme.colorScheme.primary.copy(alpha = 0.14f)
                } else {
                    MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.12f)
                },
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

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
    /** 自定义回答：用户自己写的答案；第二个参数是同时勾中的选项（补充而非覆盖）。 */
    onChooseFreeform: (String, List<String>) -> Unit = { _, _ -> },
    /** 正在卡内输入：告诉宿主收起底部聊天输入框，避免两个输入框同时在场。 */
    onEditingChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // 换一张卡就重置勾选，避免把上一题的答案带过来。
    var picked by remember(ask.token) { mutableStateOf(emptySet<String>()) }
    var freeformOpen by remember(ask.token) { mutableStateOf(false) }
    // 答完自动折叠：只留问题 + 答案摘要一行，点一下可展开回看完整选项。
    // 之前答完仍是完整卡片，多轮对话后越积越长，滚动时要划很久。
    var expandedAfterAnswer by remember(ask.token) { mutableStateOf(false) }
    var freeformText by remember(ask.token) { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val multiple = ask.allowMultiple
    // 临时诊断：确认 UI 侧读到的模式与数据一致。
    android.util.Log.i(
        "EtaAskCard",
        "render token=${ask.token} allowMultiple=${ask.allowMultiple} multiple=$multiple " +
            "options=${ask.options.size} answered=${ask.answer != null}",
    )

    LaunchedEffect(freeformOpen) {
        onEditingChanged(freeformOpen)
        if (freeformOpen) runCatching { focusRequester.requestFocus() }
    }
    // 卡片被移出组合时没机会补发 false，底部输入框会永久消失（参考分支踩过）。
    DisposableEffect(Unit) {
        onDispose { onEditingChanged(false) }
    }

    // 已作答且已展开：顶部给一个显式折叠按钮。
    // 之前展开后没有任何收起的入口，只能等页面重进才恢复折叠（实测反馈）。
    if (ask.answer != null && expandedAfterAnswer) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(role = Role.Button, onClickLabel = "收起这张卡") {
                        expandedAfterAnswer = false
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "收起",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }

    if (ask.answer != null && !expandedAfterAnswer) {
        // 折叠态：一行答案摘要，点击展开完整卡片。
        val summary = if (ask.answer.skipped) {
            "已跳过"
        } else {
            "已选择：" + ask.answer.labels.joinToString("、")
        }
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = opaqueThemedSurface(),
                    shape = RoundedCornerShape(18.dp),
                )
                .border(
                    width = 0.5.dp,
                    color = MiuixTheme.colorScheme.outline.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(18.dp),
                )
                .clickable { expandedAfterAnswer = true }
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ask.question,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(6.dp))
                AskModeBadge(multiple = ask.allowMultiple)
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = summary,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        return
    }

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
        // 标题 + 单选/多选标识：用户点之前就知道能选几个。
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = ask.question,
                style = MiuixTheme.textStyles.body2,
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(Modifier.width(6.dp))
            AskModeBadge(multiple = multiple)
        }
        if (ask.body.isNotBlank()) {
            Text(
                text = ask.body,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (freeformOpen) {
            // 输入态：选项全部收起，屏上只留这一个可写的地方。
            // 之前选项和输入框同时在场，会和底部聊天输入框叠成两个可写区域，用户不知道该往哪儿写。
            val pickedLabels = ask.options.filter { it.id in picked }.map { it.label }
            Text(
                text = if (pickedLabels.isEmpty()) {
                    "在这里写你的答案，发送后这张卡就定稿。"
                } else {
                    "已选：${pickedLabels.joinToString("、")}。下面是补充，发送时一起交上去。"
                },
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = opaqueThemedSurface(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                BasicTextField(
                    value = freeformText,
                    onValueChange = { freeformText = it.take(500) },
                    textStyle = TextStyle(
                        color = MiuixTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    decorationBox = { inner ->
                        if (freeformText.isEmpty()) {
                            Text(
                                text = "写你要的答案…",
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                        inner()
                    },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    text = "返回选项",
                    onClick = {
                        freeformOpen = false
                        freeformText = ""
                    },
                    minWidth = 0.dp,
                    minHeight = 34.dp,
                    insideMargin = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.textButtonColors(),
                )
                TextButton(
                    text = "发送",
                    onClick = {
                        val answer = freeformText.trim()
                        if (answer.isNotEmpty()) {
                            onChooseFreeform(answer, ask.options.map { it.id }.filter { it in picked })
                        }
                    },
                    minWidth = 0.dp,
                    minHeight = 34.dp,
                    insideMargin = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
            return@Column
        }
        Spacer(Modifier.height(8.dp))
        // 已作答：把当时的选择固定显示出来，用户回滚时能看清自己选了什么。
        val answered = ask.answer != null
        val answeredIds = ask.answer?.labels.orEmpty().toSet()
        ask.options.forEach { option ->
            val selected = option.id in picked || option.label in answeredIds
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
                    .clickable(enabled = !answered) {
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
        if (answered) {
            val resultText = if (ask.answer?.skipped == true) {
                "已跳过"
            } else {
                "已选择：" + ask.answer?.labels.orEmpty().joinToString("、")
            }
            Text(
                text = resultText,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(top = 6.dp),
            )
            return@Column
        }
        // 「其他…」：切换成输入态，不是并排再放一个输入框。
        if (!answered) {
            Spacer(Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = opaqueThemedSurface(MiuixTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clickable { freeformOpen = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text(
                    text = "其他…",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        // 底部动作行：多选时是「跳过 + 确认」，单选时只有「跳过」。
        // 跳过不产生任何回答消息，只把卡片标成已跳过——用户不想选时不该被迫二选一。
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 跳过：轻量文字按钮，不带实心底色——它是否定动作，不该和「确认」抢视觉权重。
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(role = Role.Button, onClickLabel = "跳过这个问题", onClick = onSkip)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "跳过",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            if (multiple) {
                Spacer(Modifier.width(4.dp))
                TextButton(
                    text = if (picked.isEmpty()) "确认" else "确认（${picked.size} 项）",
                    // 按展示顺序提交，而不是 Set 的迭代顺序，否则回灌给模型的选项顺序会乱。
                    onClick = { if (picked.isNotEmpty()) onChoose(ask.options.map { it.id }.filter { it in picked }) },
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
