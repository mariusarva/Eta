package io.github.mangi.eta.data.db

import android.database.Cursor
import java.io.InputStream

/** JSON 解码器逐块消费正文，避免先拼接整份字符串再创建第二份对象图。 */
internal fun ChunkedTextDao.openTextStream(table: String, owner: String, field: String, stored: String): InputStream {
    if (!stored.startsWith(ChunkedTextDao.REFERENCE_PREFIX)) return stored.byteInputStream(Charsets.UTF_8)
    val sizes = stored.removePrefix(ChunkedTextDao.REFERENCE_PREFIX).split(':')
    if (sizes.size != 2) return "".byteInputStream(Charsets.UTF_8)
    val count = sizes[0].toIntOrNull() ?: return "".byteInputStream(Charsets.UTF_8)
    val length = sizes[1].toIntOrNull() ?: return "".byteInputStream(Charsets.UTF_8)
    if (count <= 0 || length <= 0) return "".byteInputStream(Charsets.UTF_8)
    return ChunkedTextInputStream(textChunkCursor(table, owner, field), count, length)
}

/**
 * 按引用逐块读取。
 *
 * 历史数据里存在悬空引用或长度偏差（旧库迁移、分块被清理等）。这类问题只应影响
 * 对应字段的内容，不能让整次导出/备份抛异常中断，所以读到缺失或越界就提前收尾，
 * 返回已读到的部分。
 */
private class ChunkedTextInputStream(
    private val cursor: Cursor,
    private val expectedCount: Int,
    private val expectedLength: Int,
) : InputStream() {
    private var chunk = byteArrayOf()
    private var position = 0
    private var chunkCount = 0
    private var textLength = 0
    private var ended = false

    override fun read(): Int = if (ensureChunk()) chunk[position++].toInt() and 0xff else -1

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        require(offset >= 0 && length >= 0 && offset <= buffer.size - length)
        if (length == 0) return 0
        if (!ensureChunk()) return -1
        val count = minOf(length, chunk.size - position)
        chunk.copyInto(buffer, offset, position, position + count)
        position += count
        return count
    }

    private fun ensureChunk(): Boolean {
        while (position == chunk.size && !ended) {
            if (!cursor.moveToNext()) {
                // 分块不足：不再断言，直接结束。
                ended = true
                chunk = byteArrayOf()
                break
            }
            // 块序不符或超出声明数量时停止，保留已读内容。
            if (cursor.getInt(0) != chunkCount || chunkCount >= expectedCount) {
                ended = true
                chunk = byteArrayOf()
                break
            }
            val text = cursor.getString(1)
            textLength += text.length
            chunk = text.toByteArray(Charsets.UTF_8)
            chunkCount++
            position = 0
        }
        return !ended
    }

    override fun close() = cursor.close()
}
