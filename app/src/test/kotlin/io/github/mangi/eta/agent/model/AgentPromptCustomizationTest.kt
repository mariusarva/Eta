package io.github.mangi.eta.agent.model

import io.github.mangi.eta.agent.memory.AgentMemoryContext
import io.github.mangi.eta.agent.prompt.PromptCustomization
import io.github.mangi.eta.agent.prompt.PromptCustomizationMode
import io.github.mangi.eta.agent.skill.SkillContext
import org.json.JSONArray
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 自定义提示词与 provider systemPrompt 的优先级契约。
 *
 * 这两条曾经是同一个 bug：预设里 base 段留空时，buildSystemMessages 会把
 * provider 的 systemPrompt 当成 base 重新注入，用户看到的是“预设应用了但没生效”。
 */
class AgentPromptCustomizationTest {
    @Test
    fun emptyCustomBaseDoesNotFallBackToProviderPrompt() {
        val customization = PromptCustomization(
            enabled = true,
            mode = PromptCustomizationMode.APPEND,
            baseSystemPrompt = "",
            appendInstructions = "只加一条场景约束。",
        )
        val contents = build(customization, providerPrompt = "内置提供方提示").systemContents()
        assertFalse(contents.any { it.contains("内置提供方提示") })
        assertTrue(contents.any { it.contains("只加一条场景约束。") })
    }

    @Test
    fun replaceModeWrapsCustomIdentityAndKeepsToolRules() {
        val customization = PromptCustomization(
            enabled = true,
            mode = PromptCustomizationMode.REPLACE,
            baseSystemPrompt = "你是一个测试人格。",
        )
        val contents = build(customization, providerPrompt = "内置提供方提示").systemContents()
        assertFalse(contents.any { it.contains("内置提供方提示") })
        val identity = contents.first()
        assertTrue(identity.contains("<custom_identity"))
        assertTrue(identity.contains("你是一个测试人格。"))
        assertTrue(identity.contains("优先级高于后文的内置身份描述"))
    }

    @Test
    fun disabledCustomizationKeepsProviderPrompt() {
        val contents = build(PromptCustomization(), providerPrompt = "内置提供方提示").systemContents()
        assertTrue(contents.any { it.contains("内置提供方提示") })
    }

    private fun build(customization: PromptCustomization, providerPrompt: String): JSONArray =
        AgentPromptBuilder.buildSystemMessages(
            config = AgentModelClient.ModelConfig(
                baseUrl = "https://example.invalid/v1",
                apiKey = "test-key",
                model = "test-model",
                systemPrompt = providerPrompt,
                terminalTools = false,
                browserTools = false,
            ),
            skillContext = SkillContext.EMPTY,
            memoryContext = AgentMemoryContext.DISABLED,
            rootAvailable = false,
            customization = customization,
        )

    private fun JSONArray.systemContents(): List<String> =
        (0 until length())
            .map(::getJSONObject)
            .filter { message -> message.getString("role") == "system" }
            .map { message -> message.getString("content") }
}
