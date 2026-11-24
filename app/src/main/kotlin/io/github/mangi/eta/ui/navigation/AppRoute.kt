package io.github.mangi.eta.ui.navigation

import kotlinx.serialization.Serializable
import io.github.mangi.eta.agent.browser.BrowserSessionRegistry
import top.yukonga.miuix.kmp.nav.core.NavKey

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object Home : AppRoute

    @Serializable
    data class Browser(val sessionKey: String = BrowserSessionRegistry.MANUAL_SESSION_KEY) : AppRoute

    @Serializable
    data object Terminal : AppRoute

    @Serializable
    data object Tools : AppRoute

    @Serializable
    data object WebToolsSettings : AppRoute

    @Serializable
    data object ImageGenSettings : AppRoute

    @Serializable
    data object ImageGenGallery : AppRoute

    @Serializable
    data object Skills : AppRoute

    /** 技能市场：内置目录 + 从公开仓库安装技能。 */
    @Serializable
    data object SkillMarket : AppRoute

    @Serializable
    data object ScheduledTasks : AppRoute

    @Serializable
    data class ScheduledTaskDetail(val taskId: String) : AppRoute

    @Serializable
    data class ScheduledTaskEditor(
        val taskId: String? = null,
        val presetId: String? = null,
    ) : AppRoute

    @Serializable
    data object Subagents : AppRoute

    // 提示词 Studio：预设 / 自定义 / 版本历史 / 工具白名单 / 快照与完整性。
    @Serializable
    data object PromptPresets : AppRoute

    @Serializable
    data object PromptStudio : AppRoute

    @Serializable
    data object PromptEditor : AppRoute

    @Serializable
    data object PromptVersions : AppRoute

    @Serializable
    data object PromptTools : AppRoute

    @Serializable
    data object PromptSnapshots : AppRoute

    @Serializable
    data class PromptSnapshotDetail(val fileName: String) : AppRoute

    @Serializable
    data class PromptPresetEditor(val name: String? = null) : AppRoute

    @Serializable
    data object PromptIntegrity : AppRoute

    @Serializable
    data object PromptEffective : AppRoute

    @Serializable
    data class PromptEffectiveItem(val segment: Int) : AppRoute

    @Serializable
    data object PromptToolGroups : AppRoute

    /** 会话分类管理（整页）。 */
    @Serializable
    data object ConversationCategories : AppRoute

    /** GitHub 账号：设备码授权 + 终端 git 同步 + 官方远程 MCP 自动注册。 */
    @Serializable
    data object GitHubAccount : AppRoute

    @Serializable
    data object ConversationTitleSettings : AppRoute

    @Serializable
    data object AssistantOverlaySettings : AppRoute

    @Serializable
    data object Characters : AppRoute

    @Serializable
    data class CharacterDetail(val characterId: String) : AppRoute

    @Serializable
    data class CharacterEditor(val characterId: String? = null) : AppRoute

    @Serializable
    data object CharacterPersona : AppRoute

    @Serializable
    data class CharacterMemory(val characterId: String) : AppRoute

    @Serializable
    data object Permissions : AppRoute

    @Serializable
    data object SystemEnhance : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object AppearanceSettings : AppRoute

    @Serializable
    data object SpeechSettings : AppRoute

    @Serializable
    data object SpeechRecognition : AppRoute

    @Serializable
    data object SpeechSynthesis : AppRoute

    @Serializable
    data object SpeechOss : AppRoute

    @Serializable
    data object DataBackup : AppRoute

    @Serializable
    data object Memory : AppRoute

    @Serializable
    data object LinuxEnvironment : AppRoute

    @Serializable
    data object SharedFolders : AppRoute

    @Serializable
    data object Workspace : AppRoute

    @Serializable
    data class LinuxFiles(val distribution: String) : AppRoute

    @Serializable
    data object ModelProviders : AppRoute

    @Serializable
    data object CommunityCatalog : AppRoute

    @Serializable
    data class CommunityCatalogProvider(val catalogId: String) : AppRoute

    @Serializable
    data object McpServers : AppRoute

    @Serializable
    data class McpServerDetail(val serverId: String) : AppRoute

    @Serializable
    data class ModelProviderDetail(val providerId: String) : AppRoute

    @Serializable
    data class ModelProviderNew(val providerType: NewProviderType) : AppRoute
}

@Serializable
enum class NewProviderType { OpenAiCompatible, Anthropic, Gemini }
