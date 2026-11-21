package io.github.mangi.eta.ui.components.custom

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import io.github.mangi.eta.ui.components.AnswerActionButton
import top.yukonga.miuix.kmp.basic.TooltipBox

/** Both roles share one builder, one native Compose Dialog, and the same compact row metrics. */
@Suppress("DEPRECATION")
@Composable
internal fun CustomMessageActionsHost(
    messageId: String,
    text: String,
    enabled: Boolean,
    onDelete: (() -> Unit)?,
    onEdit: (() -> Unit)?,
    onRegenerate: (() -> Unit)?,
    onBranch: (() -> Unit)?,
    content: @Composable (selectionMode: Boolean, openMenu: () -> Unit, copiedSelection: (String) -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val composer = LocalCustomComposer.current
    val speech = key(composer, messageId) { rememberCustomSpeechController(messageId, enabled) }
    val persistence = LocalCustomMessagePersistence.current
    val saveController = key(composer, messageId, text) { rememberMessageSaveActions(persistence) }
    val continueHandler = LocalCustomContinue.current
    val savePending = saveController.saving || saveController.review != null || saveController.result != null
    val continuePending = continueHandler?.let { it.saving || it.review != null } == true
    // Reset synchronously when the message becomes unavailable, not one effect/frame later.
    // Composer + message + source isolate conversation switches and regenerated content.
    var interaction by remember(composer, messageId, text, enabled) {
        mutableStateOf(CustomMessageInteractionState())
    }
    val expanded = interaction.menuOpen
    val selecting = enabled && interaction.selecting
    var selectedText by remember(composer, messageId, text, enabled) { mutableStateOf<String?>(null) }
    var menuSelectionText by remember(composer, messageId, text, enabled) { mutableStateOf<String?>(null) }
    val selectionReader = remember(composer, messageId, text, enabled) { mutableStateOf<(() -> String)?>(null) }
    // A dialog takes focus; freeze only its invocation's real selection, never the whole body.
    val actionText = if (selecting) {
        if (expanded) menuSelectionText.orEmpty() else selectedText.orEmpty()
    } else text
    fun currentActionText(): String = if (selecting) selectionReader.value?.invoke().orEmpty() else text
    val openMenu = {
        if (enabled) {
            menuSelectionText = if (selecting) selectionReader.value?.invoke() else null
            interaction = interaction.openMenu(enabled)
        }
    }
    fun notice(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    fun finishSelection() {
        interaction = CustomMessageInteractionState()
        selectedText = null
        menuSelectionText = null
        selectionReader.value = null
    }
    fun beginSelection() {
        if (enabled && text.isNotBlank()) {
            selectedText = null
            interaction = interaction.beginSelection(enabled, text.isNotBlank())
        }
    }
    // Activity Compose 1.13 resolves an owner even when enabled=false. A service overlay
    // may provide neither owner: leave Back to its existing window callback in that case.
    val backPressedOwner = LocalOnBackPressedDispatcherOwner.current
    val navigationEventOwner = LocalNavigationEventDispatcherOwner.current
    if (hasMessageBackHandlerOwner(backPressedOwner != null, navigationEventOwner != null)) {
        BackHandler(enabled = selecting && !expanded) { finishSelection() }
    }
    fun save(skill: Boolean, selected: String = currentActionText()) {
        saveController.begin(selected, skill)
    }
    fun continueMessage(selected: String = currentActionText()) {
        when {
            !enabled -> notice("消息处理中，暂不可用")
            continueHandler == null -> notice("当前无法继续对话")
            continueHandler.saving || continueHandler.review != null -> notice("请先完成当前操作")
            // A real inline selection always uses the reviewed quote path, even at a boundary.
            selecting -> {
                if (selected.isBlank()) notice("尚未选中文字")
                else continueHandler.continueInNewChat(messageId, selected)
            }
            onBranch != null -> continueHandler.branchInNewChat(messageId)
            selected.isBlank() -> notice("没有可引用的文字")
            else -> continueHandler.continueInNewChat(messageId, selected)
        }
    }
    fun share(selected: String) {
        try {
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, selected)
            }, "分享"))
        } catch (error: Exception) { notice(error.message ?: "没有可用的分享应用") }
    }
    fun speak(selected: String) {
        if (speech.state.value.messageId == messageId && speech.state.value.phase != CustomSpeechPhase.ERROR) speech.stop()
        else speech.speak(messageId, selected)
    }
    val selectionActions = CustomSelectionActions(
        bindSelectionReader = { selectionReader.value = it },
        enabled = buildSet {
            if (enabled) {
                add(CustomSelectionAction.COPY)
                add(CustomSelectionAction.SPEAK)
                if (composer != null) add(CustomSelectionAction.QUOTE)
            }
            add(CustomSelectionAction.DONE)
        },
        perform = { action, selected ->
            when {
                action == CustomSelectionAction.DONE -> finishSelection()
                !enabled -> notice("消息处理中，暂不可用")
                action == CustomSelectionAction.QUOTE && composer == null -> notice("当前没有可引用的输入框")
                selected.isBlank() -> notice("尚未选中文字")
                else -> when (action) {
                    CustomSelectionAction.COPY -> clipboard.setText(AnnotatedString(selected))
                    CustomSelectionAction.SPEAK -> speak(selected)
                    CustomSelectionAction.QUOTE -> {
                        composer?.let { it.setTextAndPlaceCursorAtEnd(quoteWithDraft(selected, it.text.toString())) }
                        finishSelection()
                    }
                    CustomSelectionAction.DONE -> Unit
                }
            }
        },
    )
    val toolbar: @Composable () -> Unit = {
        TooltipBox(text = if (savePending) "请先完成保存审核" else "保存记忆") {
            AnswerActionButton(Icons.Rounded.Memory, "保存记忆", { save(false) },
                enabled = enabled && !savePending && actionText.isNotBlank())
        }
        TooltipBox(text = if (savePending) "请先完成保存审核" else "保存技能") {
            AnswerActionButton(Icons.Rounded.RocketLaunch, "保存技能", { save(true) },
                enabled = enabled && !savePending && actionText.isNotBlank())
        }
        TooltipBox(text = "引用追问") {
            AnswerActionButton(Icons.Rounded.FormatQuote, "引用追问", {
                when {
                    !enabled -> notice("消息处理中，暂不可用")
                    composer == null -> notice("当前没有可引用的输入框")
                    text.isBlank() -> notice("没有可选取的文字")
                    else -> beginSelection()
                }
            })
        }
        TooltipBox(text = "在新对话中继续") {
            AnswerActionButton(Icons.AutoMirrored.Rounded.CallSplit, "在新对话中继续", { continueMessage() })
        }
        TooltipBox(text = "分享") {
            AnswerActionButton(Icons.Rounded.Share, "分享", {
                if (enabled && actionText.isNotBlank()) share(actionText) else notice("没有可分享的文字")
            })
        }
        // 删除这一轮：与长按菜单同一个回调（用户消息删整轮，助手消息删单条）。
        TooltipBox(text = if (onDelete == null) "当前消息不能删除" else "删除这一轮") {
            AnswerActionButton(
                Icons.Rounded.Delete,
                "删除这一轮",
                { if (onDelete == null) notice("当前消息不能删除") else onDelete.invoke() },
                enabled = enabled && onDelete != null,
            )
        }
    }
    Column {
        key(composer, messageId, text) {
            CompositionLocalProvider(
                LocalCustomSelectionActions provides selectionActions,
                LocalCustomMessageActionSlots provides toolbar,
            ) {
                content(selecting, openMenu) { selected ->
                    if (selecting) selectedText = selected.takeIf(String::isNotBlank)
                }
            }
        }
        CustomSpeechControls(speech, messageId)
    }
    // Stable host-level render, never conditional on the menu/selection state.
    MessageSaveDialogs(saveController)
    if (!expanded || !enabled) return
    val emptyReason = if (actionText.isBlank()) "没有可操作的文字" else null
    val saveReason = when {
        emptyReason != null -> emptyReason
        savePending -> "请先完成保存审核"
        persistence == null -> "当前无法保存"
        else -> null
    }
    val actions = listOf(
        MessageMenuAction("复制", Icons.Rounded.ContentCopy, emptyReason) { clipboard.setText(AnnotatedString(actionText)) },
        MessageMenuAction("选取文字", Icons.Rounded.SelectAll, if (text.isBlank()) "没有可选取的文字" else null) { beginSelection() },
        MessageMenuAction("朗读", Icons.AutoMirrored.Rounded.VolumeUp, emptyReason) { speak(actionText) },
        MessageMenuAction("分享", Icons.Rounded.Share, emptyReason) { share(actionText) },
        MessageMenuAction("删除", Icons.Rounded.Delete, if (onDelete == null) "当前消息不能删除" else null) { onDelete?.invoke() },
        MessageMenuAction("引用追问", Icons.Rounded.FormatQuote,
            if (composer == null) "当前没有可引用的输入框" else if (text.isBlank()) "没有可选取的文字" else null) { beginSelection() },
        MessageMenuAction("保存记忆", Icons.Rounded.Memory, saveReason) { save(false, actionText) },
        MessageMenuAction("保存技能", Icons.Rounded.RocketLaunch, saveReason) { save(true, actionText) },
        MessageMenuAction("在新对话中继续", Icons.AutoMirrored.Rounded.CallSplit,
            when {
                continueHandler == null -> "当前无法继续对话"
                continuePending -> "请先完成当前操作"
                selecting && actionText.isBlank() -> "尚未选中文字"
                onBranch == null && actionText.isBlank() -> "没有可引用的文字"
                else -> null
            }) { continueMessage(actionText) },
        MessageMenuAction(if (onRegenerate != null) "重新生成" else "编辑",
            if (onRegenerate != null) Icons.Rounded.Refresh else Icons.Rounded.Edit,
            if (onRegenerate == null && onEdit == null) "当前消息不能编辑" else null) { (onRegenerate ?: onEdit)?.invoke() },
    )
    CustomMessageMenuDialog(actions, onDismiss = { interaction = interaction.dismissMenu() }, onUnavailable = ::notice)
}
