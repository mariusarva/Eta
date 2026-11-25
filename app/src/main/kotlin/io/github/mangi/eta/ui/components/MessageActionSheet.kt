package io.github.mangi.eta.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.R
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 菜单项：id 用于回调分发，enabled=false 时整行置灰且不可点。 */
internal data class MessageActionItem(
    val id: Int,
    val label: String,
    val icon: ImageVector,
    val enabled: Boolean = true,
)

/**
 * 会话快捷菜单（长按消息弹出）。
 *
 * 顺序与参考实现一致：复制 / 选取文字 / 朗读 / 分享 / 删除 / 引用追问 /
 * 保存记忆 / 保存技能 / 在新对话中继续 / 编辑。
 *
 * 不用系统 DropdownMenu：那样会带阴影与最小宽度约束，和截图里的紧凑卡片不一致；
 * 这里自绘一张圆角浮层，宽度按内容收窄。
 */
@Composable
internal fun MessageActionSheet(
    items: List<MessageActionItem>,
    onSelect: (MessageActionItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .widthIn(min = 180.dp, max = 260.dp)
            .background(
                color = MiuixTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
            )
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (item.enabled) {
                            Modifier.clickable { onSelect(item) }
                        } else {
                            Modifier
                        },
                    )
                    .heightIn(min = 44.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = if (item.enabled) {
                        MiuixTheme.colorScheme.onSurface
                    } else {
                        MiuixTheme.colorScheme.onSurface.copy(alpha = 0.32f)
                    },
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = item.label,
                    style = MiuixTheme.textStyles.body2,
                    color = if (item.enabled) {
                        MiuixTheme.colorScheme.onSurface
                    } else {
                        MiuixTheme.colorScheme.onSurface.copy(alpha = 0.32f)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
