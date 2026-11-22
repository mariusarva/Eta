# 提示词系统移植记录（Eta-por → 本 fork）

日期：2026-11-19
来源：[Oo0520/Eta-por](https://github.com/Oo0520/Eta-por) `58a1faec3`
目标：本 fork（`mariusarva/Eta`，基于 `Mangi-11/Eta` main `042420b56`）

## 为什么做这次移植

上游把系统提示词挂在 `model_providers.system_prompt`（per-provider），只有「每个提供商一段文本」这一层。Eta-por 在它之上加了一整套人格层：可编辑的 base / persona / append、预设的导入导出、版本历史、请求快照与 diff、完整性扫描、工具范围绑定。

第一次移植时只搬了逻辑层（8 个文件）并写了一个极简预设列表页，结果：

- 不能自定义提示词（只能从固定预设里选）
- 不能导入新预设
- 没有 Studio、版本、快照、工具范围等能力

而且更根本的问题：**`AgentPromptBuilder.buildSystemMessages` 从未接受 `PromptCustomization` 参数**，所以即使把配置存进 prefs，也从未进入实际请求。这是「不能自定义提示词」的真正原因。

## 最终方案：逻辑层直接搬，组件层补齐，UI 层零改动

尝试过三条路，最后选的是第三条：

| 方案 | 做法 | 结果 |
|---|---|---|
| A 全量照搬 | 16 个 UI + 12 个组件 + 290 字符串 | UI 风格混入 Miuix，与官方 COUI 并存 |
| B UI 全部重写 COUI | 重写 15.5 万字节页面 | 工作量过大，且会丢掉 Eta-por 的交互细节 |
| **C 保留 UI 原样，补齐组件** | UI 文件零改动，只补它调用的组件 | **采用** |

C 的关键洞察：UI 文件调用的是组件 API（`EtaArrowPreference`、`MiuixScaffoldPage` 等），不关心内部实现。只要 API 签名一致，页面代码一行不用改。

## 移植清单

### 1. 逻辑层（9 个，零改动）

`app/src/main/kotlin/io/github/mangi/eta/agent/prompt/`：

| 文件 | 大小 |
|---|---|
| `PromptCustomization.kt` | 9735 |
| `PromptConflictAnalyzer.kt` | 8432 |
| `PromptPreset.kt` | 5214 |
| `PromptPresetCodec.kt` | 4278 |
| `PromptPresetRepository.kt` | 3589 |
| `AgentToolPolicy.kt` | 3621 |
| `ToolGroupStore.kt` | 2630 |
| `PromptCustomizationStore.kt` | 1661 |

全部只依赖 `io.github.mangi.eta.config.Prefs`，上游同名类存在、键名机制一致，**零改动**。

### 2. 依赖类（8 个，官方完全没有）

| 文件 | 大小 | 位置 |
|---|---|---|
| `AgentRequestSnapshot.kt` | 7482 | `agent/model/` |
| `AgentSnapshotAnalysis.kt` | 7369 | `agent/model/` |
| `AgentContextSnapshot.kt` | 1049 | `agent/model/` |
| `AgentToolDirectory.kt` | 2522 | `agent/model/` |
| `LocalToolProfile.kt` | 800 | `agent/model/` |
| `LocalToolProfileStore.kt` | 1763 | `agent/model/` |
| `AgentToolNaming.kt` | 11101 | `agent/tool/` |
| `PromptIntegrityGuard.kt` | 9674 | `agent/security/` |

`AgentContextSnapshot` 和 `AgentRequestSnapshot` 零外部依赖，其余只依赖已移植的 prompt 包。

### 3. 组件层（补齐 9 个，回滚 5 个）

**新增**（官方没有，因为官方已全面 COUI 化）：

| 文件 | 大小 | 说明 |
|---|---|---|
| `EtaPreferenceRow.kt` | 8074 | `EtaArrowPreference` / `EtaPreference` / `EtaPreferenceRow` |
| `EtaPreferenceStyle.kt` | 4092 | `EtaPreferenceTheme` / `EtaPreferenceGroup` / `EtaPreferenceColors` / `EtaPreferenceIcon` / `EtaPreferenceDivider` |
| `EtaActionRows.kt` | 5834 | `EtaActionPreference` / `EtaActionTone` / `EtaExpandPreference` / `EtaSelectPreference` |
| `EtaControls.kt` | 4596 | `EtaTextButton` / `EtaWindowDialog` / `EtaOverlayDialog` / `EtaSwitch` |
| `EtaDropdownPreference.kt` | 5115 | 三种下拉形态 |
| `EtaSelectionPreferences.kt` | 3822 | `EtaSwitchPreference` / `EtaRadioButtonPreference` / `EtaCheckboxPreference` |
| `EtaCard.kt` | 1779 | |
| `EtaTextToolbar.kt` | 6063 | 文本选择菜单 |

**回滚**（这些上游本来就有，Eta-por 版会破坏上游 API）：

| 文件 | 原因 |
|---|---|
| `MiuixScaffoldPage.kt` | Eta-por 版依赖 `TopBarBackdrop` / `rememberTopBarBackdrop` / `topBarContainerColor` / `captureForTopBar`，官方 GlassBackdrop 里没有 |
| `EtaPreferenceGroupItem.kt` | 上游已有，Eta-por 版删了 `hasLeadingIcon` 参数，导致 AgentToolsScreen / WorkspaceScreen / LinuxFilesScreen 编译失败 |
| `EtaChoiceChip.kt` | 上游已有 |
| `EtaFeatureCard.kt` | 上游已有 |
| `EtaFonts.kt` | 上游已有 |

回滚时**必须用基线提交 `042420b56` 的版本，不能用上游当前 main**——上游 main 已经引用了尚未引入的 `DemandSceneBackdrop`，直接用会编译失败。

### 4. UI 层（16 个，零改动）

`app/src/main/kotlin/io/github/mangi/eta/ui/screens/prompts/`：

| 文件 | 大小 | 作用 |
|---|---|---|
| `PromptStudioScreen.kt` | 9473 | 概览：当前状态、冲突提示、快捷入口 |
| `PromptEditorScreen.kt` | 20084 | 编辑器：base / persona / append + 实时预览 |
| `PromptPresetsScreen.kt` | 13518 | 预设列表：应用、导入、导出、新建、删除 |
| `PromptVersionsScreen.kt` | 11048 | 版本历史：查看、恢复、清理 |
| `PromptSnapshotsScreen.kt` | 16788 | 请求快照列表：保留策略、基线选择 |
| `PromptSnapshotDetailScreen.kt` | 18912 | 快照详情：分段、工具、diff |
| `PromptEffectiveScreen.kt` | 13991 | 实际生效内容：按段展示 |
| `PromptIntegrityScreen.kt` | 9307 | 完整性扫描：文件 / 锚点 / Xposed / 快照 |
| `PromptToolsScreen.kt` | 20570 | 工具范围：搜索、分组、白名单 |
| `PromptToolGroupsScreen.kt` | 12480 | 工具分组管理 |
| `PromptStudioWidgets.kt` | 10621 | Studio 内部组件 |
| `PromptTextField.kt` | 6551 | 带统计与粘贴的文本框 |
| `PromptEditDiff.kt` | 2984 | diff 计算 |
| `PromptEditDiffCard.kt` | 3911 | diff 展示 |
| `ToolSelectionItems.kt` | 16843 | 工具选择项 |
| `ToolSelectorState.kt` | 798 | 选择状态 |

### 5. 字符串（352 条 × 3 套）

`prompt_*` 前缀共 352 条，`values` / `values-b+zh+Hans` / `values-b+zh+Hant` 三套各补 352 条。

另有 3 条组件用到的非 `prompt_` 前缀字符串：`eta_row_collapse`、`eta_row_expand`、`text_action_cut`。

### 6. 路由（12 条）

`AppRoute.kt`：

```kotlin
data object PromptStudio : AppRoute
data object PromptEditor : AppRoute
data object PromptVersions : AppRoute
data object PromptTools : AppRoute
data object PromptPresets : AppRoute
data object PromptSnapshots : AppRoute
data class PromptSnapshotDetail(val fileName: String) : AppRoute
data class PromptPresetEditor(val name: String? = null) : AppRoute
data object PromptIntegrity : AppRoute
data object PromptEffective : AppRoute
data class PromptEffectiveItem(val segment: Int) : AppRoute
data object PromptToolGroups : AppRoute
```

`AgentAppRoot.kt` 逐条 `entry<...>` 挂载，`AgentAppShell.kt` 的 `titleForRoute` 补 11 条标题（`PromptPresets` 之前已有）。

### 7. 核心接入：`AgentPromptBuilder`

这是让自定义提示词**真正生效**的关键改动。上游签名：

```kotlin
fun buildSystemMessages(
    config, skillContext, memoryContext, rootAvailable,
    roleplayContext = null, executionContext = null,
): JSONArray
```

改造后：

```kotlin
fun buildSystemMessages(
    config, skillContext, memoryContext, rootAvailable,
    roleplayContext = null, executionContext = null,
    customization: PromptCustomization = PromptCustomization(),
): JSONArray {
    val custom = customization.takeIf { it.active }
    val basePrompt = custom?.baseSystemPrompt?.takeIf { it.isNotBlank() } ?: config.systemPrompt
    if (roleplayContext == null && basePrompt.isNotBlank()) {
        val identity = custom?.let { customIdentityMessage(it, basePrompt) }
        messages.put(identity ?: systemMessage(basePrompt))
    }
    // ... 上游全部正文原样保留 ...
    custom?.let { customPersonaMessage(it) }?.let(messages::put)
    custom?.let { customInstructionsMessage(it) }?.let(messages::put)
    return messages
}
```

三个辅助函数：

- `customIdentityMessage`：`REPLACE` 模式把 base 包进 `<custom_identity version=… revision=…>` 并声明优先级；`APPEND` 模式原样使用
- `customPersonaMessage`：persona 非空时产出 `<custom_persona>`
- `customInstructionsMessage`：append 非空时产出 `<custom_instructions>`

**关键点**：只加参数和注入点，上游那段长正文（模型身份、工具优先级、记忆、Markdown 规则等）一行没动。Eta-por 的 `AgentPromptBuilder` 是 31729 字节的深度改写版，整文件覆盖会丢掉上游十天里的新内容。

`buildMessages` 也要透传 `customization`，否则带历史的请求走不到自定义。

## 踩过的坑

### 坑 1：`return messages` 有多个

第一次注入 `custom?.let {...}` 时用 `src.find('        return messages')` 定位，结果命中的是 `buildMessages` 而非 `buildSystemMessages`。症状：

```
AgentPromptBuilder.kt:30:9 Unresolved reference 'custom'.
AgentPromptBuilder.kt:30:17 Cannot infer type for type parameter 'R'.
```

修法：先 `find('    fun buildSystemMessages(')` 定位函数起点，再在 `start:` 之后的片段里找第一个 `return messages`。

### 坑 2：三层同名参数（与暂停功能同源）

`AgentChatBody` 里有三层同名参数，每一层都要补。`PromptPresetsScreen` 的 `onEditPreset` 也是必填参数，`AgentAppRoot` 里漏传会报：

```
AgentAppRoot.kt:510:25 No value passed for parameter 'onEditPreset'.
```

### 坑 3：不要用上游当前 main 覆盖

回滚 `MiuixScaffoldPage.kt` 时从上游 main 取了最新版，它引用 `DemandSceneBackdrop`（上游新引入，本 fork 基线里没有），编译失败：

```
MiuixScaffoldPage.kt:58:29 Unresolved reference 'DemandSceneBackdrop'.
```

**必须用基线提交 `042420b56` 的版本**。

### 坑 4：`when` 穷尽性

新增 12 条路由后，`AgentAppShell.titleForRoute` 的 `when` 必须补齐，否则：

```
AgentAppShell.kt:646:55 'when' expression must be exhaustive. Add the 'PromptEditor', ... branches or an 'else' branch.
```

### 坑 5：`EtaArrowPreference` 与 `EtaActionPreference` 的图标位置

「新建预设」用 `EtaArrowPreference`，它虽然有 `startAction` 参数但默认 `null`，所以没有图标；旁边的「导入」「导出」用 `EtaActionPreference` 且显式传了 `startAction`，所以有图标。补上即可：

```kotlin
EtaArrowPreference(
    title = stringResource(R.string.prompt_preset_new),
    startAction = { EtaPreferenceIcon(Icons.Rounded.Add, EtaPreferenceColors.Green) },
    onClick = { onEditPreset(null) },
)
```

## 现在可用的能力

进入「设置 → 提示词预设」：

| 能力 | 入口 |
|---|---|
| 自定义 base 提示词 | 预设页 → 新建 / 编辑 → 编辑器 |
| 自定义人格（名称 + 风格） | 同上 |
| 追加指令 | 同上 |
| 切换 REPLACE / APPEND 模式 | 编辑器 |
| 导入预设（zip / json） | 预设页 → 导入 |
| 导出预设 | 预设页 → 导出 |
| 应用预设 | 预设页 → 点任意预设 |
| 删除自定义预设 | 预设页 |
| 版本历史与恢复 | Studio → 版本 |
| 查看实际生效内容（分段） | Studio → 实际生效 |
| 请求快照与 diff | Studio → 快照 |
| 完整性扫描 | Studio → 完整性 |
| 工具范围与分组 | Studio → 工具 |

## 已知遗留

- `PromptStudioWidgets` 的预览依赖 `AgentPromptBuilder.buildSystemMessages(customization=…)`，已通。但预览里的工具列表是本地拼的（MCP + conversation_history），与实际请求仍可能有细微差异。
- Eta-por 的 `AgentPromptBuilder` 正文与上游差异较大（模型身份、工具优先级、记忆规则等表述都改过）。本次只移植了 `customization` 机制，**正文用上游版**。如果要用 Eta-por 的正文表述，需要逐段对比替换，风险较高，未做。
- `PromptIntegrityGuard` 依赖的 Xposed 相关锚点检查在本 fork 的模块结构下可能部分失效，未逐项验证。
