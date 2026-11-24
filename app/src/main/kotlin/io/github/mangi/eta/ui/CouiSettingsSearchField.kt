package io.github.mangi.eta.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.ui.components.coui.CouiTextField
import io.github.mangi.eta.ui.components.rememberDraftTextFieldState

/**
 * COUI 样式下的设置搜索框。
 *
 * 设置页用的是 ColumnScope 版 `CouiScaffoldPage`，没有 `search` 参数
 * （只有 LazyListScope 版才有），所以这里自己放一个常驻输入框。
 *
 * `CouiTextField` 只接 `TextFieldState`，用项目里的 `rememberDraftTextFieldState`
 * 把编辑内容回写到外部的 String 状态。
 */
@Composable
internal fun CouiSettingsSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
) {
    val state = rememberDraftTextFieldState(query, onQueryChange)
    CouiTextField(
        state = state,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        placeholder = hint,
        singleLine = true,
    )
}
