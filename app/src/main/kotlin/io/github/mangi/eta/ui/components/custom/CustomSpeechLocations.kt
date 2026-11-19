package io.github.mangi.eta.ui.components.custom

/** Text coordinates in the concatenation of CustomSpeechText's final spoken segments.
 * Kotlin String.length is UTF-16 (not code points, bytes, or estimated audio duration).
 * Exact boundaries select the following sentence; EOF selects the last nonempty one.
 */
internal class CustomSpeechLocations(sentences: List<String>) {
    private val starts = IntArray(sentences.size + 1).also { offsets ->
        sentences.forEachIndexed { index, text -> offsets[index + 1] = offsets[index] + text.length }
    }
    val totalUtf16: Int get() = starts.last()

    fun startOffset(sentence: Int): Int =
        if (starts.size == 1) 0 else starts[sentence.coerceIn(0, starts.size - 2)]

    /** Returns -1 when there is no spoken text, otherwise a valid nonempty sentence. */
    fun sentenceAt(offset: Int): Int {
        if (totalUtf16 == 0) return -1
        val target = offset.coerceIn(0, totalUtf16 - 1)
        var low = 0
        var high = starts.lastIndex
        while (low < high) {
            val mid = (low + high + 1) ushr 1
            if (starts[mid] <= target) low = mid else high = mid - 1
        }
        return low
    }
}
