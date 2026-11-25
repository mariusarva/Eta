package io.github.mangi.eta.ui.components.custom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.contextmenu.builder.item
import androidx.compose.foundation.text.contextmenu.modifier.appendTextContextMenuComponents
import androidx.compose.foundation.text.contextmenu.modifier.filterTextContextMenuComponents
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.rememberSelectionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow

/** Per-conversation composer, never a global cached input. */
internal val LocalCustomComposer = compositionLocalOf<TextFieldState?> { null }

internal enum class CustomSelectionAction(val label: String) {
    COPY("复制"), SPEAK("朗读"), QUOTE("追问"), DONE("完成"),
}

internal data class CustomSelectionActions(
    val enabled: Set<CustomSelectionAction>,
    val perform: (CustomSelectionAction, String) -> Unit,
    val bindSelectionReader: (((() -> String)?) -> Unit)? = null,
)

internal val LocalCustomSelectionActions = compositionLocalOf<CustomSelectionActions?> { null }

/** Live public SelectionState, one inline container, and exactly four native context actions. */
@Composable
internal fun CustomMessageSelection(
    explicitSelection: Boolean,
    onCopiedSelection: (String) -> Unit,
    content: @Composable () -> Unit,
) {
    if (!explicitSelection) {
        // Null the public selection registrar: ordinary text must not join an ancestor.
        // This does not intercept pointers; combinedClickable and list dragging stay native.
        DisableSelection { content() }
        return
    }
    val selection = rememberSelectionState()
    val laidOut = androidx.compose.runtime.remember(selection) { MutableStateFlow(false) }
    val currentSelectionCallback = rememberUpdatedState(onCopiedSelection)
    val currentActions = rememberUpdatedState(LocalCustomSelectionActions.current)
    LaunchedEffect(selection) {
        // Wait for this actual body to register its selectable text, not a second long press.
        laidOut.first { it }
        withFrameNanos { }
        selection.selectAll()
    }
    LaunchedEffect(selection) {
        snapshotFlow { selection.selectedTexts.joinToString("\n") { it.text } }
            .collect { currentSelectionCallback.value(it) }
    }
    DisposableEffect(selection) {
        currentActions.value?.bindSelectionReader?.invoke {
            selection.selectedTexts.joinToString("\n") { it.text }
        }
        onDispose {
            currentActions.value?.bindSelectionReader?.invoke(null)
            selection.clear()
        }
    }
    SelectionContainer(
        state = selection,
        modifier = Modifier.onGloballyPositioned { laidOut.value = true }
            .appendTextContextMenuComponents {
                CustomSelectionAction.entries.forEach { action ->
                    item(key = action, label = action.label) {
                        // Capture the real current substring before releasing toolbar focus.
                        val selected = selection.selectedTexts.joinToString("\n") { it.text }
                        currentActions.value?.perform?.invoke(action, selected)
                        close()
                    }
                }
            }
            // Remove native Copy/Select-all/process-text as well: no duplicates or overflow list.
            .filterTextContextMenuComponents { it.key is CustomSelectionAction },
    ) {
        Column { content() }
    }
}
