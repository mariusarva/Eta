# Eta fork 开发日志

记录本 fork 相对上游 `Mangi-11/Eta` 的全部改动。每条按「现象 → 定位 → 改动」写，
失败的尝试也保留，便于回溯。时间线倒序，最新的在最上面。

版本号规则：`versionName = 3.4.4-fork.N`，`versionCode = 2026112100 + N`。
签名固定（RSA 4096，见 `mariusarva/eta-signing`）。

---

## fork.102（2026-11-24）备份报「History payload chunk missing」

**现象**：手动导出 / 自动备份直接失败，提示 `History payload chunk missing`。

**根因**：大字段被拆成 16384 字符的块存进 `agent_text_chunks`，正文列只留 `@eta:chunks:v1:<块数>:<总长>` 引用。读取侧（`ChunkedTextDao.restoreText` 与 `ChunkedTextInputStream`）对分块做的是**硬断言** `check(...)`：只要有一条引用找不到对应分块、块序不符或长度偏差，就抛异常，整次导出中断。

真机上查到的实际情况：71 条分块引用里 1 条悬空——

```
user-run-659b951a-0bf1-4047-ba14-359ea3346c67
images_json = @eta:chunks:v1:10:157989
→ 声明 10 块 / 157989 字符，agent_text_chunks 里 0 块
```

其余 60 条 `images` 引用与分块数量完全匹配；4 条 checkpoint 有轻微长度偏差（声明 2464878、实际 2464876，`storeText` 拆 UTF-16 代理对时少算），不致命。

**改法**：

- `restoreText`：引用格式非法返回空串而不是抛异常；读到缺失 / 块序不符 / 越界时提前收尾，返回已读到的部分
- `ChunkedTextInputStream.ensureChunk`：同样去掉断言，分块不足或越界就结束流
- 就地清理了那条悬空引用（`images_json` 置 `[]`），清理前整库备份到 `/data/local/tmp/eta-before-fix.db`

**取舍**：个别字段内容丢失，好过整次备份失败。备份本身就是为了保数据，不能因为一条坏记录而完全不产出。

---

## fork.96 – fork.101（2026-11-24）技能市场：合入、修安装失败、加 GitHub 搜索

### fork.96：技能市场合入 rebase（保持 schema v31）

原 `feat/skill-market` 分支基于 v30 时代，落后 rebase 38 个提交。从它直接出包（fork.97）会出大问题：

**现象**：装到已装 fork.96（v31）的设备上，数据全丢。

**根因**：`feat/skill-market` 的 `EtaDatabase.version = 30`，缺 `MIGRATION_30_31`。Room 检测到数据库从 v31 降到 v30，判定为降级，`fallbackToDestructiveMigration(dropAllTables = true)` 直接清空所有表。签名、包名、versionCode 都没问题，不是覆盖安装冲突。

**改法**：不回退版本，把技能市场 cherry-pick 到 `rebase`（v31）之上，只取 UI/资源改动，数据库版本保持 v31。

合入时连带修了三处阻断编译的问题：

| 问题 | 改法 |
|---|---|
| `SkillMarketScreen.kt` 用 `com.itos.coui` 旧包名 + `CouiChipTabs` / `Group` | 改新包 `io.github.suqi8.coui.kmp`，适配 `CouiTextField(state=)` / `CouiPreferenceGroup` + `item` |
| `strings.xml` 与 `strings_fork_prompt_github.xml` 有 399 个重复键 | 从 `strings.xml` 移除（1867 → 1468），`packageReleaseResources` 不再报 Duplicate resources |
| `AgentSkillsScreen.kt` 的 `Group(title=...)` 未解析 | 改 `CouiPreferenceGroup` + `item(key)` |

### fork.98 – fork.99：安装失败「Skill description 必填且不能超过 1024 字符」

**现象**：点「PDF 处理」安装，弹窗报 description 超限，但 pdf 的 description 只有 437 字符。

**根因**：`SkillPackageInstaller.discoverCandidates` 对仓库里**每一个** `SKILL.md` 都调 `parseAndValidateSkill`。`anthropics/skills` 里 `claude-api` 的 description 是 1068 字符，超 1024 上限，直接抛 `INVALID_SKILL`，导致装任何技能都失败——一个坏技能拖垮整个仓库。

**改法**：

- `discoverCandidates` 改 `mapNotNull` + `runCatching`，单个不合规的 SKILL.md 跳过，不影响其他技能
- 选中路径若不在候选里，单独再解析一次，把真实原因原样报出来，而不是笼统说「路径无效」
- `SkillMarketScreen` 的 `CouiNoticeDialog` 从 `CouiScaffoldPage` 外部移进 `overlay` 槽（与其他页面一致）
- 安装流程补 start / downloaded / result / throw 四条日志

### fork.100 – fork.101：GitHub 搜索技能

新增 `GitHubSkillSearch`，五条途径：

| 途径 | 需要登录 | 精度 |
|---|---|---|
| 内置目录 `skill_market.json` | 否 | 直接可装 |
| `/search/code?q=filename:SKILL.md` | **是**（GitHub 强制） | 精确到技能目录 |
| `/search/repositories?q=xxx skill` | 否 | 仓库级 |
| `/search/repositories?q=topic:claude-skills` 等 3 个话题 | 否 | 仓库级 |
| `/repos/{r}/git/trees/{ref}?recursive=1` | 否 | 精确到技能目录 |

有 token 时优先代码搜索；没 token 走仓库搜索 + 话题聚合，再点条目「展开」用 git/trees 把仓库拆成具体技能。

仓库搜索结果只知道仓库名（`path = "."`），`SkillPackageInstaller` 为此加了自动模式：路径为 `.` 且仓库里恰好一个技能就直接装，多个则报 `MULTIPLE_SKILLS_FOUND`。

**编译错误**（两次）：

- `buildList { ... ::add }` 用方法引用推断不出元素类型 → 五处 `buildList` 显式标注 `<SkillMarketEntry>` / `<String>`
- 新增 `Mode.TOPIC` 后 `when (searchMode)` 不再穷尽 → 补 `TOPIC` 分支

---

## fork.91 – fork.92（2026-11-24）工作台提示词入口 / 通知暂停失效

### fork.91：工作台「工具能力」→「提示词设置」

侧栏工作台那一行原来指向 `AppRoute.Tools`，改为 `AppRoute.PromptStudio`。回调名 `onOpenTools`
同步改为 `onOpenPromptStudio`，涉及 `ConversationPanePanel`、`ConversationSidePaneScaffold`、
`AgentAppShell`、`AgentAppRoot` 四处。工具能力页本身保留，设置页里的入口没动。

### fork.91：通知暂停后不刷新

`AgentExecutionService.updateRunStatus` 的 `publishedSignature` 去重串里**没有 `isPaused`**。
暂停只改按钮文案（暂停↔继续），标题和任务计数都不变，签名相同 → 通知不重建 → 按钮一直停在旧文案。
补上 `isPaused` 与 `primaryRunId`。

### fork.92：通知暂停指令根本没送达（真 bug）

现象：点通知上的「暂停」，运行没有暂停，继续跑完，看起来像「被中断」。

根因链条：

1. 通知按钮 `ACTION_TOGGLE_PAUSE` 经 `PendingIntent.getService` 投递到 `onStartCommand`；
2. `onStartCommand` 跑在**主线程**，里面直接 `AgentRuntimeClient.pauseRun(...)`；
3. `AgentRuntimeClient.withRuntimeMessenger` → `AgentRuntimeConnection.acquire` 开头有
   `check(Looper.myLooper() != Looper.getMainLooper())`，抛 `IllegalStateException`；
4. 该异常被 `withRuntimeMessenger` 的 `catch (throwable: Throwable)` **静默吞掉**，暂停指令从未发出；
5. fork.91 又无条件把 `runPaused` 乐观翻成 `true`，于是通知显示「继续」，实际仍在跑。

改法：新增单线程 `pauseDispatch`（`eta-execution-pause`，daemon），把暂停/继续的投递移出主线程；
投递失败写 `Execution pause dispatch failed` 日志不再静默；`onDestroy` 里 `shutdownNow()`。

**这和 fork.88 的 `SupplementActivity` 是同一类坑**：凡是在主线程调用 `AgentRuntimeClient`
同步接口的地方都会撞上 `acquire` 的主线程断言。

### 试过但放弃：通知按钮加图标

fork.91 曾给暂停/继续、插入、停止分别做了 5 个 24dp 单色矢量图并接进 `Notification.Action`，
真机上按钮仍然只有文字。原因：**从 Android 7.0 起 AOSP 的通知动作条不再渲染 `Action` 图标**，
只显示文字标签，HyperOS 同样如此。图标资源已删除，`icon` 参数回到 `null` 并加注释说明。

---

## fork.90（2026-11-24）侧栏多选条与系统悬浮窗透底

fork.89 之后按真机截图继续排查，补上侧栏和系统悬浮窗这一批。

| 文件 | 组件 | 现象 |
|---|---|---|
| `ConversationPanePanel.kt` | `ConversationBulkBar` | 侧栏多选底部条（「点会话开始多选 / 移到分类 / 删除 / 取消」）透底，能看到下面的会话行 |
| `ConversationPanePanel.kt` | `BulkActionChip` | 多选条里的三个按钮，两层叠加后仍然透 |
| `ConversationPanePanel.kt` | `CategoryChip` | 分类筛选胶囊未选中态透底 |
| `ConversationPaneChrome.kt` | `PaneTopBarActionButton` | 侧栏顶栏圆形按钮浮在列表上，透底 |
| `ConversationPaneChrome.kt` | `PaneSelectedCharacterChip` | 顶栏角色胶囊同样透底 |
| `AgentOverlayCapsule.kt` | 悬浮胶囊底色 | 原来用 `surfaceContainer.copy(alpha = 0.95f)`，浮在别的应用之上会透出桌面 |
| `AgentOverlayCapsule.kt` | `CapsuleAction` | 胶囊内的操作按钮 |
| `AgentOverlayContent.kt` | `OverlaySupplementInput` | 悬浮补充输入框 |
| `VirtualDisplayStageCard.kt` | 微缩镜 / 画布 / `StagePillButton` | 虚拟屏悬浮卡片三处 |

**系统悬浮窗这一批更要紧**：它们叠在别的应用之上，透底比应用内弹窗更明显。

**明确保留不动的**（页面内嵌卡片，透出的是页面底色，属正常层次设计）：
`ChatMessageItem`、`AgentToolActivity`、`AgentChatInputBar`、`GeneratedImageMessageCard`、
`ToolChip`、`CharacterPortraitCard`、`SubagentActivitySheet` 内部卡片、`ImageGen*`、
`AgentToolsScreen` 的筛选条、`SpeechControls`。

**判别口径**（两轮下来稳定成立）：浮层或悬浮物的**最外层底色**必须走 `opaqueThemedSurface(...)`；
页面内容区里的卡片可以用 `surfaceContainer` 系列做层次。例外是 `AgentOverlayCapsule` 的
`surfaceContainer.copy(alpha = 0.95f)` —— 它本来就是刻意的半透明，但作为系统级悬浮窗仍然透得太多。

---

## fork.89（2026-11-24）弹窗与浮层透底（surfaceContainer 带 alpha）

同一个根因的第二批：`MiuixTheme.colorScheme.surfaceContainer` / `surfaceContainerHigh` 是 MD3 的
**叠加色**，深色主题下自带 alpha，直接拿来当浮层底色就会透出下层内容，文字糊成一片。
fork.85 修过 4 处，这轮把剩下的浮层全查了一遍。

| 文件 | 组件 | 现象 |
|---|---|---|
| `EtaControls.kt` | `EtaWindowDialog` | 删除轮次确认框（「删除第 N 轮」）整个卡片透底，能看到后面的轮次列表 |
| `EtaControls.kt` | `EtaOverlayDialog` | 另一套 overlay 版弹窗，同样透底 |
| `EtaTextToolbar.kt` | 文字选择浮条 | 长按选字后的剪切/复制/粘贴菜单透底 |
| `ChatStatsPopover.kt` | `ChatStatsPopoverPopup` | Token 用量 / 上下文占用气泡透底（`ContextUsageStatsPopoverPopup` 复用同一实现） |
| `ConversationPaneChrome.kt` | `conversationPaneContainerColor()` | 侧栏面板深色下透出背后的对话内容 |

最后一处最隐蔽：`lerp(surface, surfaceContainer, 0.45f)` 把 `surfaceContainer` 的 alpha 一起混进了
结果，混合出来的面板本身就是半透明的。改为 `lerp(surface, opaqueThemedSurface(surfaceContainer), 0.45f)`。

全部统一走 `opaqueThemedSurface(...)`（`OpaqueSurfaces.kt`：把容器色按自身 alpha 合成到页面背景，
保留主题色相，Monet 取色照常生效）。

**核对后确认无需改的**（已正确或不属于浮层）：

- `AgentAppRoot.kt` 角色选择 Dialog：已用 `opaqueThemedSurface()`
- `ConversationPanePanel.kt` 的 `PaneDialog`：已用 `paneOpaqueDialogColor()` + 自加 0.62 遮罩
- COUI 的 `CouiAlert` / `CouiBottomSheet`：`DialogDefaults.backgroundColor()` 本身不透明；有材质模糊时才用 `Color.Transparent`
- Miuix 的 57 处 `GlassDialog` / `GlassBottomSheet` / `GlassPopup`：默认 `containerColor` 就是 `MiuixTheme.background`，不透明
- `SubagentActivitySheet` 内部卡片、`ChatMessageItem` / `AgentToolActivity` / `AgentChatInputBar` 等：页面内嵌卡片，不是浮层

**判别口径**：只要是独立浮层（Dialog / Popup / BottomSheet / 侧栏面板）的**最外层底色**，就不能直接用
`surfaceContainer` 系列；浮层内部再叠的卡片可以用叠加色做层次。

---

## fork.88（2026-11-24）自动备份选项失效 / 通知「插入」丢内容 / 插入弹层透底

三处都是「点了没反应 / 内容消失」，根因各不相同。

| 现象 | 定位 | 改动 |
|---|---|---|
| 数据备份页「保留份数 / 备份间隔 / 备份格式 / 备份历史」点了完全没反应 | 这三个 `GlassDialog` 挂在 `when (LocalUiStyle)` 之外，而设备当前是 COUI 样式。`AgentAppTheme` 的 COUI 分支只提供了 `CouiScaffold`，**没挂 `MiuixPopupHost`**。Miuix 的 `GlassDialog` 只把自己注册进 `LocalDialogStates`，真正绘制由 `MiuixPopupHost` 完成；没有宿主时 `show=true` 也不会有任何画面 | `AgentAppTheme` COUI 分支补 `MiuixPopupHost()`（放在 `Box` 内与内容并列）。同时把三个弹窗改为按 `uiStyle` 分派：COUI 走 `CouiNoticeDialog`（COUI 原生 `OverlayDialog`），MIUIX 走 `GlassDialog`，两套样式各有原生弹窗 |
| 保留份数只能停在默认 7 份，选不了 3 份 / 5 份 | 预设档位只有 3/7/14/30，且弹窗本身打不开 | 弹窗里加「自定义」输入行（数字键盘，1–60 夹紧）；备份间隔同理（1–720 小时）。档位值改为 `String` 传递，预设与自定义统一走 `onPick` |
| 通知卡片点「插入」，弹层消失但内容没插进去 | `SupplementActivity.onSubmit` 在 Compose 点击回调（主线程）里直接调 `AgentRuntimeClient.steerRun`，而 `AgentRuntimeConnection.acquire` 开头有 `check(Looper.myLooper() != Looper.getMainLooper())` —— 抛 `IllegalStateException`，被外层 `runCatching{}` 静默吞掉，紧接着 `finish()` | 提交改到 `lifecycleScope` + `withContext(Dispatchers.IO)`；按 `SteerOutcome` 分三种结果 Toast（已插入 / 运行已结束 / 连不上运行进程），失败不再无声。按钮加 `submitting` 态防重复点击 |
| 插入弹层背景太透，文字糊 | 卡片底用 `MiuixTheme.colorScheme.surfaceContainer`（MD3 叠加色，深色下带 alpha），遮罩只有 0.45 | 卡片与输入框改用 `opaqueThemedSurface()`（`OpaqueSurfaces.kt`，把容器色按 alpha 合成到页面背景）；遮罩提到 0.58 |

**复用要点**：Miuix 的 `GlassDialog` / `GlassBottomSheet` / `GlassPopup` 都依赖 `MiuixPopupHost` 渲染。任何在 `AgentAppTheme` 里绕过 Miuix `Scaffold` 的分支（如 COUI 的 `CouiScaffold`）都必须自己挂宿主，否则该分支下所有 Miuix 浮层静默失效。

---


按要求把「上拉小横条」从独立一行挪到能力胶囊那一行的左侧。

| 版本 | 现象 | 定位与改动 |
|---|---|---|
| fork.86 | 横条在能力胶囊上方、居中偏左，两者不在同一水平线 | 横条原先在 `Scaffold.bottomBar` 里独占一行（靠 `offset(y=10dp)` 往下贴输入框），胶囊在 `AgentChatInputBar` 内部右上角。给输入栏加 `topStartSlot` 插槽，与 `CapabilityCapsule` 放同一 `Row`（`SpaceBetween`：插槽在左、胶囊在右，垂直居中）；`ChatTurnNavigator` 外层不再无条件 `fillMaxWidth` + `BottomCenter`，未展开时按内容宽度 `CenterStart` |
| fork.86 | **展开面板被压成 1/3 宽**，标题与轮次全被截断 | 上一版把整个 `ChatTurnNavigator`（含面板）塞进了 `weight(1f)` 插槽，面板自然只剩 1/3。拆成三部分：`ChatTurnNavigatorState`（共享 `expanded`）+ `ChatTurnHandle`（96dp 把手，进插槽）+ `ChatTurnPanel`（面板，放回 `bottomBar` 并 `fillMaxWidth`） |

**最终布局**：

```
[小横条]                    MCP 1/3  技能 15/15  内存 49%
┌─────────────────────────────────────────────────┐
│ Eta 正在执行…                                    │
│  +   深度思考        ○   🎤   ⏸   ⏹            │
└─────────────────────────────────────────────────┘
```

展开后的轮次面板仍在输入框上方、整行宽度，与改造前一致。

### 这一段踩到的坑

- **把手与面板必须共享同一份 `expanded`**。拆组件时如果各自 `remember { mutableStateOf(false) }`，点把手面板不会出现。用 `@Stable class ChatTurnNavigatorState` + `rememberChatTurnNavigatorState()` 传递。
- **放进 `weight(1f)` 的容器里的组件，其 `fillMaxWidth` 只等于那个权重宽度**。面板要整行宽，就不能和把手共用同一个 Row 槽位。
- **拆函数后要检查局部变量是否还被引用**：`haptics` 在 `ChatTurnPanel` 里还有两处调用（跳转、删除），删掉声明后编译报错。
- 下载产物时 8 段并行与单线程 curl 都可能因 `Errno 103` / 速度归零卡住；`gh run download <run-id> -D <dir>` 走发布通道，通常十秒内完成，应作为首选。

---

## fork.82 – fork.85（2026-11-23）主题收尾：GitHub 页与弹窗

fork.81 之后按「GitHub 页面没有接入莫奈」「轮次列表这种弹出窗口也要接入」两条反馈继续。

### 改动

| 版本 | 内容 |
|---|---|
| fork.82 | **GitHub 账号页补 MIUIX 版式**。该页此前只写 COUI 版式，MIUIX 样式下渲染 `CouiScaffoldPage`/`CouiPreferenceGroup`，取 COUI 基线色，拿不到 Monet。按 `when (LocalUiStyle.current)` 拆两分支，MIUIX 用 `MiuixScaffoldPage` + `Card` + `ArrowPreference`/`BasicComponent`/`SwitchPreference`/`SmallTitle`/`HorizontalDivider`，覆盖未连接、申请设备码、等待验证码、已连接、失败五态 |
| fork.83 | **弹窗/浮层底色改用主题合成**。新增 `OpaqueSurfaces.kt`：`opaqueThemedSurface()` 把主题容器色按自身 alpha 合成到 `background`，等价不透明且保留色相。替换 4 处写死的绝对色：轮次列表（`#F7F7FA`/`#1C1C1E`）、角色选择弹窗、侧栏对话框、分类弹窗（均为 `#FFFFFF`/`#2A2A2E`） |
| fork.85 | 检查脚本 E4 扩展到 13 个 `Modifier.xxx` 扩展函数 |

### 为什么弹窗底色要特殊处理

Miuix 的 `surface` / `surfaceContainer` 系列在深色下带 alpha（MD3 容器色本就是叠加色），直接当弹窗底色会透出下层列表，文字糊成一片。早期各处用写死的白/灰兜底，能挡住透底但**绕过了 Monet**。合成法同时满足两个约束：不透明 + 跟随色相。alpha 为 1 时结果即原色，无偏移。

### 排查方法修正

fork.81 的脚本把 `AgentBrowserScreen`、`AgentMemoryScreen`、`UserTerminalScreen`、`AgentToolsScreen` 误判为「缺 MIUIX 版式」——它们实际都写了 `when (LocalUiStyle.current) { COUI -> ...; MIUIX -> ... }`，只是主体内容共用 `pageContent(modifier)`，没有各自调 `MiuixScaffoldPage`。W1 规则改为「既无 `MiuixScaffoldPage` 也无任何样式分支」才报警。`AgentHomeScreen` 只转发 state 给 `AgentChatBody`，加入 `PURE_CONTAINER_SCREENS` 白名单。

### 检查脚本当前规则

| 规则 | 级别 | 内容 |
|---|---|---|
| E1 | 错误 | `AgentAppTheme` 之外直接构造 COUI 基线配色 |
| E2 | 错误 | 页面用固定 hex 且无明暗分支 |
| E3 | 错误 | 弹窗/浮层用固定 hex 当底色 |
| E4 | 错误 | `@Composable` 文件用了 `Modifier`/`dp`/`sp`/`Spacer` 或 13 个 `Modifier.xxx` 扩展却缺 import |
| W1 | 警告 | COUI 页面既无 MIUIX 版式也无样式分支 |
| W2 | 警告 | 无主题色引用也无 Scaffold |

当前 47 个页面：**0 错误 0 警告**。

### 这一段踩到的坑

- **连续两次因漏 import 挂构建**（fork.82 漏 `Modifier`/`dp`，fork.84 漏 `padding`）。编译器一次只报一批，人肉逐个补效率极低——这是 E4 规则存在的理由。
- **E4 第一版误报 4 个 xposed 文件**：`dp` 被当成参数名 `dp: Int` 匹配、`Modifier` 出现在注释里。修法是加 `strip_comments()` 去掉注释与字符串字面量，且 `dp`/`sp` 用 `(?<![\w.])` + `(?![\w:])` 双重边界。
- **`__pycache__` 会跟着 `git add -A` 进仓库**，`.gitignore` 要显式加一条。
- 脚本跑得比编译快，把低级错误挡在 CI 之前能省一整轮 6–8 分钟的构建。

---

## fork.75 – fork.81（2026-11-23）主题：深色模式与 Monet 取色排查

按「排查哪个页面没适配夜间/日间模式」的要求做的一轮全量审计。

### 排查方法

1. 39 个 `*Screen.kt` 逐一检查是否引用主题色来源、是否有 COUI/MIUIX 双分支
2. 全仓 115 处固定 `Color(0x…)` 逐个查是否同文件存在明暗分支
3. 拉取 `coui-ui-android-2.1.0-sources.jar` 与两个版本的 `material-color-utilities` AAR，
   用 `javap` 核对 `COUITheme.isDark` / `LocalColors` 默认值 / `DynamicScheme` 类路径
4. 把上述规则固化成 `scripts/check-theme-coverage.py`

### 发现与修复

| 版本 | 现象 | 定位与改动 |
|---|---|---|
| fork.76 | 深色模式下**会话分类**、**GitHub 账号**两页整页浅色 | 两页只写了 COUI 版式。`COUITheme` 此前只在 `uiStyle == COUI` 时提供，MIUIX 样式下 `COUITheme.colorScheme` 回落到 `LocalColors` 的默认值 `lightColorScheme()`。改为无条件提供 `COUITheme` |
| fork.77 | 深色已生效，但 COUI 配色不跟 Monet | `couiNative` 固定为 `lightColorScheme()/darkColorScheme()`，与 `monetEnabled`/`accentColor`/`paletteStyle` 全无关 |
| fork.78 | fork.77 编译失败（3 处） | 直接调 `colorsFromSeed` 不行：它是 `internal`、`platformDynamicColors` 是 `@Composable`、两个库的 `ThemePaletteStyle` 是不同类型。改用 COUI 公开的 `ThemeController` |
| fork.79 | fork.78 编译通过但 R8 失败：`Missing class com.materialkolor.scheme.DynamicScheme` | COUI 2.1.0 依赖 material-color-utilities **5.0.0**（`com.materialkolor.scheme`），Miuix beta03 依赖 **6.0.0**（`com.materialkolor.dynamiccolor`），Gradle 取高版本后 COUI 的旧包名消失。`force` 到 5.0.0 会反向让 Miuix 报错 |
| fork.80 | — | 按要求回退：**COUI 不接 Monet**，恢复自带浅/深基线。COUI 整套槽位按自家 MD3 推导，套 Miuix 种子色后对比度和层次明显变差 |
| fork.81 | MIUIX 样式下**会话分类页没有 Monet 取色** | 该页只有 COUI 版式，MIUIX 下仍渲染 `CouiScaffoldPage`/`CouiPreferenceGroup`，取的是 COUI 基线色。补 MIUIX 分支：`MiuixScaffoldPage` + `Card` + `ArrowPreference` + `BasicComponent`，颜色全走 `MiuixTheme.colorScheme`；新增 `MiuixIconAction` 与 COUI 版 `IconAction` 同尺寸 |

### 审计结论

- 真正缺适配的只有 2 个页面（会话分类、GitHub 账号），其余 37 个都是双样式
- 6 处固定 hex 且无明暗分支的，是品牌色/语义色/字标渐变，设计上不随主题变，不是缺陷
- `values-night` 只有 `styles.xml`，主题全走 Compose，不涉及系统资源切换
- `EtaPreferenceTheme` 的固定色有 `!monetEnabled` 守卫，Monet 开启时不生效
- 剩余待补 MIUIX 版式：`AgentBrowserScreen`、`GitHubAccountScreen`、`AgentMemoryScreen`、`UserTerminalScreen`、`AgentToolsScreen`

### 这一段踩到的坑

- **COUI 的 `colorsFromSeed` / `monetSystemColors` 都是 `internal`**，外部模块无法调用；要接 Monet 只能用公开的 `ThemeController`。
- **`ThemeController.currentColors()` 是 `@Composable`**，不能在 `remember` 块里调用。
- **两个库的 `ThemePaletteStyle` / `ThemeColorSpec` 是各自独立的枚举**，不能互相传；COUI 还有 Miuix 没有的 `CMF` 变体。
- **`material-color-utilities` 5.0.0 与 6.0.0 类路径不兼容**：`scheme.DynamicScheme` vs `dynamiccolor.DynamicScheme`。升级任一库前必须先确认两者版本一致。
- **`COUITheme.colorScheme` 的默认值来自 `LocalColors`，而 `LocalColors` 默认是 `lightColorScheme()`**——这是「深色模式里某页整页浅色」的根因模式，遇到先查该页有没有拿到 COUITheme。
- 排查主题问题不能只看代码里有没有 `MiuixTheme`，要看**实际渲染路径**：只写 COUI 版式的页面在 MIUIX 样式下根本不会读 Miuix 主题。

---

## fork.67 – fork.74（2026-11-23）角色卡：选择器修复、头像动图/视频、语音

上一版发版后按真机反馈做的一整段角色卡工作。

### 角色选择器

| 版本 | 现象 | 定位与改动 |
|---|---|---|
| fork.67 | 点顶栏角色按钮，弹窗里角色列表空白；去设置里进一次角色库后再点就正常 | `CharacterRepository` 的 context 只在进角色库页面时才 `initialize`。首次点击 `list()` 抛 `IllegalStateException`，被 `runCatching{}.getOrDefault(emptyList())` 吞成空列表。改为 `EtaApp.onCreate()` 里就绑定 context；读取失败不再静默，弹窗显示失败原因 |
| fork.67 | 选择列表没有头像，最多只显示 12 个 | 每行加 40dp `CharacterAvatar`（复用 `CharacterImageCache` 降采样），去掉 `take(12)`，改 `heightIn(360dp)` + 滚动 |
| fork.70 | 选完角色后看不出选了谁 | 顶栏角色按钮在已选时变成「头像 + 名字 + 箭头」胶囊；弹窗标题下写「当前已选：<名字>」，选中行加主色底 + 描边 + 「✓ 已选」 |
| fork.73 | 工作台多了「新建会话角色」一行，和顶栏胶囊重复 | 删掉该行，只保留顶栏胶囊 |

### 头像支持动图与视频

| 版本 | 内容 |
|---|---|
| fork.68 | 新增 `CharacterAvatarKind`，按魔数识别 PNG/JPEG/WebP/GIF/MP4/WebM（不看扩展名）。`writeAvatar` 不再强制 PNG：动图与视频原样落盘，静态位图仍规整为 PNG |
| fork.68 | 新增 `CharacterAnimatedAvatar`：GIF/动图 WebP 走 `AnimatedImageDrawable`，视频走 `TextureView` + `MediaPlayer`（静音、循环、自动播放、离开组合即释放）。只在角色详情主视觉播放；列表与消息头像用视频第 0 帧作缩略图，走原缓存 |
| fork.68 | PNG 角色卡导出遇到动图/视频头像时先抽帧转 PNG，避免写坏文件 |
| fork.68 | 头像选择器放开 `video/*`（`GetContent` 不收 MIME 数组，改用 `OpenDocument`） |

### 角色卡语音

| 版本 | 内容 |
|---|---|
| fork.68 | `CharacterCard` 新增 `extensions.eta_voice`；`RoleplayBinding` / `RoleplayChatUi` 带 `voiceId`；`SpeechPlaybackController.speak` 加 `voiceOverride`；手动朗读与自动朗读都传角色音色 |
| fork.70 | 修 `binding` 同步只更新名字/头像、不更新 `voiceId`，导致改完音色老会话仍用旧快照 |
| fork.71 | 新增 `listSystemTtsVoices()` 枚举系统引擎音色（offlineOnly 时只留已安装且不要求联网的）；`SystemTtsConfig` 加 `voiceName`；系统 TTS 设置页加音色下拉；角色编辑器音色改下拉（系统离线音色 + 自定义） |
| fork.74 | 角色卡可选**朗读服务商**而不只是音色：新增 `extensions.eta_tts_provider`，`withVoiceOverride` 同时覆盖 `tts` 与音色，`speak` 加 `providerOverride`。角色编辑器「语音」改为「服务商 + 音色」两个下拉，换服务商时清空音色 |

### 这一段踩到的坑

- **本地离线语音模型里没有 TTS**：`assets/local_speech/catalog.json` 的 8 个模型全是 ASR（`qwen3_asr` / `sensevoice` / `vosk`），`libeta_transcribe_jni.so` 只做转写。所以「下载的离线模型」无法用于朗读，离线朗读唯一可用的是系统 TTS 引擎音色。
- **音色 id 是各家自己的标识**：`Cherry` 是千问、`zh_female_vv_uranus_bigtts` 是豆包。只记音色不记服务商，全局换服务商后角色音色必然失效——这是 fork.74 存在的理由。
- `ActivityResultContracts.GetContent` 的 input 是单个 MIME `String`，传数组会报 `Argument type mismatch: actual type is 'Array<String>', but 'String' was expected`；要多 MIME 用 `OpenDocument`。
- `CompositionLocal.current` 不能在 `LaunchedEffect` 里读（`@Composable invocations can only happen from the context of a @Composable function`），要先在组合作用域取出再传给 effect。
- public 函数不能暴露 `internal` 类型（`'public' function exposes its 'internal' parameter type`），`ConversationSidePaneScaffold` 因此改成 `internal`。
- 编辑工具批量替换时，`*\/` 与后面的 `@Composable` 粘成 `*\/@Composable` 会让注释不闭合，把后面整段函数吞掉；改完要用括号/注释配平脚本过一遍。
- CI 只认完整 40 位 SHA；构建中转仓库是 `mariusarva/eta-ci`，主仓库 Actions 被平台禁用。
- 产物下载遇到 `Errno 103 Software caused connection abort` 时，8 段并行重试会卡死；改用单线程 `curl -L --retry 20` 反而更稳。

---

## fork.66（2026-11-23）发版

fork.63–65 三处修复验证通过后的正式发版，内容同 fork.62 + 上述三修。

- APK `Eta-v3.4.4-fork.66+ad3daaf12fe6ff5578853c19ab2e960fc50b8ed9.apk`，27,197,249 字节
- SHA-256 `601df3845e32b0d41560fc7b99a2eb239c31623d73d65aa860bd10faee382764`
- versionName `3.4.4-fork.66` / versionCode `2026112166`，target `ad3daaf`
- CI：`mariusarva/eta-ci` run `45094375409`，artifact `14782966790`

**发版流程备忘**：Release 附件除 APK 外必须同时挂 `CHANGELOG-FORK.md` 与 `FORK_PATCH_GUIDE.md`。tag 一次性，`v3.4.4-fork.62` 已占用，不能复用。

---

## fork.62 – fork.65（2026-11-23）发布收尾：分类弹窗与角色选择器

fork.62 是第一次正式发版（含 fork.38–61 全部改动）。之后按真机反馈又修了三处。

| 版本 | 现象 | 定位与改动 |
|---|---|---|
| fork.62 | — | 发版：上游同步 + 提示词与工具边界对齐 + 全部 fork 特性 |
| fork.63 | 新建分类弹窗背景透明，文字糊在列表上 | `CouiPageDefaults.themeColors().card` 在深色主题下带 alpha。改按明暗取绝对色（浅 `#FFFFFF` / 深 `#2A2A2E`）+ 0.5dp 描边 |
| fork.64 | 点侧栏顶栏中间的人像图标没反应 | 角色选择 `GlassDialog` 只写在 `when (LocalUiStyle.current)` 的 COUI 分支里。上游 `a14286b` 已把 Miuix 设为默认样式，跑的是 MIUIX 分支，`showCharacterPicker` 置位但没有对话框消费它。移到 `when` 之外 |
| fork.65 | fork.64 仍不显示 | 日志显示点击后 `ImageDecoder` 解出了 104×104 头像（列表已加载），但 0.9 秒后 `setTopOnBackInvokedCallback` 切回 Activity —— 弹窗起来后立刻消失。`GlassDialog` 依赖 Miuix 的 glass overlay 宿主，放在 `when` 之外拿不到，`show` 无法维持窗口。改用系统 `Dialog` 窗口（与会话分类弹窗同一套），并加 `character picker requested` / `opened: count=N` 两条日志 |

**这一段踩到的坑**：

- 同一个「弹窗起来立刻消失」现象，`GlassDialog` 在 `when (LocalUiStyle)` 之外会静默失效，不报错、不打日志，只能靠 `ImageDecoder` 与 `setTopOnBackInvokedCallback` 的时间差判断。
- 把 Compose 代码块在 `when` 分支与函数体之间搬动时，缩进层级一变，`{` / `}` 计数极易错位；改完必须数一遍括号。
- 工作区清理会连带删掉代码仓库与下载脚本；`dl2.py` 可随时重写，但 `tmp-work` 需要重新 clone（`rebase` 分支在远端有备份）。

---

## fork.38 – fork.61（2026-11-22）UI 对齐、上游同步、PR 与外部仓库对齐

**背景**：fork.37 之后是按真机反馈逐条修 UI 的过程，中间夹了上游同步与两次外部代码对齐。

### UI 与交互（fork.38–52）

| 版本 | 现象 | 定位与改动 |
|---|---|---|
| fork.38 | 通知卡按钮压住图标与标题、没有停止键 | 通知只渲染前 3 个 action，加了暂停/插入变成 4 个，停止被裁；改为 暂停·继续 / 插入 / 停止，打开交给 contentIntent |
| fork.38 | 暂停与插入比停止小一圈 | 两者按 24dp 画、主键是 32dp；统一 32dp 视觉圆 / 40dp 命中 / 18dp 图标 |
| fork.39 | 分类条最右两个按钮无效 | ＋/⋯ 与 chip 同在横向滚动容器末尾，分类一多被挤出可视区；改为左侧滚动、右侧固定 |
| fork.39 | 离线语音模型无法下载 | `assets/local_speech/catalog.json` 没随搬运进仓库，`LocalSpeechRegistry` 读不到清单 → 列表为空 |
| fork.39–52 | 分类布局反复调整 | 最终形态：不再是顶部 chip 条，改为侧栏工作台一行「会话分类」入口 + 独立整页管理 |
| fork.40 | 底部轮次横条看不到 | 它只有 `fillMaxWidth`，放进 Box 按 TopStart 排在消息区下方被盖住；最终移进 Scaffold bottomBar、输入框正上方 |
| fork.41 | 无法新建分类 | 侧栏 4 个对话框用 GlassDialog 叠在 200dp 侧栏内被裁切；改用真正的 Dialog 窗口 |
| fork.42 | 弹窗/轮次面板文字糊在一起 | Miuix 的 surface 系列深色下带 alpha，透出下层；改用绝对色（深 `#2A2A2E` / 浅 `#FFFFFF`） |
| fork.43 | 分类默认展开把会话列表顶走 | 分组默认折叠（后因改成工作台入口，此逻辑随之移除） |
| fork.44 | 排队条与输入框糊在一起 | 圆角胶囊 → 方角 → 最终 14dp 圆角 + 0.5dp 描边 |
| fork.45–46 | 分类对话框偏右 | `usePlatformDefaultWidth=false` 后内容按自身宽度排；最终改回满屏窗口 + 居中 + 左右对称 14dp |
| fork.47–48 | 轮次横条要求去背景并下移 | 把手去掉底色块、26→18dp、offset(y=10dp) 贴输入框 |
| fork.50 | 能力胶囊闪烁 | 去掉「有消息且非流式」条件，常驻显示；弹窗加右上角关闭键 + 42% 黑遮罩 |
| fork.51 | 每轮消息找不到删除入口 | 时间行右侧加 26dp 删除键，走同一条 `onDeleteMessage` |
| fork.52 | 会话分类点开是弹窗，要求整页 | 新增 `AppRoute.ConversationCategories` + `ConversationCategoriesScreen`，新建/改名/上移/下移/删除都在页内 |

### 功能（fork.53–61）

| 版本 | 内容 |
|---|---|
| fork.53–55 | 「先选定角色，之后新建都用」：`characterForNewChat` 落 Prefs，`createConversation` 检测到绑定就走 `startCharacterConversation`；内部拆 `createPlainConversation` 打断递归 |
| fork.56 | 移植 PR #3（`4bc209f`）：新建会话自动绑定角色卡。未归档取列表第一张，`roleplayAutoBindInFlight` 防连点，补 4 条单测。与「先选定」共存：显式选定优先 |
| fork.57 | 同步上游 `36b1c77 → 05086d3`：AGP 9.5.2、compose-window-size 1.5.0、lifecycle 2.11.1、Gradle wrapper 升级、miuix 主题修复、定时任务编辑页改 COUI TimePicker。冲突只有三处纯 import 段 |
| fork.58 | 关于页与反馈链接、模型目录 UA 全部由 `Mangi-11/Eta` 改为 `mariusarva/Eta` |
| fork.59 | 对齐 Oo0520/Eta-por 的提示词与工具边界（详见下节） |
| fork.60 | 设置页「提示词预设」改为「提示词与工具边界」并跳概览页 |
| fork.61 | 能力胶囊计数：技能改用 `listSkillsForManagement()`（`listInstalledSkills()` 已过滤 enabled，导致永远 N/N）；计数改 15 秒轮询 + 打开明细强制重读 |

### 与 Oo0520/Eta-por 的逐文件对比结果

`agent/prompt/` 8 个文件字节级一致，`ui/screens/prompts/` 仅 `PromptPresetsScreen.kt` 多一个图标，预设目录仅 `default.json` 不同。真正差异四处，全部补齐：

1. **工具边界执行侧兜底**：run 起点读一次 `AgentToolPolicy`，请求侧按它过滤 tools，执行侧再用同一份 `allows()` 复核，未授权调用返回 `TOOL_NOT_ALLOWED_BY_POLICY`。此前只有请求侧过滤，模型从历史里拼出的调用能绕过。
2. **纯聊天档（`LocalToolProfile.CHAT`）**：系统提示走独立分支，只留身份/人格/追加指令 + 「本轮不提供任何工具」说明，不注入记忆与技能索引；历史里的 `role=tool` 与 `tool_calls` 一并跳过。
3. **技能索引只在 FULL 档注入**：否则模型看得到目录却读不到正文。
4. **`default.json` 预设**改回 `APPEND` + 空 base（原为 `REPLACE` + 一句身份，会抑制整段内置规则）。

未搬：`AgentHistoryTrimmer` / `AgentContextComposition`。它们依赖 `TokenUsageUi` 的 `systemPromptTokens` / `toolDefinitionTokens` / `conversationTokens` 三字段，属于「上下文构成面板」配套；本仓库 `ChatStatsPopover` 已有完整的 Token 用量面板（字段为 `llmMs`/`ttftMs`/`decodeMs`/`stepCount`），搬过来要改数据库列与所有 provider 的用量构造，收益仅为外观，故放弃。

**这一段踩到的坑**：

- `listInstalledSkills()` 内部已 `filter { installed && enabled }`，拿它统计启用数会永远 N/N。
- `MiuixIcons.Glass.FolderOutline` 在本仓库的 miuix 版本不存在，菜单图标要用 `Glass.Import` 或 `Icons.Outlined.Folder`。
- 稀疏检出对 `app/src/test/...`、`app/src/main/assets/local_speech`、`app/src/main/jniLibs`、`docs` 都要显式 `sparse-checkout add`，否则 `git add` 静默跳过。
- push 偶发 `Recv failure` / `not our ref`：要循环 push 并用 `git ls-remote` 校验远端 SHA 再触发 CI。

---

## fork.29 – fork.37（2026-11-22）底包重建收尾：fork 特性全部对齐

**背景**：`rebase` 分支换到上游 `36b1c77` 后只搬了数据层与部分 UI，侧栏、运行时、通知、备份
都是空的。这一段把 fork 特性逐项搬回并逐一验证编译。

| 版本 | 内容 | 关键改动 |
|---|---|---|
| fork.29 | 暂停 / 继续 + 运行中插入 | `MSG_PAUSE`/`MSG_RESUME`；`AgentRunController.awaitIfPaused()` 插到 `onProviderEvent` 入口，流式 token 到达即停；三层 UI 透传与输入框两个键 |
| fork.30 | 会话分类 | 数据链路 9 个方法 + 分组（置顶→分类→未分类按时间）+ 筛选条 + 新建；修掉整行 upsert 丢 `category_id` |
| fork.31 | 分类管理 + 通知按钮 | 改名 / 上下移 / 删除对话框；通知动作 打开 → 暂停/继续 → 插入 → 停止 |
| fork.32 | 时间戳带日期 | `rowLabel`（M月d日 HH:mm，跨年补年份）；会话行、消息气泡、轮次列表同源 |
| fork.33 | 自动备份 UI | 开关 / 间隔 / 份数 / SAF 目录 / 立即备份 / 历史删除 |
| fork.34–35 | 轮次列表 | 搬回 `ChatTurnNavigator`，轮次改读全库 `userMessageDigests`，跳转前按页加载 |
| fork.36 | 备份格式 | 自动备份可选仅数据 JSON 或全量 zip |
| fork.37 | 多选批量 | 顶栏多选入口、底部批量条、移动到分类 / 批量删除 |

**踩到的坑**：

- `CouiPreferenceGroup` 的 content 是 `CardListScope.()`，子项必须包 `item {}`，否则报
  「@Composable invocations can only happen from the context of a @Composable function」。
- `SettingsIconColors` 在 `SettingsScreen.kt` 是 `private`，跨文件要自带颜色常量。
- 上游 `AgentExecutionService` 通知已重构成 headline + 打开/停止，暂停按钮要插在中间而不是
  重建整个 action 列表。
- `updateAutoBackupSettings` 返回 `Unit`，写入后要重新读取状态，不能直接赋值。
- `ConversationPaneTopBar` 的 MIUIX 分支没有 `IconButton`，要用 `GlassIconButton`。
- 稀疏检出要显式 `git sparse-checkout add app/src/main/jniLibs docs app/src/test`，
  否则大文件 commit 会静默丢路径。
- CI 的 `ref` 必须是已推送的完整 40 位 SHA；push 未完成就触发会报 `not our ref`。

---

## fork.28 补充（2026-11-22）离线语音搬回 + 体积回归

**现象**：fork.28 首个成功产物只有 9.7MB，`lib/` 只剩 7 个 so、共 1.5MB；旧底座同版本是 26MB。
APK 能装能跑，但语音输入/朗读全部失效。

**定位**：体积差全在 `lib/arm64-v8a`，缺的正是离线语音运行库：

| 缺失 | 压缩后 |
|---|---|
| libonnxruntime.so | 7.59MB |
| libsherpa-onnx-jni.so | 1.81MB |
| libncnn.so | 1.78MB |
| libeta_transcribe_jni.so | 1.41MB |
| libvosk.so | 3.11MB |
| libsherpa-ncnn-jni.so | 0.29MB |
| libc++_shared.so | 0.44MB |

上游自身没有离线语音，这些 so 与 `com.k2fsa.sherpa.onnx` Kotlin binding 都是 fork 自己带的，
rebase 时只搬了 UI 层，源代码与二进制一起漏了。

**改动**：

- `app/src/main/jniLibs/arm64-v8a/` 补回 7 个 so（`libeta_pty` / `libproot_*` 仍由上游依赖提供，
  不重复放）。
- 复制 `com.k2fsa.sherpa.onnx` 5 个 Kotlin binding 文件。
- 搬回 `agent/voice/local/` 22 文件（注册表、下载、校验、GGUF/Vosk 引擎、独立进程服务）、
  `agent/voice/qwen/WarmCachePolicy`、`agent/voice/SystemSpeechPlayback`。
- 搬回 `ui/voice/` 的本地模型管理与系统 TTS 设置 6 文件。
- `SpeechSettings`：新增 `SystemTtsConfig`；`AsrProvider` 补 `LOCAL/SHERPA_NCNN/QWEN3_LOCAL`
  并用 `canonical` 把历史枚举归一到 `LOCAL`（下拉不会出现三个同名项）；`TtsProvider` 补 `SYSTEM`。
- 各 `when` 分支补齐：文件识别走本地转写、系统朗读不经 PCM/网络管线直接交
  `SystemSpeechPlayback`、`SYSTEM` 无云端凭据字段、不支持流式合成。
- `SpeechPlaybackController` 接 `SYSTEM` 分支，引擎实例复用（每次重建要多几百毫秒初始化）。
- Manifest 注册 `:local_speech` 独立进程（native 崩溃不牵连主进程）；`build.gradle` 加
  `com.alphacephei:vosk-android:0.3.75`，并把 `versionName/versionCode` 改回 forkVersion 规则。

**验证**：产物 27.1MB，`lib/arm64-v8a` 18.00MB，与旧底座逐项对齐（components 分块统计一致）。

---

## fork.28（2026-11-22）rebase 到上游 main：GitHub 账号与提示词 Studio 接通

**背景**：上游自 `83a25e7` 起 61 个提交、300 文件变更（Miuix 0.9.5、COUI 换库、minSdk 34、
i18n 全量外置）。fork 底座整体重建到 `36b1c77`，分支 `rebase`。

**现象**：搬进来的提示词 Studio 与 GitHub 账号页面编译不过，且没有任何入口能进。

**定位**（三次编译错误逐层剥）：

1. `4a55a36` 只搬了 43 个文件（UI + 部分数据层），实际依赖的上游不存在符号全部报
   `Unresolved reference`：`AgentToolDirectory`、`LocalToolProfile(Store)`、
   `AgentRequestSnapshot`、`AgentSnapshotAnalysis`、`AgentToolNaming`、
   `PromptIntegrityGuard`、`GitHubAccountStore`。
2. 405 条字符串资源随上游 i18n 外置后没跟着搬，`R.string.prompt_*` 全红。
3. `GitHubAccountScreen` 直接 `import com.itos.coui.*`（旧底座是 `io.github.itoseo:coui`
   alpha11），上游已换 `io.github.suq8.coui.kmp:2.1.0`，包名整体变掉。
4. 上游 `CouiPreferenceGroup` 的 content 是 `CardListGroupScope.()`，子项必须包在 `item {}`
   里；直接放 `CouiPreference` 会报 `@Composable invocations can only happen from the
   context of a @Composable function`。
5. `SettingsIconColors` 在 `SettingsScreen.kt` 里是 `private`，跨文件不可见。

**改动**：

- 补齐上述 7 个类（从旧底座原样搬，接口未变）。
- 生成 `res/values/strings_fork_prompt_github.xml`，回填 405 条 prompt_*/github 相关字符串。
- `GitHubAccountScreen` 重写为上游 `CouiPreferenceGroup` + `item{}` + `CouiCardDivider`，
  页面自带 `GitHubIconColors.Blue`。
- `AppRoute` 加 13 条路由；`AgentAppRoot` 加 13 个 `entry<>`；`AgentAppShell` 加对应标题；
  `SettingsScreen` 的「上下文扩展」组加「GitHub 账号」「提示词预设」两个入口（Coui 版与
  旧 ArrowPreference 版都加）。
- **运行时真正生效**：
  - `AgentPromptBuilder.buildSystemMessages` 融合 `customization`（自定义身份 / 人格 /
    追加指令），保留上游新增的 `extraSystemContext`（定时任务指引）；默认参数改为
    `PromptCustomizationStore.load()`，编辑后下一次请求即生效。
  - `AgentModelClient.toolsFor` 的工具数组接入 `AgentToolPolicyStore` 白名单，关掉的工具
    不再下发。
- 修掉一处旧底座遗留：`REPLACE + 空 base` 时整段内置规则不下发（只留工具说明段），
  避免「预设应用了但身份没换」。


**现象**：会话移进分类后自己跑出来，或者根本移不进去；轮次表里的删除按钮点了没反应。

**定位**：

1. `AgentConversationStore.saveChanges` 用整行 `@Upsert` 写 `ConversationEntity`，
   `category_id` 未赋值即被默认值 NULL 覆盖 —— 任何一次保存（发消息、改标题、置顶、切会话）
   都会抹掉归属。DB 实证：`SELECT COUNT(*) FROM conversations WHERE category_id IS NOT NULL` = 0。
2. `moveConversationsToCategory` 只写库，等 `reloadConversationCategories()` 重查才更新内存，
   中间侧栏按旧映射重绘，看起来"移不进去"。
3. 轮次行把 `clickable`（跳转）挂在 `Row` 上，删除按钮嵌套其内，事件被外层消费。

**改动**：

- `ConversationIdentity` 查询带 `category_id`，构造实体时 `categoryId = stored[id]?.categoryId` 回填；
- 移动时先更新 `conversationCategoryIds` 内存映射再落库；删会话时同步清理映射；
- 长按菜单加「移动到分类」（COUI + MIUIX，简繁英文案）；
- 轮次行把跳转点击区缩到左侧（序号+标题+时间），删除按钮独立 36dp 命中区。

**踩坑**：`Icons.Outlined.DriveFileMove` 在 Material outlined 里不存在、
`MiuixIcons.Glass.FolderOutline` 漏导入，第一次构建失败；改用 `CreateNewFolder` 并补导入。

**遗留**：修复前所有移动都没留住，装好后需要重新移一次分类。

---

## fork.24（2026-11-21）侧栏首次展开不卡顿

**现象**：第一次点左侧会话列表会顿一下，之后再点就正常。

**定位**：侧栏关闭时整体被 `anchoredDraggable` 推出屏幕，Compose 跳过绘制，
所以三笔一次性成本全压在"第一次展开"这一帧：

1. `rememberGlassBackdrop` 的 `layerBackdrop { drawRect + drawContent }` 首次创建
   RenderNode，玻璃用的 RuntimeShader 现场编译（logcat 里 `SKIA: CreateGraphicsPipeline
   pipeline cache missed, elapsed 10-19ms`）。
2. 关闭时整块挂 `clearAndSetSemantics {}`，展开瞬间摘掉，无障碍服务立刻取整棵子树，
   产生一次 17KB+ 跨进程事务（`Large outgoing transaction of 17624 bytes`）。
3. `ConversationHistoryRow` 每行都注册 `onGloballyPositioned`（长按菜单定位用），
   首次布局时可见行各算一次窗口坐标。

**改动**：

- `glassReady` 初始跟随 `isRevealed`；首次展开先 `withFrameNanos { }` 让动画第一帧先出，
  再建离屏层，建好一直复用；
- 行坐标回调改成 `if (showActionMenu)` 时才挂。

**验证**：`dumpsys gfxinfo` 总帧 7546 → 5027、Janky 43(0.57%) → 13(0.26%)；
主观确认首次展开不再卡。

---

## fork.23（2026-11-21）固定签名

**现象**：每次构建出来的 APK 签名都不一样，覆盖安装只靠 CorePatch 忽略校验。

**定位**：CI 里 `keytool -genkeypair` 每次现场生成新密钥对。

**改动**：本地生成一把 RSA 4096 / 30 年密钥，base64 存进 `mariusarva/eta-ci` 的
secret `ETA_KEYSTORE_B64`，workflow 改为从 secret 还原 jks。密钥另备份到私有仓库
`mariusarva/eta-signing`。**从这一版起签名固定**；与更早版本仍不同，那一次升级仍依赖 CorePatch。

---

## fork.22（2026-11-21）单条删除修复

**现象**：删一条消息，它后面所有对话跟着消失。

**定位**：工具条的删除走 `deleteMessageTurn` → `deleteFromTurn`，
后者 `messages.take(boundary.userMessageIndex)` 以**用户轮次**为边界截断
——这是"从这轮重来"的语义，用在"重新生成"上对，用在"删除"上就是误删。

**改动**：新增 `AgentConversationRevisionReducer.deleteSingle`，只移除目标消息，
同步移除 `history`/`journal` 里同 `messageId` 的条目，并清掉因此失去 `tool_calls`
归属的 `tool` 结果（否则下一次请求会因协议不合法被服务端拒绝）。
`AgentAppState.deleteSingleMessage` 负责落库与侧栏刷新。轮次列表的删除仍按整轮语义，带确认。

---

## fork.21（2026-11-21）版本号递增

**现象**：装完新包，设置里版本号看起来反而"退回"了。

**定位**：`versionCode` 固定成常量 `2026111302`。

**改动**：`versionCode = 2026112100 + forkVersion`，`versionName = 3.4.4-fork.N`，
N 由 `-Peta.forkVersion` 传入。

---

## fork.20（2026-11-20）对话列表无障碍语义合并

流式期间对话列表反复重建语义树，造成大 Binder 事务与掉帧。把列表整块合并语义
（`mergeDescendants`），流式期间不再逐条暴露。中途有一次括号位置写错导致编译失败
（`9a04ec4`），最终基于干净文件重新落地（`9d745bd`）。

---

## fork.18 / 17 / 16 / 15 / 14（2026-11-20）会话分类与通知

- **分类**：新表 `conversation_categories` + `conversations.category_id`（DB 29 → 30）。
  顶栏三个按钮（分类 / 多选 / 新建）；分类面板支持建、删（带确认）、改名、上下移动；
  侧栏按分类分组、可折叠；分类组标题右侧「+ 新会话」（首条消息落库时自动归类）；
  多选批量移动 / 删除。
- **踩坑**：`MIGRATION_29_30` 建了 `index_conversations_category_id`，但 `ConversationEntity`
  没用 `@Entity(indices = ...)` 声明，Room 打开时结构比对失败，**每次启动闪退**（`c76c46b` 修）。
  那次正是「迁移前自动快照」第一次真正派上用场，`files/db-snapshot/` 里留下 3 份原始库。
- **分类面板**：行内四个动作挤爆窄侧栏 → 改紧凑胶囊；分类多了滚不动 → 列表加
  `heightIn(max = 300.dp)` + `verticalScroll`，整块 `imePadding()` 避键盘。
- **通知**：运行通知加「暂停 / 继续」（`MSG_PAUSE` / `MSG_RESUME`）与「插入」（`SupplementActivity`
  独立栈透明弹层，`steerRun` → `MSG_STEER`）；文案精简为「插入」「停止」；
  设置里可关掉卡片右上角大图标（`ExecutionNotificationPolicy` 缓存，避免每次重建都读 DataStore）。

---

## fork.13 / 12（2026-11-20）界面收紧

- 侧栏顶栏三按钮 40 → 34dp，间距 6 → 4dp，图标 19 → 17/18dp；
- 批量条改单行紧凑胶囊，修掉窄侧栏下「已选 N 项」被挤成竖排；
- 侧栏首屏 `contentPadding.top` 从「顶栏实测高度 + 10dp」改为 `+16dp`
  —— 顶栏高度由 Layout 测量，第一帧用的还是旧值，会把 Skills 卡片压掉一角。

同时合并上游 PR #1（自定义 base 留空不再回落 provider systemPrompt）、
PR #2（REPLACE + 空 base 时不下发内置行为规范）、PR #3（新建会话自动绑定角色卡）。

---

## fork.11（2026-11-19）轮次序号

**现象**：130 轮的会话，序号出现 `12 12 12 12 12 13`；总轮数也比实际少。

**定位**：

- 序号取 `ChatTurnEntry.index`，而 `LazyColumn` 的 key 用用户消息 id；
  内存里同一 id 可能出现多条（编辑、运行中注入 `user-<runId>-supplement-<n>`），
  重复 key 让 Compose 复用同一行状态，序号串位；
- `LaunchedEffect(conversationId, isStreaming)` 在流式期间直接 `return`，
  拿不到全库轮次，回落到 128 条分页窗口切分。

**改动**：序号按列表位置生成（`position + 1`），key 改 `"<position>:<messageId>"`；
流式期间也加载全库轮次。

---

## fork.10（2026-11-19）全量备份 + 迁移快照

- **全量包**：`EtaFullBackupArchive` 打 zip（`eta-backup.json` + `private/` 下的
  databases / shared_prefs / files，跳过 `files/terminal*`）+ `manifest.json`。
  导入走 `files/restore-staging`，重启后进程最早期覆盖；覆盖 `databases/*` 前先删
  `-wal` / `-shm` / `-journal`，避免旧库配新日志导致 SQLite 校验失败。
- **迁移前自动快照**：打开库前读 `PRAGMA user_version`，与当前 schema 不一致就
  `wal_checkpoint(TRUNCATE)` 后把 `eta.db` 复制到 `files/db-snapshot/eta-v<旧版本>-<时间>.db`，
  保留 3 份。起因是 `fallbackToDestructiveMigration(dropAllTables = true)`
  会在版本对不上时**直接清空所有表且无提示**。
- **轮次列表读全库**：改读用户消息摘要（`id + 前 80 字 + created_at`），
  不再受 128 条分页限制；点未加载的轮次会自动往前加载到该条再滚动过去（上限 80 页）。

---

## fork.9（2026-11-20）自动备份支持自选文件夹

`AutoBackupSettings.directoryUri` 存 SAF 目录树，`DocumentFile` 读写；
没选时用默认目录（有「所有文件访问权限」写 `/storage/emulated/0/Eta备份/`，
否则回落应用私有目录，界面显示实际路径）。

---

## fork.8（2026-11-19）自动备份

`AutoBackupAlarm`（`setAndAllowWhileIdle` 排一次，跑完再排）+ `AutoBackupReceiver`
（开机 / 时间变更 / 覆盖安装后重排）+ `AutoBackupRunner`（指纹跳过、`.part` 再改名、
保留份数、失败落状态）。串行锁保证闹钟与「立即备份」不会同时写两份。

---

## fork.7 / 6（2026-11-19）时间戳位置

三处时间戳从「与标题挤一行」改成「独立一行放标题下方」：轮次列表行、会话列表行、
用户消息气泡。原因是同行会挤压标题宽度，长标题直接被截断。

`MIGRATION_28_29` 回填历史消息时间：按会话 `created_at → updated_at` 区间在
`sort_index` 上线性插值——**这是估算值**，升级前没有逐条时间戳。

---

## fork.1 – fork.5（2026-11-19）

提示词预设（COUI 三段式）、GitHub 账号（设备码授权）、暂停/继续与运行中注入、
离线语音三引擎、消息快捷菜单、设置页分隔线、检查更新指向本 fork。
