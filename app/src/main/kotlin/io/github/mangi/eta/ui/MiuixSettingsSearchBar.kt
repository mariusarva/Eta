package io.github.mangi.eta.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.R
import io.github.mangi.eta.ui.components.rememberDraftTextFieldState
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * MIUIX 样式下的设置搜索框。
 *
 * 与 COUI 版同源的紧凑搜索条：
 * - 圆角沿用设置卡片 `CardDefaults.CornerRadius`（MIUIX 卡片同款 squircle）；
 * - 高度固定 40.dp，避免上下过宽；
 * - 左搜索图标、右清除按钮，输入态用 `BasicTextField(state)` 保留草稿同步。
 */
@Composable
internal fun MiuixSettingsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
) {
    val state = rememberDraftTextFieldState(query, onQueryChange)
    val colors = MiuixTheme.colorScheme
    val clearLabel = stringResource(R.string.page_clear_84fcd7)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .height(40.dp)
            .squircleSurface(color = colors.surfaceContainer, cornerRadius = CardDefaults.CornerRadius)
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = colors.onSurfaceVariantSummary,
        )
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            state = state,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            lineLimits = TextFieldLineLimits.SingleLine,
            textStyle = MiuixTheme.textStyles.body2.copy(color = colors.onSurface),
            decorator = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (state.text.isEmpty()) {
                        Text(
                            text = hint,
                            style = MiuixTheme.textStyles.body2,
                            color = colors.onSurfaceVariantSummary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    inner()
                }
            },
            cursorBrush = SolidColor(colors.primary),
        )
        if (state.text.isEmpty()) {
            Spacer(Modifier.width(12.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .semantics { contentDescription = clearLabel }
                    .clickable(role = Role.Button) { state.clearText() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = colors.onSurfaceVariantSummary,
                )
            }
        }
    }
}
