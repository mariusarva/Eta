package io.github.mangi.eta.agent.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * `ask_user`：让模型主动向用户提问，并把选项渲染成可点选的卡片。
 *
 * 调用后本轮暂停，用户在卡片上点选后答案以消息形式回灌，
 * 模型不必自己编答案，也不必等用户手打「A」。
 */
internal object AgentAskToolCatalog {
    const val ASK_USER = "ask_user"

    fun schema(): JSONObject = AgentToolSchema.function(
        name = ASK_USER,
        description = "向用户提出一个带若干选项的问题，本轮会暂停直到用户点选。" +
            "用于：需要用户拍板、偏好无法从上下文推断、或有多条合理路线需要他选。" +
            "不要用它问已经知道答案的事，也不要用它代替确认危险动作（危险动作由系统自动拦截）。",
        parameters = JSONObject()
            .put("type", "object")
            .put(
                "properties",
                JSONObject()
                    .put(
                        "question",
                        JSONObject().put("type", "string")
                            .put("description", "一句话问题，作为卡片标题。"),
                    )
                    .put(
                        "body",
                        JSONObject().put("type", "string")
                            .put("description", "补充说明：为什么问他、这个选择会影响什么。"),
                    )
                    .put(
                        "options",
                        JSONObject().put("type", "array")
                            .put("description", "选项列表，第一个作为推荐项；不要为了凑数塞无用项。")
                            .put(
                                "items",
                                JSONObject()
                                    .put(
                                        "properties",
                                        JSONObject()
                                            .put(
                                                "label",
                                                JSONObject().put("type", "string")
                                                    .put("description", "选项文案，尽量短。"),
                                            )
                                            .put(
                                                "description",
                                                JSONObject().put("type", "string")
                                                    .put("description", "选了这个会发生什么。"),
                                            ),
                                    )
                                    .put("required", JSONArray().put("label")),
                            ),
                    )
                    .put(
                        "allow_multiple",
                        JSONObject().put("type", "boolean")
                            .put("description", "是否允许多选。默认 false。"),
                    ),
            )
            .put("required", JSONArray().put("question").put("options")),
    )
}
