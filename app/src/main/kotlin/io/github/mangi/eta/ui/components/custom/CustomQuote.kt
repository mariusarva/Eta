package io.github.mangi.eta.ui.components.custom

/** Preserve the live local composer draft verbatim; never send or add hidden markers. */
internal fun quoteWithDraft(text: String, draft: String): String =
    text.replace("\r\n", "\n").replace('\r', '\n')
        .split('\n').joinToString("\n") { "> $it" } + "\n\n" + draft
