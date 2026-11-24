package io.github.mangi.eta.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.TextField

/**
 * MIUIX 样式下的设置搜索框。
 *
 * 用 Miuix 的 TextField 而不是 SearchBar：设置页只需要一个常驻输入框，
 * SearchBar 的展开/收起动效在这里反而碍事。
 */
@Composable
internal fun MiuixSettingsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        label = hint,
        singleLine = true,
    )
}
