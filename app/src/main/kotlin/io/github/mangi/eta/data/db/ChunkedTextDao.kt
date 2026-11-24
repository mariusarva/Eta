package io.github.mangi.eta.data.db

import android.database.Cursor
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/** 每行有界，文档总长不限；避免完整历史占用单个 CursorWindow 行。 */
@Entity(tableName = "agent_text_chunks", primaryKeys = ["owner_table", "owner_id", "field", "chunk_index"])
internal data class AgentTextChunkEntity(
    @ColumnInfo(name = "owner_table") val ownerTable: String,
    @ColumnInfo(name = "owner_id") val ownerId: String,
    val field: String,
    @ColumnInfo(name = "chunk_index") val chunkIndex: Int,
    val content: String,
)

/** 由业务 DAO 在同一 Room 事务内写入主记录与分块，不暴露存储引用给领域层。 */
internal interface ChunkedTextDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTextChunk(chunk: AgentTextChunkEntity)

    @Query("DELETE FROM agent_text_chunks WHERE owner_table = :table AND owner_id = :owner AND field = :field")
    suspend fun deleteTextChunks(table: String, owner: String, field: String)

    @Query("SELECT * FROM agent_text_chunks WHERE owner_table = :table AND owner_id = :owner AND field = :field ORDER BY chunk_index LIMIT :limit OFFSET :offset")
    suspend fun textChunks(table: String, owner: String, field: String, limit: Int, offset: Int): List<AgentTextChunkEntity>

    @Query("SELECT chunk_index, content FROM agent_text_chunks WHERE owner_table = :table AND owner_id = :owner AND field = :field ORDER BY chunk_index")
    fun textChunkCursor(table: String, owner: String, field: String): Cursor

    suspend fun storeText(table: String, owner: String, field: String, text: String): String {
        deleteTextChunks(table, owner, field)
        if (text.length <= CHUNK_CHARS && !text.startsWith(REFERENCE_PREFIX)) return text
        var count = 0
        var offset = 0
        while (offset < text.length) {
            // 不把 UTF-16 代理对拆到两个 SQLite TEXT 中。
            var end = minOf(offset + CHUNK_CHARS, text.length)
            if (end < text.length && text[end - 1].isHighSurrogate()) end--
            insertTextChunk(AgentTextChunkEntity(table, owner, field, count++, text.substring(offset, end)))
            offset = end
        }
        return "$REFERENCE_PREFIX$count:${text.length}"
    }

    suspend fun restoreText(table: String, owner: String, field: String, stored: String): String {
        if (!stored.startsWith(REFERENCE_PREFIX)) return stored
        val parts = stored.removePrefix(REFERENCE_PREFIX).split(':')
        if (parts.size != 2) return ""
        val count = parts[0].toIntOrNull() ?: return ""
        val length = parts[1].toIntOrNull() ?: return ""
        if (count <= 0 || length <= 0) return ""
        val result = StringBuilder()
        var offset = 0
        while (offset < count) {
            val page = textChunks(table, owner, field, minOf(32, count - offset), offset)
            // 分块缺失时返回已恢复的部分，不让整次读取失败：
            // 历史数据里存在悬空引用（例如 images_json 引用分块但分块被清理掉），
            // 抛异常会让整个备份/导出直接中断，代价远大于丢掉个别字段。
            if (page.isEmpty()) break
            var progressed = false
            page.forEach { chunk ->
                if (chunk.chunkIndex != offset) return@forEach
                offset++
                progressed = true
                result.append(chunk.content)
            }
            if (!progressed) break
        }
        return result.toString()
    }

    companion object {
        private const val CHUNK_CHARS = 16_384
        const val REFERENCE_PREFIX = "@eta:chunks:v1:"
    }
}
