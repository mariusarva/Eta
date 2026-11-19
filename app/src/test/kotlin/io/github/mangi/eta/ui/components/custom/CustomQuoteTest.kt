package io.github.mangi.eta.ui.components.custom

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CustomQuoteTest {
    @Test fun preservesActualDraftIncludingWhitespace() {
        assertEquals(
            "> 第一行\n> \n> 第二行\n\n  草稿\n尚未发送  ",
            quoteWithDraft("第一行\n\n第二行", "  草稿\n尚未发送  "),
        )
    }

    @Test fun normalizesOnlyQuotedLineEndings() {
        assertEquals(
            "> a\n> b\n> c\n\ndraft\r\n",
            quoteWithDraft("a\r\nb\rc", "draft\r\n"),
        )
    }

    @Test fun emptyDraftLeavesBlankLineForTyping() {
        val result = quoteWithDraft("消息", "")
        assertEquals("> 消息\n\n", result)
        assertFalse(result.contains("UUID"))
    }
}
