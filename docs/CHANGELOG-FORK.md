# Eta fork 开发日志

记录本 fork 相对上游 `Mangi-11/Eta` 的全部改动。每条按「现象 → 定位 → 改动」写，
失败的尝试也保留，便于回溯。时间线倒序，最新的在最上面。

版本号规则：`versionName = 3.4.4-fork.N`，`versionCode = 2026112100 + N`。
签名固定（RSA 4096，见 `mariusarva/eta-signing`）。

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
