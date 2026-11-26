package io.github.mangi.eta.agent.ask

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/** 卡片上的一个可选项。`id` 回灌给模型，`label` 给用户看。 */
internal data class AskOption(
    val id: String,
    val label: String,
    val description: String = "",
)

/**
 * 一次待答问题（模型主动提问）。
 *
 * `token` 是回灌用的唯一标识；同一会话同一时刻只有一条待答。
 */
internal data class PendingAsk(
    val token: String,
    val conversationId: String,
    val question: String,
    val body: String = "",
    val options: List<AskOption> = emptyList(),
    val allowMultiple: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    /**
     * 已作答的结果；null 表示还等着用户回答。
     *
     * 答完不删记录，改成写这里：卡片继续留在消息流原位置，
     * 用户回滚时还能看到自己选了什么。
     */
    val answer: AskAnswer? = null,
) {
    /** 是否已作答（选过或跳过）。 */
    val answered: Boolean get() = answer != null
}

/**
 * 一次作答的结果。
 *
 * [skipped] 与 [labels] 为空是两件事：跳过代表用户明确不回答，
 * 空列表代表异常（正常不会出现）。UI 上要能区分这两种状态。
 */
internal data class AskAnswer(
    val labels: List<String> = emptyList(),
    val skipped: Boolean = false,
    val answeredAtMillis: Long = System.currentTimeMillis(),
)

/**
 * 待答问题的落盘。
 *
 * 用独立 SharedPreferences 而不是 DataStore：写入后 UI 需要同步立即可见，
 * DataStore 是挂起 API；且只有极少几条记录，不值得扩 Settings 的 schema。
 *
 * 存数组而不是单条：不同会话可以各挂一张卡，互不覆盖。
 */
internal object PendingAskStore {

    private const val FILE = "eta_pending_ask"
    private const val KEY = "current"

    @Volatile
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        }
    }

    fun load(): List<PendingAsk> {
        val store = prefs ?: return emptyList()
        val raw = store.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    add(parse(array.getJSONObject(index)))
                }
            }
        }.getOrDefault(emptyList())
    }

    fun save(pendings: List<PendingAsk>) {
        val store = prefs ?: return
        if (pendings.isEmpty()) {
            store.edit().remove(KEY).apply()
            return
        }
        val array = JSONArray()
        pendings.forEach { array.put(write(it)) }
        store.edit().putString(KEY, array.toString()).apply()
    }

    fun put(pending: PendingAsk) {
        val current = load().filterNot { it.token == pending.token || it.conversationId == pending.conversationId }
        save(current + pending)
    }

    fun remove(token: String) {
        save(load().filterNot { it.token == token })
    }

    /**
     * 记录作答结果，但不删卡片。
     *
     * 卡片答完仍留在消息流里：用户往上翻时能看到自己当时选了什么。
     * 只有会话被删除时才随 [removeConversation] 一起清掉。
     */
    fun answer(token: String, answer: AskAnswer) {
        save(
            load().map { pending ->
                if (pending.token == token) pending.copy(answer = answer) else pending
            },
        )
    }

    /** 会话删除时清掉它的卡片，避免越积越多。 */
    fun removeConversation(conversationId: String) {
        save(load().filterNot { it.conversationId == conversationId })
    }

    fun find(token: String): PendingAsk? = load().firstOrNull { it.token == token }

    fun forConversation(conversationId: String): PendingAsk? =
        load().firstOrNull { it.conversationId == conversationId }

    private fun write(pending: PendingAsk): JSONObject = JSONObject()
        .put("token", pending.token)
        .put("conversationId", pending.conversationId)
        .put("question", pending.question)
        .put("body", pending.body)
        .put("allowMultiple", pending.allowMultiple)
        .put("createdAtMillis", pending.createdAtMillis)
        .put(
            "answer",
            pending.answer?.let { answer ->
                JSONObject()
                    .put("labels", JSONArray().also { array -> answer.labels.forEach(array::put) })
                    .put("skipped", answer.skipped)
                    .put("answeredAtMillis", answer.answeredAtMillis)
            } ?: JSONObject.NULL,
        )
        .put(
            "options",
            JSONArray().also { array ->
                pending.options.forEach { option ->
                    array.put(
                        JSONObject()
                            .put("id", option.id)
                            .put("label", option.label)
                            .put("description", option.description),
                    )
                }
            },
        )

    private fun parse(json: JSONObject): PendingAsk {
        val options = mutableListOf<AskOption>()
        json.optJSONArray("options")?.let { array ->
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                options += AskOption(
                    id = item.optString("id"),
                    label = item.optString("label"),
                    description = item.optString("description"),
                )
            }
        }
        return PendingAsk(
            token = json.optString("token"),
            conversationId = json.optString("conversationId"),
            question = json.optString("question"),
            body = json.optString("body"),
            options = options,
            allowMultiple = json.optBoolean("allowMultiple"),
            createdAtMillis = json.optLong("createdAtMillis"),
            answer = json.optJSONObject("answer")?.let { answer ->
                AskAnswer(
                    labels = buildList {
                        answer.optJSONArray("labels")?.let { array ->
                            for (index in 0 until array.length()) add(array.optString(index))
                        }
                    },
                    skipped = answer.optBoolean("skipped"),
                    answeredAtMillis = answer.optLong("answeredAtMillis"),
                )
            },
        )
    }
}
