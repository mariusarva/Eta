package io.github.mangi.eta.ui.components.custom

import java.security.MessageDigest

/** Pure formatting only; the official installer remains the authoritative strict validator. */
internal object CustomMessageSkillFormat {
    data class Document(val id: String, val markdown: String)

    fun document(text: String): Document {
        require(text.isNotEmpty()) { "技能内容不能为空" }
        // Reject oversized input early, before hashing/ZIP buffering. The final UTF-8 byte
        // quota (including frontmatter) is checked separately by the caller and installer.
        require(text.length <= 512 * 1024) { "技能内容过长，文件不能超过 512 KiB" }
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8))
            .take(8)
            .joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
        val id = "chat-$hash"
        // Only generated ASCII scalars enter YAML. User text stays verbatim in the Markdown
        // body, even when it itself contains frontmatter, quotes, newlines, or instructions.
        val markdown = "---\nname: $id\ndescription: Saved chat content explicitly selected by the user.\n---\n\n$text"
        return Document(id, markdown)
    }
}
