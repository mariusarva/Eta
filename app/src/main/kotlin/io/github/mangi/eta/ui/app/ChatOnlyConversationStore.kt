package io.github.mangi.eta.ui.app

import android.content.Context
import android.content.SharedPreferences

/**
 * 纯聊天会话标记的持久化。
 *
 * 为什么不用数据库字段：项目用的是 `fallbackToDestructiveMigration(dropAllTables = true)`，
 * 为一个布尔标记加一版迁移，写错就是清库（fork.97 已经踩过一次）。
 * 这个标记丢了最坏只是该会话退回带工具，内容一条不丢，不值得冒清库风险。
 *
 * 存成会话 ID 集合；读取时用会话列表做一次交集，顺手清掉已被删除的残留。
 */
internal object ChatOnlyConversationStore {

    private const val FILE = "eta_chat_only_conversations"
    private const val KEY = "ids"

    @Volatile
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        }
    }

    /** 全部标记为纯聊天的会话 ID。 */
    fun ids(): Set<String> {
        val store = prefs ?: return emptySet()
        return store.getStringSet(KEY, emptySet()).orEmpty().toSet()
    }

    fun contains(conversationId: String): Boolean =
        conversationId.isNotBlank() && conversationId in ids()

    fun mark(conversationId: String) {
        if (conversationId.isBlank()) return
        val store = prefs ?: return
        store.edit().putStringSet(KEY, ids() + conversationId).apply()
    }

    fun unmark(conversationId: String) {
        val store = prefs ?: return
        val current = ids()
        if (conversationId !in current) return
        store.edit().putStringSet(KEY, current - conversationId).apply()
    }

    /** 只保留仍然存在的会话，清掉已删除会话留下的残留 ID。 */
    fun retainOnly(existingIds: Set<String>) {
        val store = prefs ?: return
        val current = ids()
        val pruned = current intersect existingIds
        if (pruned != current) {
            store.edit().putStringSet(KEY, pruned).apply()
        }
    }
}
