package io.github.mangi.eta.ui.components.custom

import io.github.mangi.eta.agent.voice.SpeechText

/** Reuse the official semantic Markdown formatter (links keep labels; URLs/tables/code skip).
 * Rows here are speech locations, never replacement text for the original selection UI.
 */
internal object CustomSpeechText {
    private val boundary = Regex("(?<=[。！？!?；;\\n])|(?<=\\.)(?=\\s|$)")
    fun sentences(markdown: String): List<String> {
        require(markdown.length <= 100_000) { "文本过长，请只选择需要朗读的部分" }
        val readable = SpeechText.readable(markdown)
        return splitReadable(readable)
    }

    internal fun splitReadable(readable: String): List<String> {
        require(readable.length <= 30_000) { "正文过长，请只选择需要朗读的部分" }
        return readable.split(boundary).flatMap { SpeechText.chunks(it, limit = 320) }.filter { it.isNotBlank() }
    }
}
