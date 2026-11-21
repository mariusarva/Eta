package io.github.mangi.eta.ui.app

import io.github.mangi.eta.agent.model.AgentModelClient
import io.github.mangi.eta.ui.model.AgentChatMessageUi
import io.github.mangi.eta.ui.model.AgentChatUiState
import io.github.mangi.eta.ui.model.UserMessageUi

/** 以用户轮次为边界同步裁剪展示消息与模型上下文。 */
internal object AgentConversationRevisionReducer {
    data class Boundary(
        val userMessage: UserMessageUi,
        val userMessageIndex: Int,
        val historyPrefix: List<AgentModelClient.ConversationMessage>,
        val journalPrefix: List<AgentModelClient.ConversationMessage> = historyPrefix,
        val laterTurnCount: Int,
        val contextWasCompacted: Boolean,
    )

    fun boundary(state: AgentChatUiState, targetMessageId: String): Boundary? {
        val targetIndex = state.messages.indexOfFirst { it.id == targetMessageId }
        if (targetIndex < 0) return null
        val userMessageIndex = (targetIndex downTo 0).firstOrNull { index ->
            state.messages[index] is UserMessageUi
        } ?: return null
        val userMessage = state.messages[userMessageIndex] as UserMessageUi
        val userMessageIndices = state.messages.indices.filter { state.messages[it] is UserMessageUi }
        val targetUserOrdinal = userMessageIndices.indexOf(userMessageIndex)
        if (targetUserOrdinal < 0) return null
        val turnsFromEnd = userMessageIndices.size - targetUserOrdinal

        val historySlice = sliceBeforeTurnFromTail(state.history, turnsFromEnd)
        val journalSlice = sliceBeforeTurnFromTail(state.journal, turnsFromEnd)

        // 优先保留已压缩的活跃 history；只有目标轮次已被折叠或使摘要失效时，才从原始 journal 重建。
        val (historyPrefix, compacted) = when {
            historySlice != null && !historySlice.invalidatesSummary -> {
                historySlice.prefix to false
            }
            journalSlice != null && !journalSlice.invalidatesSummary -> {
                journalSlice.prefix to false
            }
            historySlice != null -> {
                historySlice.cleanedPrefix to true
            }
            else -> {
                emptyList<AgentModelClient.ConversationMessage>() to true
            }
        }
        val journalPrefix = if (state.journal.isNotEmpty()) {
            journalSlice?.cleanedPrefix ?: emptyList()
        } else {
            historyPrefix
        }

        return Boundary(
            userMessage = userMessage,
            userMessageIndex = userMessageIndex,
            historyPrefix = historyPrefix,
            journalPrefix = journalPrefix,
            laterTurnCount = turnsFromEnd - 1,
            contextWasCompacted = compacted,
        )
    }

    private data class PrefixSlice(
        val prefix: List<AgentModelClient.ConversationMessage>,
        val invalidatesSummary: Boolean,
        val cleanedPrefix: List<AgentModelClient.ConversationMessage>,
    )

    private fun sliceBeforeTurnFromTail(
        source: List<AgentModelClient.ConversationMessage>,
        turnsFromEnd: Int,
    ): PrefixSlice? {
        if (source.isEmpty()) return null
        val userIndices = source.indices.filter { source[it].role == "user" }
        // 旧版本可能已经丢失前缀；只能对已有记录做尾部对齐，不能伪造恢复。
        val retainedUserOrdinal = userIndices.size - turnsFromEnd
        val cutIndex = userIndices.getOrNull(retainedUserOrdinal) ?: return null
        val prefix = source.take(cutIndex)
        val targetTurn = prefix.sumOf { it.compactedUserTurns + if (it.role == "user") 1 else 0 } + 1
        val invalidatesSummary = prefix.any { it.contextSummary && it.summaryThroughUserTurn >= targetTurn }
        val cleanedPrefix = if (invalidatesSummary) {
            prefix.filterNot { it.contextSummary && it.summaryThroughUserTurn >= targetTurn }
        } else {
            prefix
        }
        return PrefixSlice(
            prefix = prefix,
            invalidatesSummary = invalidatesSummary,
            cleanedPrefix = cleanedPrefix,
        )
    }

    fun deleteFromTurn(state: AgentChatUiState, targetMessageId: String): AgentChatUiState? {
        val boundary = boundary(state, targetMessageId) ?: return null
        return state.copy(
            messages = state.messages.take(boundary.userMessageIndex),
            history = boundary.historyPrefix,
            journal = boundary.journalPrefix,
            messageEdit = null,
        )
    }

    /**
     * 只删除指定的那一条消息，不动它后面的一轮。
     *
     * 与 [deleteFromTurn]（以用户轮次为边界截断）不同：这里只从展示列表里移除目标消息，
     * 并在模型上下文里同步移除同 id 的条目。若移除后留下没有对应 assistant tool_calls 的
     * tool 结果（协议上非法），一并清掉这些孤立条目，避免下一次请求被服务端拒绝。
     */
    fun deleteSingle(state: AgentChatUiState, targetMessageId: String): AgentChatUiState? {
        if (state.messages.none { it.id == targetMessageId }) return null
        val messages = state.messages.filterNot { it.id == targetMessageId }
        val history = pruneContext(state.history, targetMessageId)
        val journal = if (state.journal.isEmpty()) state.journal else pruneContext(state.journal, targetMessageId)
        return state.copy(messages = messages, history = history, journal = journal, messageEdit = null)
    }

    /** 移除目标消息，并清掉失去 tool_calls 归属的 tool 结果。 */
    private fun pruneContext(
        source: List<AgentModelClient.ConversationMessage>,
        removedMessageId: String,
    ): List<AgentModelClient.ConversationMessage> {
        val trimmed = source.filterNot { it.messageId == removedMessageId }
        val callIds = trimmed
            .filter { it.role == "assistant" && it.toolCallsJson.isNotBlank() }
            .flatMap { extractToolCallIds(it.toolCallsJson) }
            .toSet()
        return trimmed.filterNot { it.role == "tool" && it.toolCallId.isNotBlank() && it.toolCallId !in callIds }
    }

    /** 从 tool_calls JSON 里取出 id 字段；解析失败按空处理（宁可不裁剪，也不误删）。 */
    private fun extractToolCallIds(json: String): List<String> = runCatching {
        val array = org.json.JSONArray(json)
        (0 until array.length()).mapNotNull { index ->
            array.optJSONObject(index)?.optString("id")?.takeIf { it.isNotBlank() }
        }
    }.getOrDefault(emptyList())

    fun visibleMessagesForEdit(
        messages: List<AgentChatMessageUi>,
        targetMessageId: String?,
    ): List<AgentChatMessageUi> {
        if (targetMessageId == null) return messages
        val targetIndex = messages.indexOfFirst { it.id == targetMessageId }
        return if (targetIndex < 0) messages else messages.take(targetIndex + 1)
    }
}
