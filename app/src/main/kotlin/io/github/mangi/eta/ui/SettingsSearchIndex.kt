package io.github.mangi.eta.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import io.github.mangi.eta.R
import io.github.mangi.eta.ui.navigation.AppRoute

/**
 * 设置页的可搜索入口。
 *
 * 只索引「点一下会跳到某个页面/触发某个动作」的条目：开关类条目不跳转，
 * 搜到也没法直接定位到开关本身，反而让人以为设置页支持逐项跳转。
 *
 * [keywords] 放用户可能想到但标题里没有的说法（英文名、别名、旧叫法），
 * 中文标题本身会参与匹配，不需要重复列一遍。
 */
@Immutable
internal data class SettingsSearchEntry(
    @StringRes val titleRes: Int,
    @StringRes val groupRes: Int,
    val route: SettingsSearchRoute,
    val keywords: List<String> = emptyList(),
)

/** 搜索结果的跳转目标；用封闭类型避免把 AppRoute 全量搬进来。 */
@Immutable
internal sealed interface SettingsSearchRoute {
    data class Navigate(val route: AppRoute) : SettingsSearchRoute
    data object CheckForUpdate : SettingsSearchRoute
    data object Feedback : SettingsSearchRoute
    data object StarOnGitHub : SettingsSearchRoute
}

/**
 * 设置页搜索索引。
 *
 * 新增设置入口时在这里补一条，否则搜不到（这是纯数据表，不会因为漏加而崩溃）。
 */
internal object SettingsSearchIndex {

    val entries: List<SettingsSearchEntry> = listOf(
        // 模型与对话
        SettingsSearchEntry(R.string.ui_model_provider_e8c7f5, R.string.settings_llm_providers,
            SettingsSearchRoute.Navigate(AppRoute.ModelProviders),
            listOf("provider", "api", "模型", "服务商", "key")),
        SettingsSearchEntry(R.string.conversation_title_settings_title, R.string.settings_context_extensions,
            SettingsSearchRoute.Navigate(AppRoute.ConversationTitleSettings),
            listOf("title", "标题", "自动命名")),
        SettingsSearchEntry(R.string.ui_memory_b55ff5, R.string.settings_context_extensions,
            SettingsSearchRoute.Navigate(AppRoute.Memory),
            listOf("memory", "记忆", "memory.md")),
        SettingsSearchEntry(R.string.ui_misc_route_subagents, R.string.settings_context_extensions,
            SettingsSearchRoute.Navigate(AppRoute.Subagents),
            listOf("subagent", "子代理", "子智能体")),
        SettingsSearchEntry(R.string.route_skills, R.string.settings_context_extensions,
            SettingsSearchRoute.Navigate(AppRoute.Skills),
            listOf("skill", "技能", "技能市场")),
        SettingsSearchEntry(R.string.route_mcp_servers, R.string.settings_context_extensions,
            SettingsSearchRoute.Navigate(AppRoute.McpServers),
            listOf("mcp", "服务器", "工具协议")),
        SettingsSearchEntry(R.string.route_scheduled_tasks, R.string.settings_context_extensions,
            SettingsSearchRoute.Navigate(AppRoute.ScheduledTasks),
            listOf("schedule", "定时", "任务", "cron")),
        SettingsSearchEntry(R.string.conversation_dock_characters, R.string.settings_context_extensions,
            SettingsSearchRoute.Navigate(AppRoute.Characters),
            listOf("character", "角色", "角色卡")),
        SettingsSearchEntry(R.string.settings_github_account, R.string.settings_context_extensions,
            SettingsSearchRoute.Navigate(AppRoute.GitHubAccount),
            listOf("github", "账号", "登录", "token")),
        SettingsSearchEntry(R.string.settings_prompt_studio, R.string.settings_context_extensions,
            SettingsSearchRoute.Navigate(AppRoute.PromptStudio),
            listOf("prompt", "提示词", "边界")),

        // 工具
        SettingsSearchEntry(R.string.settings_tools_list, R.string.ui_tool_a72ef1,
            SettingsSearchRoute.Navigate(AppRoute.Tools),
            listOf("tool", "工具", "列表")),
        SettingsSearchEntry(R.string.web_tools_settings_title, R.string.ui_tool_a72ef1,
            SettingsSearchRoute.Navigate(AppRoute.WebToolsSettings),
            listOf("web", "网页", "搜索")),
        SettingsSearchEntry(R.string.image_gen_settings_title, R.string.ui_tool_a72ef1,
            SettingsSearchRoute.Navigate(AppRoute.ImageGenSettings),
            listOf("image", "画图", "生图", "绘图")),
        SettingsSearchEntry(R.string.ui_linux_tool_environment_314d22, R.string.ui_tool_a72ef1,
            SettingsSearchRoute.Navigate(AppRoute.LinuxEnvironment),
            listOf("linux", "终端", "环境", "debian")),
        SettingsSearchEntry(R.string.capability_enhancements, R.string.ui_tool_a72ef1,
            SettingsSearchRoute.Navigate(AppRoute.SystemEnhance),
            listOf("enhance", "增强", "root", "系统")),

        // 助手与交互
        SettingsSearchEntry(R.string.assistant_overlay_settings_title,
            R.string.ui_system_assistant_takes_over_f46043,
            SettingsSearchRoute.Navigate(AppRoute.AssistantOverlaySettings),
            listOf("assistant", "助手", "浮窗", "悬浮")),

        // 通用
        SettingsSearchEntry(R.string.appearance_title, R.string.settings_general,
            SettingsSearchRoute.Navigate(AppRoute.AppearanceSettings),
            listOf("appearance", "外观", "主题", "深色", "颜色")),
        SettingsSearchEntry(R.string.speech_settings_title, R.string.settings_general,
            SettingsSearchRoute.Navigate(AppRoute.SpeechSettings),
            listOf("speech", "语音", "朗读", "tts", "识别")),
        SettingsSearchEntry(R.string.data_backup_title, R.string.settings_general,
            SettingsSearchRoute.Navigate(AppRoute.DataBackup),
            listOf("backup", "备份", "恢复", "导出", "导入")),

        // 关于
        SettingsSearchEntry(R.string.ui_about_update_title, R.string.ui_about_bed172,
            SettingsSearchRoute.CheckForUpdate,
            listOf("update", "更新", "升级", "版本")),
        SettingsSearchEntry(R.string.ui_about_feedback_title, R.string.ui_about_bed172,
            SettingsSearchRoute.Feedback,
            listOf("feedback", "反馈", "问题", "issue")),
        SettingsSearchEntry(R.string.ui_about_github_star_title, R.string.ui_about_bed172,
            SettingsSearchRoute.StarOnGitHub,
            listOf("star", "star", "github", "收藏")),
    )

    /**
     * 按关键词过滤。
     *
     * 标题、分组名、[SettingsSearchEntry.keywords] 任一命中即保留；空查询返回全部。
     * 大小写不敏感，中文按字面子串匹配。
     */
    fun search(query: String, resolve: (Int) -> String): List<SettingsSearchEntry> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return entries
        val needle = trimmed.lowercase()
        return entries.filter { entry ->
            val haystacks = buildList {
                add(resolve(entry.titleRes))
                add(resolve(entry.groupRes))
                addAll(entry.keywords)
            }
            haystacks.any { it.lowercase().contains(needle) }
        }
    }
}
