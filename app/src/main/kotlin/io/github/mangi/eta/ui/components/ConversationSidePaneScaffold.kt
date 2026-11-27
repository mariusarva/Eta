package io.github.mangi.eta.ui.components

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import io.github.mangi.eta.R
import io.github.mangi.eta.ui.app.LocalEtaTopLayer
import io.github.mangi.eta.ui.model.ConversationPaneUiState
import io.github.mangi.eta.ui.model.ConversationSummaryUi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.theme.MiuixTheme

private object ConversationPaneMetrics {
    val PaneMaxWidth = 340.dp
    val PaneWidthFraction = 0.84f
    const val ChatScrimAlpha = 0.16f
    const val SettleDampingRatio = 1f
    const val SettleStiffness = 146f
    const val SettleVisibilityThresholdPx = 0.5f
    const val SettlePositionThresholdFraction = 0.5f
}

@Composable
internal fun ConversationSidePaneScaffold(
    state: ConversationPaneUiState,
    visible: Boolean,
    /** 当前是否停在对话页；只有对话页右滑才打开侧栏，文件页右滑留给翻页。 */
    swipeToOpen: Boolean = true,
    backHandlerEnabled: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    onSearchChange: (String) -> Unit,
    onNewConversation: () -> Unit = {},
    onConversationSelected: (String) -> Unit,
    onConversationTogglePin: (ConversationSummaryUi) -> Unit,
    onConversationRename: (ConversationSummaryUi) -> Unit,
    onConversationExport: (ConversationSummaryUi) -> Unit,
    onConversationDelete: (ConversationSummaryUi) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenModelProviders: () -> Unit,
    onOpenPromptStudio: () -> Unit,
    onOpenSkills: () -> Unit,
    onOpenScheduledTasks: () -> Unit = {},
    onFilterCategory: (String?) -> Unit = {},
    onCreateCategory: (String) -> Unit = {},
    onRenameCategory: (String, String) -> Unit = { _, _ -> },
    onDeleteCategory: (String) -> Unit = {},
    onMoveCategory: (String, Int) -> Unit = { _, _ -> },
    onDeleteConversations: (List<String>) -> Unit = {},
    onMoveConversationsToCategory: (List<String>, String?) -> Unit = { _, _ -> },
    onNewConversationInCategory: (String?) -> Unit = {},
    onOpenMemory: () -> Unit = {},
    onOpenMcpServers: () -> Unit = {},
    onOpenCategories: () -> Unit = {},
    onPickCharacterForNewConversation: (() -> Unit)? = null,
    selectedCharacter: RoleplayChatUi? = null,
    onOpenCharacters: () -> Unit,
    onOpenPermissions: () -> Unit,
    modifier: Modifier = Modifier,
    activeModelLabel: String? = null,
    hasPermissionAttention: Boolean = false,
    content: @Composable () -> Unit,
) {
    val sceneLifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    val navigationEventState = rememberNavigationEventState(NavigationEventInfo.None)
    val existingTopLayer = LocalEtaTopLayer.current
    val fallbackTopLayer = if (existingTopLayer == null) remember { EtaTopLayerState() } else null
    val activeTopLayer = existingTopLayer ?: fallbackTopLayer!!

    CompositionLocalProvider(LocalEtaTopLayer provides activeTopLayer) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val paneWidth = minOf(maxWidth * ConversationPaneMetrics.PaneWidthFraction, ConversationPaneMetrics.PaneMaxWidth)
        val paneWidthPx = with(density) { paneWidth.toPx() }
        val anchors = remember(paneWidthPx) {
            DraggableAnchors {
                ConversationPaneAnchor.Closed at 0f
                ConversationPaneAnchor.Open at paneWidthPx
            }
        }
        val paneDragState = remember {
            AnchoredDraggableState(
                initialValue = if (visible) ConversationPaneAnchor.Open else ConversationPaneAnchor.Closed,
                anchors = anchors,
            )
        }
        val settleAnimation = remember {
            spring<Float>(
                dampingRatio = ConversationPaneMetrics.SettleDampingRatio,
                stiffness = ConversationPaneMetrics.SettleStiffness,
                visibilityThreshold = ConversationPaneMetrics.SettleVisibilityThresholdPx,
            )
        }
        val currentVisible by rememberUpdatedState(visible)
        val currentOnOpen by rememberUpdatedState(onOpen)
        val currentOnDismiss by rememberUpdatedState(onDismiss)
        val currentSwipeToOpen by rememberUpdatedState(swipeToOpen)
        // 内层横向滚动（表格 / 代码块 / 公式）方向声明：置位时这条全屏手势让位。
        val horizontalScrollLock = remember { HorizontalScrollLock() }
        // 边缘横滑手势已移除：对话页三页 Pager 需要独占横向手势，
        // 侧栏改由顶栏历史按钮程序化开合。这里保留 isDragging 的恒 false 语义，
        // 让下方 conversationPaneSettledValues 仍按"已稳定"路径收敛。
        val isDragging = false
        val isRevealed by remember(paneDragState) {
            derivedStateOf {
                val offset = paneDragState.offset
                !offset.isNaN() && offset > 0.5f
            }
        }
        val panelListState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        val focusManager = LocalFocusManager.current
        val keyboard = LocalSoftwareKeyboardController.current
        val closePane: () -> Unit = {
            focusManager.clearFocus()
            keyboard?.hide()
            if (currentVisible) {
                currentOnDismiss()
            } else {
                scope.launch { paneDragState.animateTo(ConversationPaneAnchor.Closed, settleAnimation) }
            }
        }
        LaunchedEffect(isRevealed) {
            if (isRevealed) {
                focusManager.clearFocus()
                keyboard?.hide()
            }
        }

        SideEffect {
            paneDragState.updateAnchors(anchors)
        }

        LaunchedEffect(visible, paneWidthPx) {
            if (!visible && isRevealed) {
                focusManager.clearFocus()
                keyboard?.hide()
            }
            val target = if (visible) ConversationPaneAnchor.Open else ConversationPaneAnchor.Closed
            if (paneDragState.targetValue != target || paneDragState.settledValue != target) {
                paneDragState.animateTo(target, settleAnimation)
            }
        }

        LaunchedEffect(paneDragState) {
            conversationPaneSettledValues(paneDragState) { isDragging }.collectLatest { settledValue ->
                // 动画途中 settledValue 仍是旧锚点，会和刚被置位的 visible 打架，
                // 出现「刚打开又被关回去」，表现为第一次滑没反应、要滑两次（实测）。
                if (settledValue != paneDragState.targetValue) return@collectLatest
                val settledOpen = settledValue == ConversationPaneAnchor.Open
                if (settledOpen != currentVisible) {
                    if (settledOpen) {
                        currentOnOpen()
                    } else {
                        focusManager.clearFocus()
                        keyboard?.hide()
                        currentOnDismiss()
                    }
                }
            }
        }

        // NavDisplay 的退出条目在转场期间仍会保留组合；仅允许已稳定显示的首页
        // 处理侧栏返回，避免它抢先消费二级页面的第一次返回事件。
        NavigationBackHandler(
            state = navigationEventState,
            isBackEnabled = isRevealed &&
                backHandlerEnabled &&
                sceneLifecycleState == Lifecycle.State.RESUMED,
            onBackCompleted = closePane,
        )

        // 把横向滚动标记提供给会话内容里的表格 / 代码块 / 公式，
        // 全屏右滑手势据此让位，不会把表格的横向拖动抢成「打开侧栏」。
        CompositionLocalProvider(LocalHorizontalScrollLock provides horizontalScrollLock) {
        Box(
            modifier = Modifier.fillMaxSize()
                .clipToBounds()
                .background(conversationPaneContainerColor())
                // 侧栏双向横滑，挂在「侧栏面板 + 会话内容」的共同祖先上：
                // · 展开时左滑 40dp 关闭；
                // · 未展开时右滑 220dp 打开（仅对话页），必须真实移动够远。
                // 状态只看 visible，不看退场动画的 offset，所以关掉之后不管动画走到哪，
                // 第一次右滑就能重新打开；也不依赖焦点落在哪个子节点。
                .pointerInput(Unit) {
                    // 阈值必须按 dp 折算成像素：之前直接把 px 当 dp 用，
                    // 220「dp」实际只有 84dp（屏幕 18%），一滑就开。
                    val openThresholdPx = with(density) { OPEN_SWIPE_DP.toPx() }
                    val closeThresholdPx = with(density) { CLOSE_SWIPE_DP.toPx() }
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitPointerEvent(PointerEventPass.Initial)
                                .changes.firstOrNull { it.changedToDownIgnoreConsumed() }
                                ?: continue
                            val downTime = android.os.SystemClock.uptimeMillis()
                            android.util.Log.i(
                                "EtaPaneSwipe",
                                "down x=${down.position.x} visible=$currentVisible open=$currentSwipeToOpen hl=${horizontalScrollLock.isActive()}",
                            )
                            var totalX = 0f
                            var totalY = 0f
                            var handled = false
                            var moveFrames = 0
                            var horizontalIntent = false
                            var blockedLogged = false
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == down.id }
                                    ?: break
                                if (!change.pressed) break
                                moveFrames++
                                // 绝对位移：直接以按下点为基准，不逐帧累加 previousPosition。
                                // 逐帧累加时，只要某一帧 previousPosition 异常（回零、跨手势残留、
                                // 多指切换），就会出现一次巨大的 delta，轻轻一点也瞬间越过阈值——
                                // 这正是「轻点一下侧栏就开」的根因。
                                totalX = change.position.x - down.position.x
                                totalY = change.position.y - down.position.y
                                if (!handled) {
                                    // 至少要移动 5 帧（约 80ms）才允许判定，杜绝单帧跳变触发。
                                    if (moveFrames < 5) continue
                                    // 长按判定：按下后超过 800ms 还没滑够就放弃。
                                    // 之前 400ms 太短——正常滑动稍微慢一点就被判成放弃，
                                    // 手势又不 consume，长按识别器随即弹出快捷菜单。
                                    if (android.os.SystemClock.uptimeMillis() - downTime > 800L) {
                                        break
                                    }
                                    // 方向锁定后再也不因纵向分量放弃：
                                    // 起手几帧内 totalX 还很小，只要带一点纵向抖动就会被判成
                                    // 「纵向滚动」整段放弃，表现为要滑两次才成功（实测）。
                                    val horizontalDominant = kotlin.math.abs(totalX) > 28f &&
                                        kotlin.math.abs(totalX) > kotlin.math.abs(totalY)
                                    if (!horizontalDominant &&
                                        kotlin.math.abs(totalY) > 16f &&
                                        kotlin.math.abs(totalY) > kotlin.math.abs(totalX) * 1.5f
                                    ) {
                                        android.util.Log.i(
                                            "EtaPaneSwipe",
                                            "give up vertical dx=$totalX dy=$totalY",
                                        )
                                        break
                                    }
                                    // 文件页（未展开且 swipeToOpen=false）：这一层完全不介入。
                                    // 之前这里照样 consume，把 Pager 的翻页手势挡在外面，
                                    // 表现就是「从文件区滑回对话页经常失灵」。
                                    if (!currentVisible && !currentSwipeToOpen) {
                                        android.util.Log.i(
                                            "EtaPaneSwipe",
                                            "skip: file page, hand over to pager",
                                        )
                                        break
                                    }
                                    // 一旦判定为水平意图，就吃掉移动事件：
                                    // 否则长按识别器照常收到位移，滑动途中弹出快捷菜单。
                                    if (horizontalDominant) horizontalIntent = true
                                    if (horizontalIntent) change.consume()
                                    if (!currentVisible && totalX > openThresholdPx &&
                                        horizontalScrollLock.isActive() && !blockedLogged
                                    ) {
                                        blockedLogged = true
                                        android.util.Log.i("EtaPaneSwipe", "blocked by horizontal scroll")
                                    }
                                    if (currentVisible) {
                                        if (totalX < -closeThresholdPx &&
                                            kotlin.math.abs(totalX) > kotlin.math.abs(totalY) * 2.2f
                                        ) {
                                            handled = true
                                            android.util.Log.i("EtaPaneSwipe", "close dx=$totalX")
                                            closePane()
                                        }
                                    } else if (currentSwipeToOpen && !horizontalScrollLock.isActive()) {
                                        if (totalX > openThresholdPx && totalX > kotlin.math.abs(totalY) * 2.2f) {
                                            handled = true
                                            android.util.Log.i("EtaPaneSwipe", "open dx=$totalX")
                                            currentOnOpen()
                                            // 不依赖重组后的 LaunchedEffect：直接驱动锚点动画，
                                            // 避免打开请求与退场动画互相顶。
                                            scope.launch {
                                                paneDragState.animateTo(
                                                    ConversationPaneAnchor.Open,
                                                    settleAnimation,
                                                )
                                            }
                                        }
                                    }
                                }
                                if (handled) change.consume()
                            }
                        }
                    }
                },
            contentAlignment = AbsoluteAlignment.TopLeft,
        ) {
            // 两个槽位并排测量和放置，由同一位移推移；不在聊天内容下面叠放侧栏。
            // 侧栏保留组合，避免每次起手重新创建列表和搜索框。
            Layout(
                modifier = Modifier.fillMaxSize(),
                content = {
                    ConversationPanePanel(
                        state = state,
                        width = paneWidth,
                        listState = panelListState,
                        isRevealed = isRevealed,
                        activeModelLabel = activeModelLabel,
                        hasPermissionAttention = hasPermissionAttention,
                        onSearchChange = onSearchChange,
                        onNewConversation = onNewConversation,
                        onConversationSelected = onConversationSelected,
                        onConversationTogglePin = onConversationTogglePin,
                        onConversationRename = onConversationRename,
                        onConversationExport = onConversationExport,
                        onConversationDelete = onConversationDelete,
                        onOpenSettings = onOpenSettings,
                        onOpenModelProviders = onOpenModelProviders,
                        onOpenPromptStudio = onOpenPromptStudio,
                        onOpenSkills = onOpenSkills,
                        onOpenScheduledTasks = onOpenScheduledTasks,
                        onFilterCategory = onFilterCategory,
                        onCreateCategory = onCreateCategory,
                        onRenameCategory = onRenameCategory,
                        onDeleteCategory = onDeleteCategory,
                        onMoveCategory = onMoveCategory,
                        onDeleteConversations = onDeleteConversations,
                        onMoveConversationsToCategory = onMoveConversationsToCategory,
                        onNewConversationInCategory = onNewConversationInCategory,
                        onOpenMemory = onOpenMemory,
                        onOpenMcpServers = onOpenMcpServers,
                        onOpenCategories = onOpenCategories,
                        onPickCharacterForNewConversation = onPickCharacterForNewConversation,
                        selectedCharacter = selectedCharacter,
                        onOpenCharacters = onOpenCharacters,
                        onOpenPermissions = onOpenPermissions,
                        modifier = Modifier
                            .focusProperties { onEnter = { if (!isRevealed) cancelFocusChange() } }
                            .focusGroup()
                            .then(if (!isRevealed) Modifier.clearAndSetSemantics {} else Modifier),
                    )
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .drawWithContent {
                                drawContent()
                                val progress = conversationPaneMotion(paneDragState.offset, paneWidthPx).progress
                                drawRect(Color.Black, alpha = ConversationPaneMetrics.ChatScrimAlpha * progress)
                            }
                            .background(MiuixTheme.colorScheme.background),
                    ) {
                        Box(
                            modifier = Modifier
                                .focusProperties {
                                    onEnter = { if (isRevealed) cancelFocusChange() }
                                }
                                .focusGroup()
                                .then(if (isRevealed) Modifier.clearAndSetSemantics {} else Modifier),
                        ) {
                            content()
                        }
                        // 遮罩只在「已展开且仍然打开」时存在。退场动画期间 offset 还没归零，
                        // isRevealed 仍是 true；此时如果遮罩还在，它会盖住整个会话区并成为
                        // 触摸目标，用户从侧栏滑回会话区后第一次右滑会被它吞掉（实测）。
                        if (isRevealed && currentVisible) {
                            Box(
                                modifier = Modifier.fillMaxSize().clickable(
                                    interactionSource = null,
                                    indication = null,
                                    onClickLabel = stringResource(R.string.action_close),
                                    onClick = closePane,
                                ),
                            )
                        }
                    }
                },
            ) { measurables, constraints ->
                val panel = measurables[0].measure(
                    Constraints.fixed(paneWidthPx.fastRoundToInt(), constraints.maxHeight),
                )
                val page = measurables[1].measure(
                    Constraints.fixed(constraints.maxWidth, constraints.maxHeight),
                )
                layout(constraints.maxWidth, constraints.maxHeight) {
                    // 物理左侧是会话区；不随 RTL 改变两个槽位的关系。
                    val motion = conversationPaneMotion(paneDragState.offset, paneWidthPx)
                    val offset = motion.foregroundOffset.fastRoundToInt()
                    panel.placeWithLayer(offset - panel.width, 0)
                    page.placeWithLayer(offset, 0)
                }
            }
        }
        }
        if (fallbackTopLayer != null) {
            EtaTopLayerHost(fallbackTopLayer)
        }
    }
    }
}

/** 打开侧栏所需水平位移（dp）：约占屏幕宽 40%，必须明确滑一段。 */
private val OPEN_SWIPE_DP = 150.dp

/** 关闭侧栏所需水平位移（dp）。 */
private val CLOSE_SWIPE_DP = 120.dp
