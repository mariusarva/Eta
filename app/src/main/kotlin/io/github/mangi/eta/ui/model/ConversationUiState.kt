package io.github.mangi.eta.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class ConversationPaneUiState(
    val conversations: List<ConversationSummaryUi>,
    val selectedConversationId: String?,
    val searchQuery: String,
    /** 用户自建的会话分类，按 sort_order 升序。 */
    val categories: List<ConversationCategoryUi> = emptyList(),
    /** 非空时侧栏只显示该分类下的会话。 */
    val categoryFilterId: String? = null,
)

/** 侧栏用的分类行：名字 + 归属会话数。 */
@Immutable
data class ConversationCategoryUi(
    val id: String,
    val name: String,
    val sortOrder: Int,
    val conversationCount: Int,
)

@Immutable
data class ConversationSummaryUi(
    val id: String,
    val title: String,
    val preview: String,
    val timeLabel: String,
    val updatedAtMillis: Long = 0L,
    val mode: ConversationModeUi,
    val isPinned: Boolean = false,
    val isActiveRun: Boolean = false,
    val runStatus: String? = null,
    val characterName: String? = null,
    val characterAvatarPath: String? = null,
    /** 所属分类；null 表示未分类。 */
    val categoryId: String? = null,
)

@Immutable
enum class ConversationModeUi {
    Chat,
    PhoneAgent,
    Terminal,
    Automation,
}
