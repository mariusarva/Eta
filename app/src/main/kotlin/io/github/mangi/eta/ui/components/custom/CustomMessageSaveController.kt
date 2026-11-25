package io.github.mangi.eta.ui.components.custom

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Owns a click-time snapshot, never a live selection callback or model-rewritten content. */
internal data class MessageSaveReview(
    val originalText: String,
    val text: String,
    val skill: Boolean,
    val error: String? = null,
)

internal data class MessageSaveResult(val message: String, val error: Boolean = false)

@Stable
internal class MessageSaveController internal constructor(
    private val context: Context,
    private val scope: CoroutineScope,
    private val persistence: CustomMessagePersistence?,
) {
    var review: MessageSaveReview? by mutableStateOf(null)
        private set
    var result: MessageSaveResult? by mutableStateOf(null)
        private set
    var saving: Boolean by mutableStateOf(false)
        private set

    /** UI entry point for either the complete message or the exact selected substring. */
    fun begin(text: String, skill: Boolean = false) {
        if (saving) return
        result = null
        review = MessageSaveReview(originalText = text, text = text, skill = skill)
    }

    fun edit(text: String) {
        if (!saving) review = review?.copy(text = text, error = null)
    }

    fun dismiss() {
        if (saving) return
        review = null
        result = null
    }

    fun confirm() {
        if (saving) return
        val snapshot = review ?: return
        val validation = when {
            persistence == null -> "当前页面不支持保存"
            snapshot.text.isBlank() -> "内容不能为空；未保存"
            else -> null
        }
        if (validation != null) {
            review = snapshot.copy(error = validation)
            return
        }
        val writer = persistence ?: return
        saving = true
        // Everything needed by the job is frozen before dispatch, including the edited body.
        scope.launch {
            try {
                val message = withContext(Dispatchers.IO) {
                    if (snapshot.skill) writer.saveSkill(context, snapshot.text)
                    else writer.saveMemory(context, snapshot.text)
                }
                review = null
                result = MessageSaveResult(message)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                // Retain the reviewed text so retry always requires another explicit confirm.
                review = snapshot
                result = MessageSaveResult(
                    "保存失败或结果尚未确认（可能已写入），请先检查后重试。\n" +
                        (error.message ?: error.javaClass.simpleName),
                    error = true,
                )
            } finally {
                saving = false
            }
        }
    }

    /** Error OK returns to the unchanged editor rather than silently retrying an append. */
    fun dismissResult() {
        if (!saving) result = null
    }
}

/** Call once in the stable message-actions host, outside menu/selection conditional content. */
@Composable
internal fun rememberMessageSaveActions(
    persistence: CustomMessagePersistence? = LocalCustomMessagePersistence.current,
): MessageSaveController {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    return remember(context, scope, persistence) { MessageSaveController(context, scope, persistence) }
}
