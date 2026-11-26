package io.github.mangi.eta.ui.components

/**
 * 对话页横向 Pager 的页序。
 *
 * 顺序即物理顺序：对话（主页面，索引 0）→ 向右滑露出工作文件夹。
 * 对话页右滑（手指右移）不翻页，改为打开侧边工作台。
 */
internal enum class ChatPage {
    CONVERSATION,
    WORKSPACE,
}

