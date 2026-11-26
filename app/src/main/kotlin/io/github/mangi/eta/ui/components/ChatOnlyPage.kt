package io.github.mangi.eta.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mangi.eta.R
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 对话页三页横向 Pager 的页序。
 *
 * 顺序即物理顺序：左 = 工作文件夹，中 = 对话，右 = 纯聊天。
 * 用 enum 而不是裸 index，避免后续插入页面时索引错位。
 */
internal enum class ChatPage {
    WORKSPACE,
    CONVERSATION,
    CHAT_ONLY,
}

/**
 * 右页：纯聊天入口。
 *
 * 这里不是纯聊天会话本身，而是它的入口面板——
 * 点「开始纯聊天」会新建一条 chatOnly 会话并跳回中间页，
 * 之后在中间页正常打字即可，工具表为空。
 *
 * 之所以不做成内嵌的第二套消息流：纯聊天会话和其他会话共用同一份
 * 消息状态机（AgentAppState.homeState），同时渲染两份会互相覆盖。
 */
@Composable
internal fun ChatOnlyPage(
    onStartChatOnly: () -> Unit,
    onGoToConversation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Forum,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MiuixTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.action_chat_only_conversation),
                style = MiuixTheme.textStyles.title2.copy(fontSize = 20.sp),
                color = MiuixTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "本轮不向模型声明任何工具，只做文本对话。适合闲聊、写作、答疑——不会调用手机能力，也不会读你的数据。",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp,
            )
            Spacer(Modifier.height(28.dp))
            TextButton(
                text = "开始纯聊天",
                onClick = onStartChatOnly,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            TextButton(
                text = "回到当前对话",
                onClick = onGoToConversation,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "左滑工作文件夹 · 右滑纯聊天",
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
