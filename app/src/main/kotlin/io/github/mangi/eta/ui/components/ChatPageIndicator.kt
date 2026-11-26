package io.github.mangi.eta.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.MapsUgc
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 三页 Pager 的顶部图标指示器。
 *
 * 三个图标分别对应工作文件夹 / 对话 / 纯聊天。左右滑动时高亮跟着走，
 * 点图标也能直接跳页——滑动不是唯一入口，长距离翻页点比划更快。
 *
 * 放在标题与模型选择器下方，不占独立一行高度：图标本身只有 20dp，
 * 与标题区共用垂直间距。
 */
@Composable
internal fun ChatPageIndicator(
    currentPage: Int,
    pageCount: Int,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 复用项目里已有的图标，不引入新资源：
    // Folder 用于工作文件夹（WorkspaceScreen 的目录图标）、
    // MapsUgc 与 Forum 分别是顶栏菜单里「新建会话」与「纯聊天对话」的图标。
    val icons = listOf(
        Icons.Rounded.Folder to "工作文件夹",
        Icons.Rounded.MapsUgc to "对话",
        Icons.Rounded.Forum to "纯聊天",
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icons.forEachIndexed { index, (icon, label) ->
            val active = index == currentPage
            val tint = if (active) {
                MiuixTheme.colorScheme.primary
            } else {
                MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.55f)
            }
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .size(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(role = Role.Tab) { onSelectPage(index) },
            )
        }
    }
}
