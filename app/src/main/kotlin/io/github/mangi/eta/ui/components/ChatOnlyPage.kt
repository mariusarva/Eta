package io.github.mangi.eta.ui.components

/**
 * 对话页横向 Pager 的页序。
 *
 * 顺序即物理顺序：左 = 工作文件夹，中 = 对话。
 * 用 enum 而不是裸 index，避免后续插入页面时索引错位。
 */
internal enum class ChatPage {
    WORKSPACE,
    CONVERSATION,
}

