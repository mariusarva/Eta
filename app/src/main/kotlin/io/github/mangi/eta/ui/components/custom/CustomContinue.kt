package io.github.mangi.eta.ui.components.custom

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import io.github.mangi.eta.ui.app.AgentAppState
import io.github.mangi.eta.ui.app.AgentAppViewModel
import io.github.mangi.eta.ui.model.AgentChatMessageUi
import io.github.mangi.eta.ui.model.AgentMessageUi
import io.github.mangi.eta.ui.model.UserMessageUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Quote fallback only: eligible assistant boundaries keep the official onBranch action. */
internal val LocalCustomContinue = compositionLocalOf<CustomContinueHandler?> { null }

internal data class CustomContinueReview(
    val sourceId: String,
    val selectedText: String,
    val sourceRole: String,
    val conversationId: String,
    val source: AgentChatMessageUi,
    val originalDraft: String,
    val onReady: (() -> Unit)?,
    val error: String? = null,
)

/** Bound to one real state owner AND the original composer; never a cached global handler. */
internal class CustomContinueHandler(
    private val state: AgentAppState,
    private val composer: TextFieldState,
    private val scope: CoroutineScope,
) {
    var review by mutableStateOf<CustomContinueReview?>(null)
        private set
    var saving by mutableStateOf(false)
        private set
    private var attached = true

    fun continueInNewChat(messageId: String, text: String, onReady: (() -> Unit)? = null) {
        if (!attached || saving || text.isBlank()) return
        val conversationId = state.conversationPaneState.selectedConversationId ?: return
        val source = state.homeState.messages.singleOrNull { it.id == messageId } ?: return
        val role = when (source) {
            is UserMessageUi -> "用户"
            is AgentMessageUi -> "助手"
            else -> return
        }
        review = CustomContinueReview(messageId, text, role, conversationId, source, composer.text.toString(), onReady)
    }

    /** Native path only: retain official branch validation/transaction while passing LIVE draft.
     * This dispatches the existing asynchronous branch; it does not report target-ready success.
     */
    fun branchInNewChat(messageId: String) {
        if (!attached || saving) return
        val conversationId = state.conversationPaneState.selectedConversationId ?: return
        if (state.homeState.messages.none { it.id == messageId && it is AgentMessageUi }) return
        state.branchConversation(conversationId, messageId, draftText = composer.text.toString())
    }

    fun dismiss() { if (!saving) review = null }
    fun detach() { attached = false }

    fun confirm() {
        val request = review ?: return
        if (!attached || saving) return
        val draft = request.originalDraft
        saving = true
        // Use the state owner's scope: creating the destination destroys this composition, not
        // the save transaction. No old-composer edits occur before or after this operation.
        scope.launch {
            try {
                val ready = state.continueFromQuote(
                    sourceId = request.sourceId,
                    selectedText = request.selectedText,
                    draft = draft,
                    expectedConversationId = request.conversationId,
                    sourceStillCurrent = {
                        attached && composer.text.toString() == draft &&
                            state.homeState.messages.any { it === request.source }
                    },
                )
                if (ready) {
                    review = null
                    request.onReady?.invoke()
                } else {
                    review = request.copy(error = "未切换：保存失败或原会话/草稿已变化。原会话和输入框仍保留，请检查后重试。")
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                review = request.copy(error = "未确认完成，请检查会话列表；不会自动重试或发送。")
            } finally {
                saving = false
            }
        }
    }
}

/** Root obtains its VM before NavDisplay, so resolve the SAME Activity store, not a route store. */
@Composable
internal fun rememberCustomContinue(composer: TextFieldState): CustomContinueHandler? {
    val context = LocalContext.current
    val activity = remember(context) { context.componentActivity() } ?: return null
    val owner = remember(activity) { ViewModelProvider(activity)[AgentAppViewModel::class.java] }
    val handler = remember(owner, composer) { CustomContinueHandler(owner.state, composer, owner.continueScope) }
    DisposableEffect(handler) { onDispose { handler.detach() } }
    return handler
}

private fun Context.componentActivity(): ComponentActivity? {
    var current: Context = this
    val seen = mutableSetOf<Context>()
    while (seen.add(current)) {
        if (current is ComponentActivity) return current
        current = (current as? ContextWrapper)?.baseContext ?: return null
    }
    return null
}
