# fork 补丁与构建流程

本文件记录本 fork 相对上游 `Mangi-11/Eta` 的改动范围、构建发版流程与常见坑。
配合 `docs/CHANGELOG-FORK.md`（逐版本开发日志）使用。

版本号规则：`versionName = 3.4.4-fork.N`，`versionCode = 2026112100 + N`，
由 CI 的 `-Peta.forkVersion=N` 传入。

---

## 一、改动范围

### 新增文件（不受上游同步影响）

```
app/src/main/kotlin/io/github/mangi/eta/agent/prompt/            提示词与工具边界（8 文件）
app/src/main/kotlin/io/github/mangi/eta/agent/github/            GitHub 账号（3 文件）
app/src/main/kotlin/io/github/mangi/eta/agent/security/          PromptIntegrityGuard
app/src/main/kotlin/io/github/mangi/eta/agent/automation/        自动备份（3 文件）
app/src/main/kotlin/io/github/mangi/eta/agent/voice/local/       离线语音引擎（22 文件）
app/src/main/kotlin/io/github/mangi/eta/agent/voice/qwen/        WarmCachePolicy
app/src/main/kotlin/io/github/mangi/eta/agent/voice/SystemSpeechPlayback.kt
app/src/main/kotlin/io/github/mangi/eta/agent/model/AgentToolDirectory.kt
app/src/main/kotlin/io/github/mangi/eta/agent/model/LocalToolProfile(Store).kt
app/src/main/kotlin/io/github/mangi/eta/agent/model/AgentRequestSnapshot.kt
app/src/main/kotlin/io/github/mangi/eta/agent/model/AgentSnapshotAnalysis.kt
app/src/main/kotlin/io/github/mangi/eta/agent/tool/AgentToolNaming.kt
app/src/main/kotlin/io/github/mangi/eta/data/repository/GitHubAccountStore.kt
app/src/main/kotlin/io/github/mangi/eta/data/repository/AutoBackupRunner.kt
app/src/main/kotlin/io/github/mangi/eta/data/repository/EtaFullBackup*.kt
app/src/main/kotlin/io/github/mangi/eta/data/model/AutoBackupSettings.kt
app/src/main/kotlin/io/github/mangi/eta/ui/screens/prompts/       提示词 Studio（16 文件）
app/src/main/kotlin/io/github/mangi/eta/ui/screens/github/        GitHub 账号页
app/src/main/kotlin/io/github/mangi/eta/ui/screens/categories/    会话分类整页
app/src/main/kotlin/io/github/mangi/eta/ui/components/Eta*.kt     8 个偏好组件
app/src/main/kotlin/io/github/mangi/eta/ui/components/CapabilityCapsule.kt
app/src/main/kotlin/io/github/mangi/eta/ui/components/ChatTurnNavigator.kt
app/src/main/kotlin/io/github/mangi/eta/agent/roleplay/CharacterAvatarKind.kt  头像类型识别（魔数）
app/src/main/kotlin/io/github/mangi/eta/ui/components/CharacterAnimatedAvatar.kt 动图/视频头像播放层
app/src/main/kotlin/io/github/mangi/eta/ui/components/OpaqueSurfaces.kt        弹窗不透明底色（主题合成）
scripts/check-theme-coverage.py                                   主题覆盖检查（明暗 + Monet + import）
app/src/main/assets/prompt_presets/                              7 个预设
app/src/main/assets/local_speech/                                模型清单 + 许可证
app/src/main/jniLibs/arm64-v8a/                                  7 个离线语音 so
app/src/main/kotlin/com/k2fsa/sherpa/onnx/                       sherpa-onnx binding（5 文件）
```

### 修改文件（上游同步时会冲突，重点看这几处）

| 文件 | 改动要点 |
|---|---|
| `AgentRuntimeWire.kt` | 新增 `MSG_PAUSE` / `MSG_RESUME` |
| `AgentRuntimeService.kt` | 处理暂停/恢复消息；发布执行状态时带主运行 id 与暂停态 |
| `AgentRuntimeClient.kt` | `pauseRun` / `resumeRun` |
| `AgentRunController.kt` | `awaitIfPaused()`（只在暂停时阻塞，不抛异常） |
| `AgentLoop.kt` | `onProviderEvent` 入口调 `awaitIfPaused()`，流式期间也能停 |
| `AgentModelClient.kt` | run 起点读一次 `AgentToolPolicy`；请求侧过滤 + 执行侧 `allows()` 兜底 |
| `AgentPromptBuilder.kt` | 融合 `PromptCustomization`；纯聊天档独立分支；技能索引只在 FULL 档 |
| `AgentExecutionService.kt` | 通知动作：暂停·继续 / 插入 / 停止（最多 3 个） |
| `AgentAppState.kt` | 暂停/恢复/插入、会话分类 9 个方法、单条删除、角色新建、自动绑卡 |
| `AgentConversationStore.kt` | 整行 upsert 回填 `category_id`；消息 `created_at` 读写 |
| `AgentConversationRevisionReducer.kt` | `deleteSingle`（只删目标 + 清孤立 tool 结果） |
| `ConversationPanePanel.kt` | 工作台入口、分类管理、多选批量、时间行、对话框 |
| `ConversationPaneChrome.kt` | 顶栏动作键、分类入口 |
| `ConversationSidePaneScaffold.kt` / `AgentAppShell.kt` | 三层回调透传 |
| `AgentChatBody.kt` | 轮次横条挂到 bottomBar；暂停/插入三层透传；时间戳 |
| `AgentChatInputBar.kt` | 暂停/继续键；能力胶囊常驻 |
| `ChatMessageItem.kt` | 每轮时间 + 删除键 |
| `AgentChatQueueStrip.kt` | 排队条外观（14dp 圆角 + 描边） |
| `ConversationTimeLabels.kt` | `rowLabel`（M月d日 HH:mm，跨年补年份） |
| `ConversationUiState.kt` | `categories` / `categoryFilterId` / `categoryId` |
| `AgentChatUiState.kt` | `isRunPaused`；`UserMessageUi.createdAtMillis` |
| `AppRoute.kt` | 13 条提示词/GitHub 路由 + 会话分类路由 |
| `AgentAppRoot.kt` | 对应 `entry<>` 与入口接线 |
| `SettingsScreen.kt` | GitHub / 提示词与工具边界入口；关于页链接指向本仓库 |
| `DataBackupScreen.kt` | 自动备份 UI（开关/间隔/份数/SAF/格式/历史） |
| `AppUpdateChecker.kt` | 版本核心剥离 `-fork.N`/`+提交号`；同上游版本比 fork 序号；Release 指向本仓库 |
| `SpeechSettings.kt` | `SystemTtsConfig`；`AsrProvider` 补 LOCAL 系；`TtsProvider` 补 SYSTEM |
| `build.gradle.kts` | `vosk-android`；`versionName`/`versionCode` 走 forkVersion |
| `AndroidManifest.xml` | `:local_speech` 独立进程 |
| `gradle/libs.versions.toml` | 与上游一致（同步时不要覆盖成旧值） |

---

## 二、构建与发版流程

### 1. 工作区与稀疏检出

```bash
cd /workspace/eta-up          # rebase 分支，基于上游 main
git sparse-checkout add app/src/main/jniLibs app/src/main/assets/local_speech \
    app/src/test docs
```

漏掉任一路径，`git add` 会**静默跳过**（提示 "outside of your sparse-checkout definition"）。

### 2. 提交与推送

```bash
git add -A
git -c user.name=Mari0us -c user.email=mariusarva@users.noreply.github.com commit -m "..."
git push fork rebase
```

推送偶尔报 `Recv failure: Software caused connection abort` 或远端报
`not our ref <sha>`。用循环 + 远端校验：

```bash
for i in 1 2 3 4 5; do
  git push fork rebase 2>&1 | tail -1
  if git ls-remote fork rebase | grep -q "$(git rev-parse HEAD)"; then echo PUSHED; break; fi
  sleep 6
done
```

### 3. 触发 CI

主仓库 Actions 被平台禁用，统一走 `mariusarva/eta-ci`：

```bash
gh workflow run build.yml --repo mariusarva/eta-ci \
  -f ref=<完整 40 位 SHA> -f forkVersion=<N>
```

`ref` 必须是已推送的完整 SHA；短 SHA 或分支名会 fetch 失败。

### 4. 下载产物

GitHub artifact 直连经常被中断，用分段并行下载脚本（`/workspace/dl2.py`）：

```bash
ID=$(gh api repos/mariusarva/eta-ci/actions/runs/<run_id>/artifacts --jq '.artifacts[0].id')
python3 dl2.py $ID /workspace/artNN.zip
```

脚本先换签名 URL，再 8 段 Range 并行、每段断点续传重试 40 次。

### 5. 安装与发布

```bash
cp Eta-v3.4.4-fork.N+<sha>.apk /storage/emulated/0/Download/Eta-fork.N.apk
```

安装必须用 `/data/local/tmp/` 路径，**不能用 `/storage/...`**（system_server 读 fuse 被
SELinux 拒绝）：

```bash
pm install -r -d /data/local/tmp/eta-forkN.apk
```

发 Release：`--target` 必须完整 40 位 SHA；**tag 只能用一次**（撞了就得换序号）。

---

## 三、常见坑

| 现象 | 原因与处理 |
|---|---|
| `CouiPreferenceGroup` 里直接放 `CouiPreference` 报「@Composable invocations can only happen…」 | content 是 `CardListGroupScope.()`，子项必须包 `item {}` |
| 跨文件用 `SettingsIconColors` 报 private | 它在 `SettingsScreen.kt` 里是 private，自己定义颜色常量 |
| MIUIX 分支没有 `IconButton` | 用 `GlassIconButton` 或自绘 Box |
| `updateAutoBackupSettings` 赋值类型不匹配 | 返回 `Unit`，写入后要重新读一次 |
| 通知卡按钮压住图标 | action 超过 3 个会被裁；同时只有标题的紧凑卡会重叠，补 `setContentText` |
| 弹窗文字糊在一起 | Miuix 的 `surface`/`surfaceContainer` 深色下带 alpha，用绝对色 |
| 侧栏里 GlassDialog 点了没反应 | 200dp 侧栏内被裁切，改用 `Dialog` 窗口 |
| 分类被清空 | 整行 `@Upsert` 未带 `category_id` 会写成 NULL，保存时要回填 |
| 能力胶囊技能数永远 N/N | `listInstalledSkills()` 已过滤 enabled，统计要用 `listSkillsForManagement()` |
| 轮次横条看不见 | 放进 Box 需显式 `align(BottomCenter)`；更稳的是挂到 Scaffold `bottomBar` |
| 离线语音模型列表为空 | `assets/local_speech/catalog.json` 没打包，`LocalSpeechRegistry` 读不到 |
| CI 报 `not our ref` | push 未完成就触发；用 `git ls-remote` 校验后再跑 |
| `CharacterProfile` 找不到 | 包路径是 `io.github.mangi.eta.agent.roleplay`，不是 `data.model` |
| 角色选择器首次打开空白，进一次角色库后正常 | `CharacterRepository` 的 context 只在进角色库时绑定，`list()` 抛异常被 `getOrDefault` 吞掉。已在 `EtaApp.onCreate()` 里 initialize |
| `GetContent` 传 MIME 数组报 `Array<String>` 类型不匹配 | 它的 input 是单个 `String`；要多 MIME 用 `OpenDocument` |
| `LaunchedEffect` 里读 `CompositionLocal` 报 @Composable 错误 | 先在组合作用域取出成局部变量再传进去 |
| public 函数报 `exposes its 'internal' parameter type` | 参数类型是 internal 时，函数也要 `internal` |
| 注释结尾 `*\/` 与下一行粘成 `*\/@Composable` | 注释不闭合会把后面整段吞掉，报一串 Unresolved reference；改完用括号/注释配平脚本过一遍 |
| 下载产物报 `Errno 103 connection abort` | 8 段并行重试会卡死，改单线程 `curl -L --retry 20 --retry-all-errors` |
| 本地离线语音模型不能朗读 | 那 8 个模型全是 ASR（`qwen3_asr`\/`sensevoice`\/`vosk`），没有 TTS；离线朗读只能用系统 TTS 引擎音色 |
| 角色音色换全局服务商后失效 | 音色 id 是各家自己的标识；角色卡要同时记 `eta_tts_provider` |
| 某页面在深色模式整页浅色 | 该页只写了 COUI 版式但没拿到 `COUITheme`，`COUITheme.colorScheme` 回落到 `LocalColors` 默认值 `lightColorScheme()` |
| MIUIX 样式下某页没有 Monet 取色 | 同上：该页只渲染 COUI 组件，从不读 `MiuixTheme`。需补 MIUIX 分支 |
| COUI 想接 Monet 报 `colorsFromSeed is internal` | COUI 只暴露 `ThemeController`，`colorsFromSeed`/`monetSystemColors` 都是 internal |
| R8 报 `Missing class com.materialkolor.scheme.DynamicScheme` | COUI 要 material-color-utilities 5.0.0（`scheme.*`），Miuix 要 6.0.0（`dynamiccolor.*`），不能共存；升级任一库前先对齐版本 |
| 弹窗底色写死 `#FFFFFF` / `#2A2A2E` 后不跟 Monet | 用 `opaqueThemedSurface()`：把主题容器色按自身 alpha 合成到 `background`，不透明且保留色相 |
| 补版式后 CI 报一批 `Unresolved reference` | 多半是漏 import（`Modifier`/`dp`/`padding`）。跑 `scripts/check-theme-coverage.py` 的 E4 规则先自查 |
| 拆组件后点把手面板不出现 | 把手与面板各持一份 `expanded`；用共享的 `ChatTurnNavigatorState` |
| 面板被压成 1/3 宽 | 它被放进了 `weight(1f)` 槽位；面板要整行宽就不能和把手共用 Row 槽位 |
| 下载产物卡在 `Errno 103` 或速度归零 | 改用 `gh run download <run-id> -R <repo> -D <dir>`，走发布通道通常十秒完成 |

---

## 四、上游同步步骤

```bash
cd /workspace/eta-up
git fetch origin main --depth=60
git diff --name-only <上次同步点>..origin/main      # 先看影响面
git stash push -u -m "sync-prep"
git merge --no-commit --no-ff origin/main
# 解冲突：多数是纯 import 段，两边都保留
git add -A && git commit -m "merge: 同步上游 main（<旧> → <新>）"
git stash pop
```

冲突集中在 `AgentChatBody` / `AgentChatInputBar` / `ConversationPaneChrome` /
`ConversationSidePaneScaffold`，因为 fork 在这几处都改过。新增文件不受影响。

**注意**：不要用旧 tag 的文件覆盖 main 版本。上游会删代码，旧文件覆盖会导致大量
`Unresolved reference`。补丁必须以 main 为底稿。
