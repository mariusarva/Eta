package io.github.mangi.eta.ui.components

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import io.github.mangi.eta.ui.components.EtaPreference
import io.github.mangi.eta.ui.components.EtaPreferenceDivider
import io.github.mangi.eta.ui.components.EtaPreferenceGroup
import io.github.mangi.eta.ui.components.EtaTextButton
import io.github.mangi.eta.ui.components.EtaWindowDialog
import io.github.mangi.eta.ui.model.ConversationCategoryUi
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.dialog.CouiDialogDefaults
import com.itos.coui.effect.couiButtonFeedback
import com.itos.coui.icons.CouiIcons
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Divider as CouiDivider
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.Menu
import com.itos.coui.menu.couiMenuAnchor
import com.itos.coui.menu.rememberCouiMenuAnchor
import com.itos.coui.shape.CouiShape
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.Text as CouiText
import com.itos.coui.theme.CouiTheme
import io.github.mangi.eta.ui.app.ConversationTimeLabels
import io.github.mangi.eta.R
import io.github.mangi.eta.data.model.AppearanceUiStyle
import io.github.mangi.eta.ui.app.LocalUiStyle
import io.github.mangi.eta.ui.components.coui.CouiIcon
import io.github.mangi.eta.ui.model.ConversationPaneUiState
import io.github.mangi.eta.ui.model.ConversationSummaryUi
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassPopup
import top.yukonga.miuix.kmp.glass.GlassPopupDefaults
import top.yukonga.miuix.kmp.glass.GlassPopupItem
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PushPin
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Delete
import top.yukonga.miuix.kmp.icon.glass.Download
import top.yukonga.miuix.kmp.icon.glass.Edit
import top.yukonga.miuix.kmp.icon.glass.FolderOutline
import top.yukonga.miuix.kmp.icon.glass.More
import top.yukonga.miuix.kmp.icon.glass.Pin
import top.yukonga.miuix.kmp.icon.glass.Search
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Stable
private class PaneBarInsets {
    var top by mutableStateOf(0.dp)
    var bottom by mutableStateOf(0.dp)
}

@Composable
internal fun ConversationPanePanel(
    state: ConversationPaneUiState,
    width: Dp,
    listState: LazyListState,
    isRevealed: Boolean,
    activeModelLabel: String?,
    hasPermissionAttention: Boolean,
    onSearchChange: (String) -> Unit,
    onNewConversation: () -> Unit,
    onConversationSelected: (String) -> Unit,
    onConversationTogglePin: (ConversationSummaryUi) -> Unit,
    onConversationRename: (ConversationSummaryUi) -> Unit,
    onConversationExport: (ConversationSummaryUi) -> Unit,
    onConversationDelete: (ConversationSummaryUi) -> Unit,
    onCreateCategory: (String) -> Unit = {},
    onRenameCategory: (String, String) -> Unit = { _, _ -> },
    onDeleteCategory: (String) -> Unit = {},
    onMoveConversationsToCategory: (List<String>, String?) -> Unit = { _, _ -> },
    onFilterCategory: (String?) -> Unit = {},
    onDeleteConversations: (List<String>) -> Unit = {},
    onMoveCategory: (String, Int) -> Unit = { _, _ -> },
    onNewConversationInCategory: (String) -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenModelProviders: () -> Unit,
    onOpenTools: () -> Unit,
    onOpenSkills: () -> Unit,
    onOpenCharacters: () -> Unit,
    onOpenPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // state.conversations 已由 AgentAppState 按标题、预览与消息内容过滤。
    val query = state.searchQuery.trim()
    val isSearching = query.isNotBlank()
    val groups = remember(state.conversations, state.categories) {
        state.conversations.groupForDrawer(state.categories)
    }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var selecting by remember { mutableStateOf(false) }
    var collapsedSections by remember { mutableStateOf(setOf<String>()) }
    var renamingCategoryId by remember { mutableStateOf<String?>(null) }
    var renameCategoryText by remember { mutableStateOf("") }
    var pendingDeleteCategoryId by remember { mutableStateOf<String?>(null) }
    val selectedConversationIds = remember { mutableStateMapOf<String, Boolean>() }
    val containerColor = conversationPaneContainerColor()
    val isCoui = LocalUiStyle.current == AppearanceUiStyle.COUI
    // 玻璃离屏层（RenderNode + RuntimeShader）首次创建要现编译 pipeline，是首次展开卡顿的主因。
    // 侧栏从未展开过之前不创建；展开一次后置位并一直复用，后续展开零成本。
    var glassReady by remember { mutableStateOf(isRevealed) }
    LaunchedEffect(isRevealed) {
        if (isRevealed && !glassReady) {
            // 让当前帧先完成展开动画的第一帧，再建离屏层，避免和动画抢同一个帧。
            withFrameNanos { }
            glassReady = true
        }
    }
    val couiBackdrop = if (isCoui && glassReady) rememberCouiBackdrop() else null
    val miuixBackdrop = if (!isCoui && glassReady) rememberGlassBackdrop(containerColor) else null
    val barInsets = remember { PaneBarInsets() }
    val bodyCaptureModifier = when {
        isCoui && couiBackdrop != null -> Modifier.couiBackdrop(couiBackdrop)
        !isCoui && miuixBackdrop != null -> Modifier.captureGlassBackdrop(miuixBackdrop)
        else -> Modifier
    }

    Box(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .background(containerColor),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 测量顶栏与底栏真实高度（含安全区与 IME），同步给滚动画布的 contentPadding，
            // 使列表自然穿梭于顶底浮层之下并分别被各自 backdrop 采样，同时避免 Subcompose 重组开销。
            Layout(
                modifier = Modifier.fillMaxSize(),
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(containerColor)
                            .then(bodyCaptureModifier),
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds()
                                .windowInsetsPadding(
                                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                                )
                                .scrollEndHaptic()
                                .overScrollVertical(),
                            contentPadding = PaddingValues(
                                // 顶栏高度测量有半帧延迟，多留 6dp 缓冲，避免首屏卡片被顶栏压住。
                                top = barInsets.top + 16.dp,
                                bottom = barInsets.bottom + 10.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(ConversationPanelMetrics.RowGap),
                            overscrollEffect = null,
                        ) {
                            if (!isSearching) {
                                item(key = "pane_workbench") {
                                    PaneWorkbenchSection(
                                        activeModelLabel = activeModelLabel,
                                        onOpenModelProviders = onOpenModelProviders,
                                        onOpenCharacters = onOpenCharacters,
                                        onOpenTools = onOpenTools,
                                        onOpenSkills = onOpenSkills,
                                    )
                                }
                            }

                            if (state.conversations.isEmpty()) {
                                item(key = "pane_empty_conversations") {
                                    EmptyConversations(isSearching = isSearching)
                                }
                            } else {
                                groups.forEachIndexed { groupIndex, group ->
                                    val sectionKey = group.section.toString()
                                    val collapsed = sectionKey in collapsedSections
                                    item(key = "section-${group.section}") {
                                        ConversationSectionHeader(
                                            group = group,
                                            isFirstSection = groupIndex == 0,
                                            hasWorkbenchAbove = !isSearching,
                                            collapsed = collapsed,
                                            onToggleCollapse = {
                                                collapsedSections = if (collapsed) {
                                                    collapsedSections - sectionKey
                                                } else {
                                                    collapsedSections + sectionKey
                                                }
                                            },
                                            onNewConversationInCategory = { categoryId ->
                                                onNewConversationInCategory(categoryId)
                                            },
                                        )
                                    }
                                    if (collapsed) return@forEachIndexed
                                    items(
                                        items = group.items,
                                        key = { it.id },
                                    ) { conversation ->
                                        ConversationHistoryRow(
                                            conversation = conversation,
                                            selected = if (selecting) {
                                                selectedConversationIds[conversation.id] == true
                                            } else {
                                                conversation.id == state.selectedConversationId
                                            },
                                            miuixBackdrop = miuixBackdrop,
                                            onClick = {
                                                if (selecting) {
                                                    if (selectedConversationIds[conversation.id] == true) {
                                                        selectedConversationIds.remove(conversation.id)
                                                    } else {
                                                        selectedConversationIds[conversation.id] = true
                                                    }
                                                } else {
                                                    onConversationSelected(conversation.id)
                                                }
                                            },
                                            onTogglePin = { onConversationTogglePin(conversation) },
                                            onRename = { onConversationRename(conversation) },
                                            onExport = { onConversationExport(conversation) },
                                            onDelete = { onConversationDelete(conversation) },
                                            categories = state.categories,
                                            onMove = { id -> onMoveConversationsToCategory(listOf(conversation.id), id) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                    ConversationPaneTopBar(
                        containerColor = containerColor,
                        couiBackdrop = couiBackdrop,
                        miuixBackdrop = miuixBackdrop,
                        onNewConversation = onNewConversation,
                        onManageCategories = { showCategoryDialog = true },
                        isSelecting = selecting,
                        onToggleSelecting = {
                            selecting = !selecting
                            if (!selecting) selectedConversationIds.clear()
                        },
                    )
                    if (selecting) {
                        ConversationBulkBar(
                            selectedCount = selectedConversationIds.size,
                            onMove = { if (selectedConversationIds.isNotEmpty()) showMoveDialog = true },
                            onDelete = {
                                val ids = selectedConversationIds.keys.toList()
                                if (ids.isNotEmpty()) {
                                    onDeleteConversations(ids)
                                    selectedConversationIds.clear()
                                    selecting = false
                                }
                            },
                            onClear = {
                                selectedConversationIds.clear()
                                selecting = false
                            },
                        )
                    } else ConversationPaneBottomDock(
                        query = state.searchQuery,
                        isRevealed = isRevealed,
                        hasPermissionAttention = hasPermissionAttention,
                        containerColor = containerColor,
                        couiBackdrop = couiBackdrop,
                        miuixBackdrop = miuixBackdrop,
                        onSearchChange = onSearchChange,
                        onOpenPermissions = onOpenPermissions,
                        onOpenSettings = onOpenSettings,
                    )
                },
            ) { measurables, constraints ->
                val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
                val topBar = measurables[1].measure(looseConstraints)
                val bottomDock = measurables[2].measure(looseConstraints)
                val topDp = topBar.height.toDp()
                val bottomDp = bottomDock.height.toDp()
                if (barInsets.top != topDp) barInsets.top = topDp
                if (barInsets.bottom != bottomDp) barInsets.bottom = bottomDp
                val body = measurables[0].measure(constraints)
                layout(constraints.maxWidth, constraints.maxHeight) {
                    body.placeRelative(0, 0)
                    topBar.placeRelative(0, 0)
                    bottomDock.placeRelative(0, constraints.maxHeight - bottomDock.height)
                }
            }

            // 侧栏物理右侧细微收边，保证浅/深/纯黑模式下与右侧聊天舞台始终保持利落边界。
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(0.5.dp)
                    .background(conversationPaneEdgeBorderColor()),
            )
        }
    }

    if (showCategoryDialog) {
        EtaWindowDialog(
            show = true,
            title = "会话分类",
            onDismissRequest = { showCategoryDialog = false },
        ) {
            Column(modifier = Modifier.fillMaxWidth().imePadding()) {
                Text(
                    text = "点分类筛选侧栏；删除只解散归属，不删会话",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(start = 2.dp, end = 2.dp, bottom = 10.dp),
                )
                // 分类一多就会超出弹窗高度，这里给列表单独的可滚动区域。
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    EtaPreferenceGroup(modifier = Modifier.fillMaxWidth()) {
                        EtaPreference(
                            title = "全部会话",
                            summary = state.conversations.size.toString() + " 个会话",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onFilterCategory(null)
                                showCategoryDialog = false
                            },
                        )
                        state.categories.forEach { category ->
                            EtaPreferenceDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                EtaPreference(
                                    title = category.name,
                                    summary = category.conversationCount.toString() + " 个会话" +
                                        if (state.categoryFilterId == category.id) " · 筛选中" else "",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        onFilterCategory(category.id)
                                        showCategoryDialog = false
                                    },
                                )
                                Row(
                                    modifier = Modifier.padding(end = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    BulkActionChip(text = "↑", compact = true, onClick = { onMoveCategory(category.id, -1) })
                                    Spacer(Modifier.width(3.dp))
                                    BulkActionChip(text = "↓", compact = true, onClick = { onMoveCategory(category.id, 1) })
                                    Spacer(Modifier.width(3.dp))
                                    BulkActionChip(
                                        text = "改名",
                                        compact = true,
                                        onClick = {
                                            renamingCategoryId = category.id
                                            renameCategoryText = category.name
                                        },
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    BulkActionChip(
                                        text = "×",
                                        compact = true,
                                        onClick = { pendingDeleteCategoryId = category.id },
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        singleLine = true,
                        textStyle = MiuixTheme.textStyles.footnote1.copy(color = MiuixTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                            ) {
                                if (newCategoryName.isEmpty()) {
                                    Text(
                                        text = "新建分类名称",
                                        style = MiuixTheme.textStyles.footnote1,
                                        color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                    )
                                }
                                inner()
                            }
                        },
                    )
                    Spacer(Modifier.width(8.dp))
                    BulkActionChip(
                        text = "新建",
                        enabled = newCategoryName.isNotBlank(),
                        onClick = {
                            onCreateCategory(newCategoryName)
                            newCategoryName = ""
                        },
                    )
                }
            }
        }
    }

    renamingCategoryId?.let { categoryId ->
        EtaWindowDialog(
            show = true,
            title = "重命名分类",
            onDismissRequest = { renamingCategoryId = null },
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                BasicTextField(
                    value = renameCategoryText,
                    onValueChange = { renameCategoryText = it },
                    singleLine = true,
                    textStyle = MiuixTheme.textStyles.footnote1.copy(color = MiuixTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                        ) {
                            if (renameCategoryText.isEmpty()) {
                                Text(
                                    text = "分类名称",
                                    style = MiuixTheme.textStyles.footnote1,
                                    color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                )
                            }
                            inner()
                        }
                    },
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    BulkActionChip(text = "取消", onClick = { renamingCategoryId = null })
                    Spacer(Modifier.width(8.dp))
                    BulkActionChip(
                        text = "保存",
                        enabled = renameCategoryText.isNotBlank(),
                        onClick = {
                            onRenameCategory(categoryId, renameCategoryText)
                            renamingCategoryId = null
                        },
                    )
                }
            }
        }
    }

    pendingDeleteCategoryId?.let { categoryId ->
        EtaWindowDialog(
            show = true,
            title = "删除分类",
            onDismissRequest = { pendingDeleteCategoryId = null },
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "分类里的会话不会被删除，只会变回未分类。",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    BulkActionChip(text = "取消", onClick = { pendingDeleteCategoryId = null })
                    Spacer(Modifier.width(8.dp))
                    BulkActionChip(
                        text = "删除",
                        onClick = {
                            onDeleteCategory(categoryId)
                            pendingDeleteCategoryId = null
                        },
                    )
                }
            }
        }
    }

    if (showMoveDialog) {
        val ids = selectedConversationIds.keys.toList()
        EtaWindowDialog(
            show = true,
            title = "移到分类",
            onDismissRequest = { showMoveDialog = false },
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "已选 " + ids.size + " 个会话",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(start = 2.dp, bottom = 10.dp),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    EtaPreferenceGroup(modifier = Modifier.fillMaxWidth()) {
                        EtaPreference(
                            title = "未分类",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onMoveConversationsToCategory(ids, null)
                                showMoveDialog = false
                                selectedConversationIds.clear()
                                selecting = false
                            },
                        )
                        state.categories.forEach { category ->
                            EtaPreferenceDivider()
                            EtaPreference(
                                title = category.name,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    onMoveConversationsToCategory(ids, category.id)
                                    showMoveDialog = false
                                    selectedConversationIds.clear()
                                    selecting = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationBulkBar(
    selectedCount: Int,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (selectedCount == 0) "点会话开始多选" else "已选 " + selectedCount + " 项",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false,
        )
        Spacer(Modifier.weight(1f))
        BulkActionChip(text = "移到分类", enabled = selectedCount > 0, onClick = onMove)
        Spacer(Modifier.width(6.dp))
        BulkActionChip(text = "删除", enabled = selectedCount > 0, onClick = onDelete)
        Spacer(Modifier.width(6.dp))
        BulkActionChip(text = "取消", onClick = onClear)
    }
}

/** 批量条的紧凑按钮：侧栏宽度有限，用自绘小胶囊代替带最小宽度的 TextButton。 */
@Composable
private fun BulkActionChip(
    text: String,
    enabled: Boolean = true,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(
                MiuixTheme.colorScheme.surfaceContainer,
                RoundedCornerShape(9.dp),
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = if (compact) 7.dp else 9.dp, vertical = if (compact) 4.dp else 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.92f else 0.35f),
            maxLines = 1,
            softWrap = false,
        )
    }
}

private data class WorkbenchEntry(
    val icon: ImageVector,
    val title: String,
    val trailingValue: String? = null,
    val onClick: () -> Unit,
)

@Composable
private fun PaneWorkbenchSection(
    activeModelLabel: String?,
    onOpenModelProviders: () -> Unit,
    onOpenCharacters: () -> Unit,
    onOpenTools: () -> Unit,
    onOpenSkills: () -> Unit,
) {
    val modelSummary = activeModelLabel?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.conversation_model_unconfigured)
    val entries = listOf(
        WorkbenchEntry(
            icon = ConversationPaneIcons.Model,
            title = stringResource(R.string.conversation_dock_models),
            trailingValue = modelSummary,
            onClick = onOpenModelProviders,
        ),
        WorkbenchEntry(
            icon = ConversationPaneIcons.Character,
            title = stringResource(R.string.conversation_dock_characters),
            onClick = onOpenCharacters,
        ),
        WorkbenchEntry(
            icon = ConversationPaneIcons.Tools,
            title = stringResource(R.string.route_tools),
            onClick = onOpenTools,
        ),
        WorkbenchEntry(
            icon = ConversationPaneIcons.Skills,
            title = stringResource(R.string.route_skills),
            onClick = onOpenSkills,
        ),
    )

    val cardColor = conversationPaneCardColor()
    val dividerIndent = ConversationPanelMetrics.WorkbenchRowHorizontalPadding +
        ConversationPanelMetrics.WorkbenchIconSize +
        ConversationPanelMetrics.WorkbenchIconGap

    when (LocalUiStyle.current) {
        AppearanceUiStyle.COUI -> {
            val shape = remember { CouiShape(ConversationPanelMetrics.WorkbenchCornerRadius, 2.5f) }
            val dark = CouiTheme.isDark
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ConversationPanelMetrics.PaneHorizontalPadding)
                    .then(
                        if (!dark) {
                            Modifier.dropShadow(
                                shape = shape,
                                shadow = Shadow(
                                    radius = 10.dp,
                                    color = Color.Black,
                                    alpha = 0.025f,
                                ),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .clip(shape)
                    .background(cardColor, shape)
                    .then(
                        if (!dark) {
                            Modifier.border(0.5.dp, Color(0x0A000000), shape)
                        } else {
                            Modifier.border(0.5.dp, Color(0x12FFFFFF), shape)
                        },
                    ),
            ) {
                entries.forEachIndexed { index, entry ->
                    CouiWorkbenchRow(entry = entry, dark = dark)
                    if (index < entries.lastIndex) {
                        CouiDivider(
                            startIndent = dividerIndent,
                            endIndent = ConversationPanelMetrics.WorkbenchRowHorizontalPadding,
                        )
                    }
                }
            }
        }
        AppearanceUiStyle.MIUIX -> {
            val colors = MiuixTheme.colorScheme
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ConversationPanelMetrics.PaneHorizontalPadding),
                colors = CardDefaults.defaultColors(color = cardColor),
            ) {
                entries.forEachIndexed { index, entry ->
                    MiuixWorkbenchRow(entry = entry)
                    if (index < entries.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(
                                start = dividerIndent,
                                end = ConversationPanelMetrics.WorkbenchRowHorizontalPadding,
                            ),
                            thickness = 0.5.dp,
                            color = colors.dividerLine.copy(alpha = 0.45f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CouiWorkbenchRow(
    entry: WorkbenchEntry,
    dark: Boolean,
) {
    val colors = CouiPageDefaults.themeColors(dark)
    val source = remember { MutableInteractionSource() }
    val chevronTint = Color(if (dark) 0x4DFFFFFF else 0x42000000)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = ConversationPanelMetrics.WorkbenchRowMinHeight)
            .couiButtonFeedback(dark, source, enabled = true, list = true)
            .clickable(
                interactionSource = source,
                indication = null,
                role = Role.Button,
                onClick = entry.onClick,
            )
            .padding(
                horizontal = ConversationPanelMetrics.WorkbenchRowHorizontalPadding,
                vertical = 11.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CouiIcon(
            imageVector = entry.icon,
            contentDescription = null,
            modifier = Modifier.size(ConversationPanelMetrics.WorkbenchIconSize),
            tint = colors.title.copy(alpha = 0.86f),
        )
        Spacer(Modifier.width(ConversationPanelMetrics.WorkbenchIconGap))
        CouiText(
            text = entry.title,
            kind = CouiTextKind.ItemTitle,
            color = colors.title,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!entry.trailingValue.isNullOrEmpty()) {
            CouiText(
                text = entry.trailingValue,
                kind = CouiTextKind.ItemValue,
                color = colors.summary,
                modifier = Modifier
                    .widthIn(max = 136.dp)
                    .padding(start = 8.dp),
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        CouiIcon(
            imageVector = CouiIcons.Outlined.Next,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 6.dp)
                .size(width = 11.dp, height = 20.dp),
            tint = chevronTint,
        )
    }
}

@Composable
private fun MiuixWorkbenchRow(entry: WorkbenchEntry) {
    val colors = MiuixTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = ConversationPanelMetrics.WorkbenchRowMinHeight)
            .clickable(role = Role.Button, onClick = entry.onClick)
            .padding(
                horizontal = ConversationPanelMetrics.WorkbenchRowHorizontalPadding,
                vertical = 11.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = entry.icon,
            contentDescription = null,
            modifier = Modifier.size(ConversationPanelMetrics.WorkbenchIconSize),
            tint = colors.onSurface.copy(alpha = 0.85f),
        )
        Spacer(Modifier.width(ConversationPanelMetrics.WorkbenchIconGap))
        Text(
            text = entry.title,
            style = MiuixTheme.textStyles.body1.copy(fontSize = 15.sp),
            color = colors.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!entry.trailingValue.isNullOrEmpty()) {
            Text(
                text = entry.trailingValue,
                style = MiuixTheme.textStyles.footnote1,
                color = colors.onSurfaceVariantSummary,
                modifier = Modifier
                    .widthIn(max = 136.dp)
                    .padding(start = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = ConversationPaneIcons.ChevronRight,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(14.dp),
            tint = colors.onSurfaceVariantActions,
        )
    }
}

@Composable
private fun ConversationSectionHeader(
    group: ConversationDrawerGroup,
    isFirstSection: Boolean,
    hasWorkbenchAbove: Boolean,
    collapsed: Boolean = false,
    onToggleCollapse: () -> Unit = {},
    onNewConversationInCategory: (String) -> Unit = {},
) {
    val label = group.localizedLabel()
    val topPadding = when {
        isFirstSection && hasWorkbenchAbove -> ConversationPanelMetrics.FirstSectionTopPadding
        isFirstSection -> 8.dp
        else -> ConversationPanelMetrics.SectionTopPadding
    }
    val horizontalInset = ConversationPanelMetrics.PaneHorizontalPadding +
        ConversationPanelMetrics.RowHorizontalPadding

    val isCouiHeader = LocalUiStyle.current == AppearanceUiStyle.COUI
    val headerColor = if (isCouiHeader) {
        CouiPageDefaults.themeColors().summary
    } else {
        MiuixTheme.colorScheme.onBackgroundVariant
    }
    val categoryId = (group.section as? ConversationDrawerSection.Category)?.id

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = horizontalInset,
                top = topPadding,
                end = horizontalInset,
                bottom = ConversationPanelMetrics.SectionBottomPadding,
            )
            .clickable(role = Role.Button, onClick = onToggleCollapse)
            .semantics { heading() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (collapsed) "▸" else "▾",
            style = MiuixTheme.textStyles.footnote1,
            color = headerColor,
            modifier = Modifier.padding(end = 4.dp),
        )
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote1,
            fontWeight = FontWeight.Medium,
            color = headerColor,
            maxLines = 1,
        )
        if (categoryId != null && !collapsed) {
            Spacer(Modifier.weight(1f))
            BulkActionChip(
                text = "+ 新会话",
                onClick = { onNewConversationInCategory(categoryId) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationHistoryRow(
    conversation: ConversationSummaryUi,
    selected: Boolean,
    miuixBackdrop: Backdrop?,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onRename: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    /// 这个会话能移到的分类，以及移过去以后做什么（null 是移出分类）。
    categories: List<ConversationCategoryUi> = emptyList(),
    onMove: (String?) -> Unit = {},
) {
    var showActionMenu by remember { mutableStateOf(false) }
    var showMoveMenu by remember { mutableStateOf(false) }
    var rowBounds by remember { mutableStateOf(Rect.Zero) }
    var moreButtonBounds by remember { mutableStateOf(Rect.Zero) }
    var popupAnchorBounds by remember { mutableStateOf(Rect.Zero) }
    val hapticFeedback = LocalHapticFeedback.current
    val anchor = rememberCouiMenuAnchor()
    val isCoui = LocalUiStyle.current == AppearanceUiStyle.COUI
    val couiDark = CouiTheme.isDark
    val couiRowShape = remember { CouiShape(ConversationPanelMetrics.RowCornerRadius, 2.1f) }
    val miuixColors = MiuixTheme.colorScheme
    val miuixDark = miuixColors.surface.luminance() < 0.5f

    val rowBackground = when {
        !selected -> Color.Transparent
        isCoui -> if (couiDark) Color(0xFF262932) else Color.White
        else -> miuixColors.tertiaryContainer.copy(alpha = if (miuixDark) 0.72f else 0.90f)
    }
    val rowModifier = if (isCoui) {
        val source = remember { MutableInteractionSource() }
        Modifier
            .then(
                if (selected && !couiDark) {
                    Modifier.dropShadow(
                        shape = couiRowShape,
                        shadow = Shadow(
                            radius = 8.dp,
                            color = Color.Black,
                            alpha = 0.028f,
                        ),
                    )
                } else {
                    Modifier
                },
            )
            .clip(couiRowShape)
            .background(rowBackground, couiRowShape)
            .then(
                if (selected) {
                    Modifier.border(
                        width = 0.5.dp,
                        color = if (couiDark) Color(0x1AFFFFFF) else Color(0x0D000000),
                        shape = couiRowShape,
                    )
                } else {
                    Modifier
                },
            )
            .couiButtonFeedback(couiDark, source, enabled = true, list = true)
    } else {
        Modifier.squircleSurface(
            color = rowBackground,
            cornerRadius = ConversationPanelMetrics.RowCornerRadius,
        )
    }

    val titleColor = if (isCoui) {
        CouiPageDefaults.themeColors(couiDark).title
    } else {
        miuixColors.onSurface
    }
    val summaryColor = if (isCoui) {
        CouiPageDefaults.themeColors(couiDark).summary
    } else {
        miuixColors.onSurfaceVariantSummary
    }
    val accentColor = if (isCoui) {
        CouiTheme.colorSchemeFor(couiDark).accent
    } else {
        miuixColors.primary
    }
    // 会话时间显示在标题下方，不再与标题争抢同一行宽度。
    val rowTimeLabel = if (conversation.updatedAtMillis > 0L) {
        ConversationTimeLabels.rowLabel(
            timestampMillis = conversation.updatedAtMillis,
            locale = LocalConfiguration.current.locales[0],
            use24HourClock = android.text.format.DateFormat.is24HourFormat(LocalContext.current),
        )
    } else {
        // 时间戳缺失时退回分组用的标签，至少不空着。
        conversation.timeLabel
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ConversationPanelMetrics.PaneHorizontalPadding),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ConversationPanelMetrics.RowMinHeight)
                // 只在菜单真正打开时才需要窗口坐标；未展开时跳过，省掉每行一次坐标换算。
                .then(
                    if (showActionMenu) {
                        Modifier.onGloballyPositioned { rowBounds = it.boundsInWindow() }
                    } else {
                        Modifier
                    }
                )
                .couiMenuAnchor(anchor)
                .then(rowModifier)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        popupAnchorBounds = rowBounds
                        anchor.open { showActionMenu = true }
                    },
                )
                .padding(
                    start = ConversationPanelMetrics.RowHorizontalPadding,
                    end = if (selected) 8.dp else ConversationPanelMetrics.RowHorizontalPadding,
                    top = ConversationPanelMetrics.RowVerticalPadding,
                    bottom = ConversationPanelMetrics.RowVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (conversation.characterName != null) {
                CharacterAvatar(
                    name = conversation.characterName,
                    avatarPath = conversation.characterAvatarPath,
                    size = 30.dp,
                    shape = CircleShape,
                    modifier = if (selected) {
                        Modifier.border(1.5.dp, accentColor.copy(alpha = 0.75f), CircleShape)
                    } else {
                        Modifier
                    },
                )
                Spacer(Modifier.width(11.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                val title = conversation.title.ifBlank { conversation.preview }
                Text(
                    text = title,
                    color = titleColor,
                    style = MiuixTheme.textStyles.body1.copy(fontSize = 15.sp),
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // 标题与角色名相同（如未改名的角色会话）时不再重复第二行。
                conversation.characterName?.takeIf { it != title }?.let { name ->
                    Text(
                        text = name,
                        style = MiuixTheme.textStyles.footnote1.copy(fontSize = 12.sp),
                        color = summaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                conversation.runStatus?.let { status ->
                    Text(
                        text = status,
                        style = MiuixTheme.textStyles.footnote1.copy(fontSize = 12.sp),
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                if (rowTimeLabel.isNotBlank()) {
                    Text(
                        text = rowTimeLabel,
                        style = MiuixTheme.textStyles.footnote1.copy(fontSize = 11.sp),
                        color = summaryColor.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            if (conversation.isActiveRun) {
                Box(
                    modifier = Modifier
                        .padding(start = ConversationPanelMetrics.ActiveDotGap)
                        .size(ConversationPanelMetrics.ActiveDotSize)
                        .clip(CircleShape)
                        .background(accentColor),
                )
            } else if (conversation.isPinned) {
                Icon(
                    imageVector = if (isCoui) ConversationPaneIcons.Pin else MiuixIcons.Glass.Pin,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(if (isCoui) 13.dp else 16.dp),
                    tint = summaryColor.copy(alpha = 0.72f),
                )
            }

            if (selected) {
                val moreLabel = stringResource(R.string.action_more)
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(28.dp)
                        .then(
                            if (showActionMenu) {
                                Modifier.onGloballyPositioned { moreButtonBounds = it.boundsInWindow() }
                            } else {
                                Modifier
                            }
                        )
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = moreLabel,
                        ) {
                            popupAnchorBounds = moreButtonBounds
                            anchor.open { showActionMenu = true }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isCoui) Icons.Outlined.MoreHoriz else MiuixIcons.Glass.More,
                        contentDescription = moreLabel,
                        modifier = Modifier.size(if (isCoui) 17.dp else 20.dp),
                        tint = summaryColor,
                    )
                }
            }
        }

        // 将选中指示条放在左内边距留白区，保证选中行与普通行标题严格处于同一条左对齐基线。
        if (selected && conversation.characterName == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 5.5.dp)
                    .width(2.5.dp)
                    .height(16.dp)
                    .clip(CircleShape)
                    .background(accentColor),
            )
        }

        val pinText = stringResource(
            if (conversation.isPinned) {
                R.string.action_unpin_conversation
            } else {
                R.string.action_pin_conversation
            },
        )
        val renameText = stringResource(R.string.action_rename)
        val exportText = stringResource(R.string.action_export)
        val moveText = stringResource(R.string.action_move_to_category)
        val deleteText = stringResource(R.string.action_delete)
        when (LocalUiStyle.current) {
            AppearanceUiStyle.COUI -> {
                val destructiveColor = CouiDialogDefaults.themeColors().destructive
                val menuItems = remember(pinText, renameText, exportText, deleteText, destructiveColor) {
                    listOf(
                        CouiMenuItem(value = 0, text = pinText, icon = Icons.Outlined.PushPin),
                        CouiMenuItem(value = 1, text = renameText, icon = Icons.Outlined.Edit),
                        CouiMenuItem(value = 2, text = exportText, icon = Icons.Outlined.Download),
                        CouiMenuItem(
                            value = 4,
                            text = moveText,
                            icon = Icons.Outlined.CreateNewFolder,
                        ),
                        CouiMenuItem(
                            value = 3,
                            text = deleteText,
                            icon = Icons.Outlined.Delete,
                            iconTint = destructiveColor,
                            dividerBefore = true,
                        ),
                    )
                }
                Menu(
                    expanded = showActionMenu,
                    onDismissRequest = { showActionMenu = false },
                    items = menuItems,
                    onItemClick = { item ->
                        showActionMenu = false
                        when (item.value) {
                            0 -> onTogglePin()
                            1 -> onRename()
                            2 -> onExport()
                            3 -> onDelete()
                            4 -> showMoveMenu = true
                        }
                    },
                    anchor = anchor,
                )
            }
            AppearanceUiStyle.MIUIX -> {
                val overlayBackdrop = currentGlassOverlayBackdrop() ?: miuixBackdrop
                EtaTopLayer {
                    GlassPopup(
                        show = showActionMenu,
                        onDismissRequest = { showActionMenu = false },
                        anchorBounds = popupAnchorBounds,
                        backdrop = overlayBackdrop,
                    ) {
                        GlassPopupItem(
                            text = pinText,
                            icon = MiuixIcons.Glass.Pin,
                            onClick = {
                                showActionMenu = false
                                onTogglePin()
                            },
                        )
                        GlassPopupItem(
                            text = renameText,
                            icon = MiuixIcons.Glass.Edit,
                            onClick = {
                                showActionMenu = false
                                onRename()
                            },
                        )
                        GlassPopupItem(
                            text = exportText,
                            icon = MiuixIcons.Glass.Download,
                            onClick = {
                                showActionMenu = false
                                onExport()
                            },
                        )
                        GlassPopupItem(
                            text = moveText,
                            icon = MiuixIcons.Glass.FolderOutline,
                            onClick = {
                                showActionMenu = false
                                showMoveMenu = true
                            },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(
                                horizontal = GlassPopupDefaults.DividerPaddingHorizontal,
                                vertical = GlassPopupDefaults.DividerPaddingVertical,
                            ),
                            thickness = GlassPopupDefaults.DividerThickness,
                        )
                        GlassPopupItem(
                            text = deleteText,
                            icon = MiuixIcons.Glass.Delete,
                            contentColor = MiuixTheme.colorScheme.error,
                            onClick = {
                                showActionMenu = false
                                onDelete()
                            },
                        )
                    }
                }
            }
        }
        // 选一个分类搬过去；「移出分类」把它变回未分类。
        if (showMoveMenu) {
            val moveTitle = stringResource(R.string.action_move_to_category)
            val noneText = stringResource(R.string.conversation_category_none)
            EtaWindowDialog(
                show = true,
                title = moveTitle,
                onDismissRequest = { showMoveMenu = false },
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    moveTargets(categories, noneText).forEach { (id, label) ->
                        Text(
                            text = label,
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    showMoveMenu = false
                                    onMove(id)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }
}

/// 移动目标：先是「移出分类」，然后是各分类。
private fun moveTargets(categories: List<ConversationCategoryUi>, none: String): List<Pair<String?, String>> =
    listOf<Pair<String?, String>>(null to none) + categories.map { it.id to it.name }

@Composable
private fun EmptyConversations(isSearching: Boolean) {
    val isCoui = LocalUiStyle.current == AppearanceUiStyle.COUI
    val dark = if (isCoui) CouiTheme.isDark else MiuixTheme.colorScheme.surface.luminance() < 0.5f
    val primaryTextColor = if (isCoui) {
        CouiPageDefaults.themeColors(dark).summary
    } else {
        MiuixTheme.colorScheme.onSurfaceVariantSummary
    }
    val badgeBg = conversationPaneCardColor()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ConversationPanelMetrics.PaneHorizontalPadding + 12.dp,
                vertical = ConversationPanelMetrics.EmptyVerticalPadding,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .then(
                    if (isCoui) {
                        Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(badgeBg)
                    } else {
                        Modifier.squircleSurface(color = badgeBg, cornerRadius = 14.dp)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isSearching) ConversationPaneIcons.Search else ConversationPaneIcons.BrandEta,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = primaryTextColor.copy(alpha = 0.72f),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(
                if (isSearching) R.string.conversation_no_results else R.string.conversation_empty,
            ),
            color = primaryTextColor,
            style = MiuixTheme.textStyles.body2,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        if (!isSearching) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.conversation_empty_hint),
                color = primaryTextColor.copy(alpha = 0.75f),
                style = MiuixTheme.textStyles.footnote1.copy(fontSize = 12.sp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private data class ConversationDrawerGroup(
    val section: ConversationDrawerSection,
    val items: List<ConversationSummaryUi>,
)

private sealed interface ConversationDrawerSection {
    data object Pinned : ConversationDrawerSection
    data class Category(val id: String, val name: String) : ConversationDrawerSection
    data object Today : ConversationDrawerSection
    data class Dated(val label: String) : ConversationDrawerSection
}

@Composable
private fun ConversationDrawerGroup.localizedLabel(): String = when (val value = section) {
    ConversationDrawerSection.Pinned -> stringResource(R.string.conversation_section_pinned)
    is ConversationDrawerSection.Category -> value.name
    ConversationDrawerSection.Today -> stringResource(R.string.conversation_section_today)
    is ConversationDrawerSection.Dated -> value.label
}

private fun List<ConversationSummaryUi>.groupForDrawer(
    categories: List<ConversationCategoryUi> = emptyList(),
): List<ConversationDrawerGroup> {
    if (isEmpty()) return emptyList()
    val groups = mutableListOf<ConversationDrawerGroup>()

    // 置顶永远在最前，且不进任何分类分组。
    val pinned = filter { it.isPinned }
    if (pinned.isNotEmpty()) {
        groups += ConversationDrawerGroup(ConversationDrawerSection.Pinned, pinned)
    }

    // 分类分组按 sort_order 顺序，只收非置顶会话。
    val categorized = mutableSetOf<String>()
    categories.forEach { category ->
        val items = filter { !it.isPinned && it.categoryId == category.id }
        if (items.isNotEmpty()) {
            categorized += items.map { it.id }
            groups += ConversationDrawerGroup(
                ConversationDrawerSection.Category(category.id, category.name),
                items,
            )
        }
    }

    // 其余（未分类，或分类已被删除）沿用时间分组。
    for (conversation in this) {
        if (conversation.isPinned || conversation.id in categorized) continue
        val section = conversation.drawerSection()
        val last = groups.lastOrNull()
        if (last?.section == section) {
            groups[groups.lastIndex] = last.copy(items = last.items + conversation)
        } else {
            groups += ConversationDrawerGroup(section = section, items = listOf(conversation))
        }
    }
    return groups
}

private fun ConversationSummaryUi.drawerSection(): ConversationDrawerSection = when {
    isPinned -> ConversationDrawerSection.Pinned
    isActiveRun || isUpdatedToday(updatedAtMillis) -> ConversationDrawerSection.Today
    else -> ConversationDrawerSection.Dated(timeLabel)
}

private fun isUpdatedToday(timestampMillis: Long): Boolean {
    if (timestampMillis <= 0L) return true
    val now = java.util.Calendar.getInstance()
    val target = java.util.Calendar.getInstance().apply { timeInMillis = timestampMillis }
    return now.get(java.util.Calendar.ERA) == target.get(java.util.Calendar.ERA) &&
        now.get(java.util.Calendar.YEAR) == target.get(java.util.Calendar.YEAR) &&
        now.get(java.util.Calendar.DAY_OF_YEAR) == target.get(java.util.Calendar.DAY_OF_YEAR)
}
