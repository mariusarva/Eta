package io.github.mangi.eta.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.ui.app.ConversationTimeLabels
import io.github.mangi.eta.ui.model.AgentMessageUi
import io.github.mangi.eta.ui.model.SystemNoticeMessageUi
import io.github.mangi.eta.ui.model.AgentChatMessageUi
import io.github.mangi.eta.ui.model.UserMessageUi
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 一轮对话的索引项。
 *
 * [firstMessageId] 是该轮用户消息的 id，用于滚动定位；
 * [index] 从 1 开始，供界面显示「第 N 轮」。
 */
internal data class ChatTurnEntry(
    val index: Int,
    val firstMessageId: String,
    val preview: String,
    /** 该轮用户消息的落库时间；0 表示旧数据没有时间。 */
    val timestampMillis: Long = 0L,
)

/** 把消息列表切成轮次：每条用户消息开启新一轮，之前的消息归属上一轮。 */
internal fun List<AgentChatMessageUi>.toChatTurns(): List<ChatTurnEntry> {
    val turns = mutableListOf<ChatTurnEntry>()
    var currentUser: UserMessageUi? = null
    var preview = StringBuilder()

    fun flush() {
        val user = currentUser ?: return
        val text = preview.toString().trim()
        turns += ChatTurnEntry(
            index = turns.size + 1,
            firstMessageId = user.id,
            preview = text.ifBlank { user.content.trim() },
            timestampMillis = user.createdAtMillis,
        )
        preview = StringBuilder()
    }

    forEach { message ->
        if (message is UserMessageUi) {
            flush()
            currentUser = message
        } else if (currentUser != null) {
            if (preview.length < 60) {
                val body = when (message) {
                    is AgentMessageUi -> message.content
                    is SystemNoticeMessageUi -> message.detail.orEmpty()
                    else -> ""
                }.trim()
                if (body.isNotEmpty()) {
                    if (preview.isNotEmpty()) preview.append(' ')
                    preview.append(body.take(60))
                }
            }
        }
    }
    flush()
    return turns
}

/**
 * 会话轮次导航：底部一条小横条，上拉展开当前会话每一轮的列表。
 *
 * 点击任一轮滚到该轮用户消息；横条本身不占内容区点击范围。
 */
@Composable
internal fun ChatTurnNavigator(
    messages: List<AgentChatMessageUi>,
    onJumpToMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    allTurns: List<ChatTurnEntry> = emptyList(),
) {
    // 全库轮次优先：消息是分页加载的（128 条/页），只按已加载消息切轮次会让长会话少算前面的轮。
    val turns = remember(messages, allTurns) {
        if (allTurns.isNotEmpty()) allTurns else messages.toChatTurns()
    }
    var expanded by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    // 消息太少（只有一轮）时不显示，避免占位。
    if (turns.size < 2) return

    LaunchedEffect(expanded) {
        if (expanded && turns.isNotEmpty()) {
            listState.scrollToItem(turns.lastIndex)
        }
    }

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        // 展开后的轮次面板
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(140)) + slideInVertically(tween(200)) { it / 3 },
            exit = fadeOut(tween(120)) + slideOutVertically(tween(160)) { it / 3 },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                    .navigationBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "对话轮次",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${turns.size} 轮",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    )
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    itemsIndexed(turns, key = { _, item -> item.firstMessageId }) { _, turn ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    onClickLabel = "跳到第 ${turn.index} 轮",
                                ) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onJumpToMessage(turn.firstMessageId)
                                    expanded = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = turn.index.toString(),
                                    style = MiuixTheme.textStyles.footnote1,
                                    color = MiuixTheme.colorScheme.primary,
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            // 时间放标题下面一行：同行会挤压标题宽度，长标题直接被截断。
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = turn.preview.ifBlank { "（无内容）" },
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (turn.timestampMillis > 0L) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = remember(turn.timestampMillis) {
                                            ConversationTimeLabels.rowLabel(turn.timestampMillis)
                                        },
                                        style = MiuixTheme.textStyles.footnote1,
                                        color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.size(4.dp))
            }
        }

        // 底部小横条：向上拖或轻点展开/收起
        Box(
            modifier = Modifier
                .width(72.dp)
                .heightIn(min = 20.dp)
                .semantics { contentDescription = if (expanded) "收起对话轮次" else "展开对话轮次" }
                .pointerInput(expanded) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        if (dragAmount < -6f && !expanded) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            expanded = true
                        } else if (dragAmount > 6f && expanded) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            expanded = false
                        }
                    }
                }
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClickLabel = if (expanded) "收起对话轮次" else "展开对话轮次",
                ) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    expanded = !expanded
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MiuixTheme.colorScheme.onSurface.copy(alpha = 0.28f)),
            )
        }
    }
}
