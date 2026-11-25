package io.github.mangi.eta.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.R
import io.github.mangi.eta.ui.components.coui.CouiIcon
import io.github.mangi.eta.ui.components.coui.CouiPageDefaults
import io.github.mangi.eta.ui.components.coui.CouiText
import io.github.mangi.eta.ui.components.coui.CouiTextKind
import io.github.mangi.eta.ui.components.coui.couiTextStyle
import io.github.mangi.eta.ui.components.rememberDraftTextFieldState
import io.github.suqi8.coui.kmp.theme.COUITheme

/**
 * COUI 样式下的设置搜索框。
 *
 * 设置页用的是 ColumnScope 版 `CouiScaffoldPage`，没有 `search` 参数
 * （只有 LazyListScope 版才有），所以这里自己放一个常驻输入框。
 *
 * 视觉上做成与设置卡片同源的紧凑搜索条：
 * - 圆角统一用 `CouiPageDefaults.shape`（SquircleShape 24.dp），不再用
 *   `CouiTextField` 内部 Rectangle 模式的普通 `RoundedCornerShape`；
 * - 高度固定 40.dp，比标准输入框矮，避免上下过宽；
 * - 左搜索图标、右清除按钮，输入态用 `BasicTextField(state)` 保留草稿同步。
 */
@Composable
internal fun CouiSettingsSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
) {
    val state = rememberDraftTextFieldState(query, onQueryChange)
    val colors = CouiPageDefaults.themeColors()
    val hintColor = COUITheme.colorScheme.hintNeutral
    val clearLabel = stringResource(R.string.page_clear_84fcd7)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CouiPageDefaults.horizontalMargin, vertical = 2.dp)
            .height(40.dp)
            .clip(CouiPageDefaults.shape)
            .background(colors.card),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(12.dp))
        CouiIcon(
            painter = rememberVectorPainter(Icons.Rounded.Search),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = colors.summary,
        )
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            state = state,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            lineLimits = TextFieldLineLimits.SingleLine,
            textStyle = couiTextStyle(CouiTextKind.ItemTitle).copy(color = colors.title),
            cursorBrush = SolidColor(colors.accent),
            decorator = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (state.text.isEmpty()) {
                        CouiText(
                            text = hint,
                            kind = CouiTextKind.ItemTitle,
                            color = hintColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    inner()
                }
            },
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
                CouiIcon(
                    painter = rememberVectorPainter(Icons.Rounded.Close),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = colors.summary,
                )
            }
        }
    }
}
