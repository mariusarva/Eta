# Eta Fork 开发记录（mariusarva/Eta）

本 fork 基于上游 [Mangi-11/Eta](https://github.com/Mangi-11/Eta) 的 `main` 分支，补回并新增以下能力，用于自用构建。

## 构建方式

上游 `.github/workflows/android-release.yml` 的 `build-apks` 第一步就校验四个签名 Secrets（`ETA_RELEASE_KEYSTORE_BASE64` 等），缺失直接 `exit 1`，连 debug APK 都不留。fork 没有这些 Secrets。

本 fork 新增 `.github/workflows/fork-build.yml`：

- 只跑 `:app:assembleDebug`，用 debug keystore 自动签名
- 产物直接 `upload-artifact`，不碰签名 Secrets
- `workflow_dispatch` 手动触发，或 push 到 main 自动触发

上游 `android-release.yml` 与 `pr-check.yml` 已在仓库设置中 `disabled_manually`，避免每次 push 白烧 Actions 分钟数。

### 构建环境

| 项 | 版本 |
|---|---|
| JDK | 25（temurin） |
| Gradle | 9.7.1（wrapper） |
| AGP | 9.5.1 |
| Kotlin | 2.4.21 |
| compileSdk / targetSdk | 37（`sdkmanager --channel=3 "platforms;android-37.0"`） |
| minSdk | 33 |

### 安装

产物是 debug 签名，与官方包不同。本机装了 CorePatch（无视签名校验），可直接 `pm install -r -d` 覆盖安装，数据保留。无 CorePatch 的设备必须卸载重装并重新灌数据。

## 功能一：提示词系统

> 完整移植记录（逻辑层 / 组件层 / UI 层 / 接入点 / 踩坑）见 [PROMPT_SYSTEM.md](PROMPT_SYSTEM.md)。
> 下面只保留摘要。

上游把系统提示词挂在 `model_providers.system_prompt`（per-provider），没有全局人格层。

### 逻辑层（零改动移植）

`app/src/main/kotlin/io/github/mangi/eta/agent/prompt/` 下 8 个文件，共 942 行，全部只依赖 `io.github.mangi.eta.config.Prefs`：

| 文件 | 行数 |
|---|---|
| `PromptCustomization.kt` | 265 |
| `PromptConflictAnalyzer.kt` | 230 |
| `PromptPreset.kt` | 150 |
| `PromptPresetCodec.kt` | 128 |
| `AgentToolPolicy.kt` | 110 |
| `PromptPresetRepository.kt` | 105 |
| `ToolGroupStore.kt` | 74 |
| `PromptCustomizationStore.kt` | 48 |

配套资源：`app/src/main/assets/prompt_presets/` 7 个 json（default / concise / rigorous / technical / coding / research / life）。

### 界面层

`ui/screens/prompts/PromptPresetsScreen.kt`（新写，COUI 风格）。布局对齐官方 `WebToolsSettingsScreen` / `SubagentSettingsScreen`：

```
提示词预设                        ← CouiScaffoldPage
┌─ 当前生效 ─────────────────┐
│ 系统提示词   首行摘要 · N 字 │   ← 读第一个已启用 provider 的实际值
└───────────────────────────┘
┌─ 内置预设 ─────────────────┐
│ 系统默认（Eta 原始）         │
│ ─────────────────────────  │
│ 简洁 · 先给结论              │
│ ...                        │
└───────────────────────────┘
┌─ 导入的预设 ───────────────┐   ← 有导入才显示
└───────────────────────────┘
应用规则（说明）
```

使用 `com.itos.coui.layout.Group` 分组、组内 `Divider()` 分隔、`CouiPageBottomSpacer()` 收尾。

**行为**：点击任意预设 → 合成 `base + persona + append` → 写入**所有已启用 provider** 的 `system_prompt` 列 → toast 报告写入数量 → 「当前生效」摘要立即刷新。

**关键点**：空预设不跳过。`default.json` 的 `base_system_prompt` 填的是官方内置提示词原文（87 字），点它即恢复官方默认，而不是清空。

### 接线

- `ui/navigation/AppRoute.kt`：新增 `data object PromptPresets : AppRoute`
- `ui/app/AgentAppRoot.kt`：`entry<AppRoute.PromptPresets>` 挂页面
- `ui/app/AgentAppShell.kt`：`titleForRoute` 补 `is AppRoute.PromptPresets -> "提示词预设"`
- `ui/SettingsScreen.kt`：设置页加入口，图标 `Icons.Rounded.TextFields` + `SettingsIconColors.Yellow`

## 功能二：GitHub 账号

上游完全没有此功能（dex 里 `eta_github_account` 与相关类 0 命中）。

### 逻辑层（4 文件，依赖全部对得上上游）

| 文件 | 行数 | 说明 |
|---|---|---|
| `agent/github/GitHubAccountManager.kt` | 210 | 状态机 + 自动注册 GitHub 官方远程 MCP（`https://api.githubcopilot.com/mcp/`） |
| `agent/github/GitHubDeviceFlow.kt` | 129 | 设备码授权，client_id 用 GitHub CLI 公开应用，无需回调地址与 client secret |
| `agent/github/GitHubTerminalSync.kt` | 109 | 写 `~/.git-credentials` 与 `~/.config/gh/hosts.yml`，走 root 写 chroot |
| `data/repository/GitHubAccountStore.kt` | 114 | 凭据存 Android Keystore（AES/GCM），密文进 `eta_github_account` prefs |

依赖核对：`BoundedRootCommandExecutor`、`LinuxDistribution`、`LinuxEnvironmentPaths`、`McpServerRepository`、`AndroidAgentLogger` 上游 v3.4.4 全部存在，**零改动**。

授权范围：`repo read:org workflow gist`。

### 界面层

`ui/screens/github/GitHubAccountScreen.kt`（新写，COUI 风格），按状态分组：

```
未连接：  账号（UserCircle 图标，点击授权） / 说明（授权范围）
等待中：  验证码（大字，点击复制） / 操作（取消） / 说明（下一步）
已连接：  账号 / 终端同步（开关 + 已同步 + 等待环境就绪）/ MCP / 危险操作（断开）
失败：    账号（原因 + 重试）/ 说明（常见原因）
```

图标用 `CouiIcons.Filled.UserCircle`——coui-icons 只有 25 个 Outlined、37 个 Filled，没有 GitHub 图标。

### 接线

- `AppRoute`：`data object GitHubAccount`
- `AgentAppRoot`：`entry<AppRoute.GitHubAccount>`
- `AgentAppShell`：标题 `"GitHub 账号"`
- `SettingsScreen`：入口图标 `Icons.Rounded.AccountCircle` + `SettingsIconColors.Blue`

### 已知限制

Keystore 按 UID 隔离，密钥材料在 TEE 里。旧包（`io.github.mangi.eta.beta`，UID 10409）的凭据密文复制到新包（UID 10389）后**无法解密**——两个包各自有独立的 `eta_github_account_key` keyentry。跨包迁移只能在新包内重新走一次设备码授权。

## 功能三：暂停 / 继续

上游 `AgentRunController` 已有 `pause()` / `resume()` / `pauseCondition.await()` 完整实现，但**全仓没有任何调用点**（只有两处读 `isPaused`）。本 fork 负责接线。

### 协议层

`agent/runtime/AgentRuntimeWire.kt`：

```kotlin
/** client -> service：暂停当前运行（工作线程在检查点挂起，可恢复）。 */
const val MSG_PAUSE = 20

/** client -> service：恢复被暂停的运行。 */
const val MSG_RESUME = 21
```

### 服务端

`AgentRuntimeService.kt`：

- `when (msg.what)` 新增两个分支
- `pauseRun(runId)`：优先 `guiControl.pauseForUser(runId)`；无 GUI 所有者时直接 `run.session.controller.pause()`。投影 `AgentOverlayStatus.Paused`，更新 `keepScreenOn` 与执行状态
- `resumeRun()`：`guiControl.resumeForUser()`，失败则遍历所有非终态运行逐个 `resume()`

### 客户端

`AgentRuntimeClient.kt`：`pauseRun(runId)` / `resumeRun()`，走 `withRuntimeMessenger`，不等待回执。

### 状态层

`AgentAppState.kt`：

```kotlin
fun pauseCurrentRun() {
    val runId = runStore.activeRunId(selectedConversationId) ?: return
    // 本地立刻置位：服务端只在没有活跃动作时才回推 GuiControlChanged，
    // 工具执行期间等不到事件，按钮会一直停在暂停态导致无法再点。
    setRunPaused(runId, paused = true)
    scope.launch(Dispatchers.IO) { AgentRuntimeClient(...).pauseRun(runId) }
}
```

`setRunPaused(runId, paused)` 把标记写到 run 所属会话的 `AgentChatUiState.isRunPaused`。

### 检查点补强

上游 `throwIfCancelled()` 的调用点只在每轮 LLM 请求之间和工具调用之前，**流式回调里一个都没有**。模型输出 token 时点暂停，要等整段回答跑完才生效。

修复两处：

1. `AgentRunController` 新增 `awaitIfPaused()`——仅在暂停时阻塞，不抛异常，供流式回调等不能抛异常的位置使用。**必须始终在锁内读 `paused`**，因为它不是 `volatile`，锁外读可能拿到旧值直接放行。
2. `AgentLoop.kt` 在 `providerEvent` 分发前插入 `runController.awaitIfPaused()`。暂停后新的 token 不再被消费。

### UI

`AgentChatInputBar.kt` 新增两个按钮（在 `Spacer(weight=1f)` 之后、发送键之前）：

| 按钮 | 图标 | 显示条件 | 行为 |
|---|---|---|---|
| 暂停 / 继续 | `Pause` / `PlayArrow` | `isStreaming && !isCompacting` | 切换暂停状态 |
| 插入 | `Reply` | `isStreaming && !isCompacting && canSend` | 注入补充指令 |

尺寸：图标 20dp + `Modifier.scale(1.35f)`。Material 的 `Stop` 字形只占 24dp 视口约一半，放大盒子无效，必须放大字形本身。按钮间距 10dp。

### 关键坑：三层同名参数

`AgentChatBody.kt` 里有**三层**同名参数，每层都要补：

```
AgentChatBody          ← 顶层，向 Scaffold 传参
  AgentChatScaffold    ← 中间层
    AgentChatBottomBar ← 向 InputBar 传参
      AgentChatInputBar
```

`onPause` / `onResume` / `onInject` / `isRunPaused` 都有默认值（`{}` / `false`），所以**漏传任何一层都能编译通过、运行不报错，但点击执行的是空 lambda**。排查时表现为「按钮点了没反应，日志里一条都没有」。

诊断方法：在 `pauseCurrentRun()` 和 `pauseRun()` 各打一条日志。

- 两条都没有 → 顶层透传漏了
- 只有 UI 那条且 `runId=null` → `activeRunId()` 找不到运行
- 只有 UI 那条但有 runId → Messenger 绑定问题

## 功能四：运行中注入补充指令

上游已有完整 steer 通道，但入口隐蔽：**长按发送键**才插入，点发送只是排队。

链路：`MSG_STEER` → `steerRun()` → `run.supplement(text)` → `AgentLoop.appendPendingSteeringMessage()`。

新增显式按钮后：

1. `AgentUiActions` 加 `data object InjectIntoRun : AgentHomeAction`
2. `AgentAppState.injectIntoRun()`：清空输入框 → 构造 `QueuedMessageUi(immediateInterrupt = true)` → `steerToActiveRun()`
3. 无运行中任务时退化为普通发送，避免点了没反应

## 已知未做

### 工具执行期间暂停不即时

`toolExecutor.execute()` 是阻塞调用。暂停只置标志，检查点在工具调用前后，所以 terminal 命令跑 180 秒期间点暂停要等它结束。

**这是有意为之，不是缺陷。** 暂停语义是「停在这里稍后从这点继续」，杀掉正在跑的命令会让「从这点继续」失效，且留下半完成状态（副作用已发生、结果未返回），模型拿到残缺上下文比等它跑完更危险。

需要立刻中断时用停止键——它已经覆盖这个场景：

```kotlin
toolExecutor = routingExecutor
toolsBinding = runController.register(routingExecutor::close)
```

`BoundedRootCommandExecutor` 内部维护 `activeProcesses`，`close()` 会 `destroy()` 所有子进程。

若后续要改，方向是给 UI 加反馈：工具执行中且已请求暂停时显示「等待当前步骤结束」。

## 上游同步注意

上游 `main` 会持续前进。本 fork 的改动集中在：

- 新增文件：`agent/prompt/`（8）、`agent/github/`（4）、`ui/screens/prompts/`、`ui/screens/github/`、`assets/prompt_presets/`（7）、`.github/workflows/fork-build.yml`
- 修改文件：`AgentRuntimeWire.kt`、`AgentRuntimeService.kt`、`AgentRuntimeClient.kt`、`AgentRunController.kt`、`AgentLoop.kt`、`AgentAppState.kt`、`AgentUiActions.kt`、`AgentAppRoot.kt`、`AgentAppShell.kt`、`AgentHomeScreen.kt`、`AgentChatBody.kt`、`AgentChatInputBar.kt`、`SettingsScreen.kt`、`AppRoute.kt`

拉上游时冲突会集中在后一组。新增文件不受影响。

**踩过的坑**：不要用旧 tag 的文件覆盖 main 版本。上游在 11-18 的 main 里删掉了 systemizer、厂商助理等一批代码，用 v3.4.4 的 `SettingsScreen.kt` 覆盖会导致大量 `Unresolved reference`。补丁必须以 main 为底稿。

## 离线语音（本 fork 增补，2026-11-19）

上游只提供云端 ASR/TTS。本 fork 引入 Time-IU 的三引擎离线链路，并解决本机网络拿不到模型的问题。

**引擎与依赖**

| 引擎 | 原生库 | 模型来源 |
| --- | --- | --- |
| GGUF（Qwen3-ASR / SenseVoice） | `libeta_transcribe_jni.so` | Release 镜像 |
| SenseVoice ONNX | `libsherpa-onnx-jni.so` + `com.k2fsa.sherpa.onnx` | Release 镜像 |
| Vosk 流式 | `libvosk.so`（JNA）+ Maven `com.alphacephei:vosk-android` | alphacephei 可直连 |

**三个必须知道的坑**

1. `com.k2fsa.sherpa.onnx` 的 Java 类**不能**用 jadx 反编译产物：Kotlin data class 反编译后会生成与 property getter 同名的重复方法（实测 100 处 `already defined`），javac 直接失败。用 k2-fsa/sherpa-onnx 官方 `sherpa-onnx/kotlin-api/` 源码（`OfflineRecognizer.kt` 内含全部 config 类 + `OfflineStream.kt` / `FeatureConfig.kt` / `HomophoneReplacerConfig.kt` / `QnnConfig.kt`）。已逐类核对字段集合是 .so 的 JNI `GetFieldID` 需要的超集。
2. JNI 按名访问，release 变体必须 keep：`GgufSpeechNative`、`com.k2fsa.sherpa.onnx.**`、`org.vosk.**`、`com.sun.jna.**`，否则 R8 改名后 `UnsatisfiedLinkError`。
3. `LocalSpeechModelStore` 要求 `Application.getProcessName() == "$packageName:local_speech"`，native 加载、解码、释放只能发生在独立进程。

**模型下载**

huggingface.co 在本机网络不可达，`hf-mirror.com` 对这批仓库一律 308 回源，等于不可用。做法是 `.github/workflows/model-mirror.yml` 在 runner 上下载 catalog 里 7 个固定版本文件、按 sha256 校验后发布到 Release `local-models-v1`；App 下载优先走
`https://github.com/mariusarva/Eta/releases/download/local-models-v1/<name>`，失败再回退官方地址。摘要校验仍在安装阶段全量执行，镜像内容不对会被拒。

**断点续传**

- `readTimeout` 5 秒会让百 MB 下载在抖动时反复中断，改为 30 秒。
- stage 目录从 `.stage-<id>-<uuid>` 改为固定的 `.stage-<id>`：取消、进程被回收后重新点下载会从已落盘字节继续（`Range: bytes=N-`，服务器返回 200 时自动从头重写）。
- 只在校验失败时清掉 stage；网络中断保留，避免「下次又从 0 开始」。

**卡顿相关的两个事实**

- 帧数据（`dumpsys gfxinfo`）在 release 包上 50th 12–14 ms、99th 34–44 ms、现代 janky < 0.6%；debug 包的 `legacy janky` 高达 20–38%，因为 120 Hz 屏的 8.3 ms 阈值下 debug 构建必超。用户主观的「卡」主要来自 debug 变体，不是业务代码死循环。
- 非 Vosk 模型原先每次 `inspect` / 冷加载都全量 SHA（单文件最大 590 MB）。现在 8 个模型统一走目录指纹缓存（inode / ctime / mtime / nanos），文件被替换即失效。

**原生库清单与依赖（踩过的坑）**

`jniLibs/arm64-v8a/` 必须同时包含 `libc++_shared.so`（1.4 MB）：`libeta_transcribe_jni.so` 的
`DT_NEEDED` 里有它，缺了会在 `System.loadLibrary` 阶段报
`dlopen failed: library "libc++_shared.so" not found`，服务端把 `LinkageError` 翻成
「本机离线原生库不兼容」。其它 so 的依赖都已就位：`libsherpa-onnx-jni.so` → `libonnxruntime.so`，
`libsherpa-ncnn-jni.so` → `libncnn.so`，其余只依赖系统库。

另一个同类坑：catalog 引用的许可文件必须真的在 `assets/local_speech/licenses/` 里。
`writeMetadata()` 原先直接 `assets.open()`，缺文件时整个 install 落到 STORAGE 失败分支，
表现为「GGUF/ONNX 一直待检测、Vosk 能装」——因为只有 Vosk 那份许可存在。现已补齐，
并且缺许可时降级为空文件而不是让安装失败。
