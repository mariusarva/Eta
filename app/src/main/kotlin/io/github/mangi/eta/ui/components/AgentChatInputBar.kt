package io.github.mangi.eta.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mangi.eta.R
import io.github.mangi.eta.ui.components.custom.LocalCustomComposer
import io.github.mangi.eta.data.model.AppearanceUiStyle
import io.github.mangi.eta.data.model.ReasoningEffort
import io.github.mangi.eta.data.repository.SpeechSettingsRepository
import io.github.mangi.eta.ui.app.LocalUiStyle
import io.github.mangi.eta.ui.components.coui.CouiMenu
import io.github.mangi.eta.ui.components.coui.CouiMenuItem
import io.github.mangi.eta.ui.components.coui.couiButtonFeedback
import io.github.mangi.eta.ui.components.coui.couiMenuAnchor
import io.github.mangi.eta.ui.components.coui.rememberCouiMenuAnchor
import io.github.mangi.eta.ui.markdown.EditorialLineHeightStyle
import io.github.mangi.eta.ui.model.AgentContextUsageUi
import io.github.mangi.eta.ui.model.PendingFileReferenceUi
import io.github.mangi.eta.ui.model.PendingImageUi
import io.github.mangi.eta.ui.model.QueuedMessageUi
import io.github.mangi.eta.ui.model.TokenUsageUi
import io.github.mangi.eta.ui.voice.SpeechDictationButton
import io.github.mangi.eta.ui.voice.SpeechInputFeedback
import io.github.mangi.eta.ui.voice.rememberSpeechInput
import io.github.suqi8.coui.kmp.blur.ViewBlurDefaults
import io.github.suqi8.coui.kmp.blur.couiViewBlur
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassPopupItem
import top.yukonga.miuix.kmp.glass.GlassPopupSizing
import top.yukonga.miuix.kmp.glass.GlassTransformPopup
import top.yukonga.miuix.kmp.glass.glassPanel
import top.yukonga.miuix.kmp.glass.glassPopupAnchor
import top.yukonga.miuix.kmp.glass.glassPress
import top.yukonga.miuix.kmp.glass.rememberGlassPopupAnchor
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Close
import top.yukonga.miuix.kmp.material.GlassDefaults
import top.yukonga.miuix.kmp.material.GlassEdgeStrokes
import top.yukonga.miuix.kmp.material.GlassMaterials
import top.yukonga.miuix.kmp.material.GlassShadows
import top.yukonga.miuix.kmp.material.GlassShape
import top.yukonga.miuix.kmp.material.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Reply
import io.github.suqi8.coui.kmp.blur.LayerBackdrop as CouiLayerBackdrop
import io.github.suqi8.coui.kmp.squircle.SquircleShape as CouiShape
import io.github.suqi8.coui.kmp.theme.COUITheme as CouiTheme

private val SendButtonVisualSize = ChatInputActionIconSize
private val SendIconSize = 16.dp
/** 停止方块图标：32dp 视觉圆里 14dp 才不显得空。 */
private val StopIconSize = 14.dp
/**
 * 暂停 / 插入按钮的视觉圆直径。
 *
 * 必须与输入条主键（发送 / 停止）的实心圆一致：上游主键是 32dp，
 * 之前这三键按 24dp 画，暂停与插入明显比停止小一圈。
 */
private val RunActionVisualSize = 32.dp
/** 命中区比视觉圆大一档，保证可点面积符合最小触控尺寸。 */
private val RunActionButtonSize = 40.dp
/** 图标尺寸：32dp 圆内留 7dp 边距，比 24dp 圆里的同比例更大更清晰。 */
private val ActionIconSize = 18.dp
/** 运行操作按钮（暂停 / 插入 / 停止）之间的统一间距。 */
private val RunActionGap = 8.dp

/**
 * 运行中操作键（暂停/继续、注入）的统一外形。
 *
 * 命中区固定 [RunActionButtonSize]，视觉圆固定 [SendButtonVisualSize]；
 * 视觉圆直接画在固定 Box 里，不经 IconButton 的内部内边距二次压缩，
 * 这样几个键的圆直径与相互间距才完全一致。
 */
@Composable
private fun RunActionButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .requiredSize(RunActionButtonSize)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClickLabel = contentDescription, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(RunActionVisualSize)
                .clip(CircleShape)
                .background(MiuixTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

/**
 * Agent 输入器始终保持同一空间结构，聚焦、输入和执行过程只改变状态，不搬动操作入口。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AgentChatInputBar(
    input: String,
    isCompacting: Boolean,
    contextUsage: AgentContextUsageUi,
    showContextUsage: Boolean,
    isStreaming: Boolean,
    reasoningEffort: ReasoningEffort,
    availableReasoningEfforts: List<ReasoningEffort>,
    pendingImages: List<PendingImageUi>,
    pendingFileReferences: List<PendingFileReferenceUi>,
    queuedMessages: List<QueuedMessageUi> = emptyList(),
    inputSyncVersion: Int = 0,
    focusInputOnSync: Boolean = true,
    isEditingMessage: Boolean,
    editHasLaterTurns: Boolean,
    preserveFollowingMessages: Boolean,
    onReasoningEffortChange: (ReasoningEffort) -> Unit,
    onCompactContext: () -> Unit,
    canCompactContext: Boolean,
    onSubmit: (String) -> Unit,
    onInterruptAndSubmit: (String) -> Unit = onSubmit,
    onSteerQueuedMessage: (String) -> Unit = {},
    onRecallQueuedMessage: (String, String) -> Unit = { _, _ -> },
    onRemoveQueuedMessage: (String) -> Unit = {},
    onStop: () -> Unit,
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
    isRunPaused: Boolean = false,
    onInject: () -> Unit = {},
    onAttachImage: (String) -> Unit,
    onRemoveImage: (String) -> Unit,
    onAttachFiles: (List<String>) -> Unit,
    onAttachFolder: (String) -> Unit,
    onAttachFilePath: (String) -> Unit,
    onRemoveFileReference: (String) -> Unit,
    onCancelMessageEdit: () -> Unit,
    glassBackdrop: Backdrop? = null,
    couiBackdrop: CouiLayerBackdrop? = null,
    /** 输入框上沿左侧的插槽（轮次横条）；与右侧能力胶囊同一行，左对齐。 */
    topStartSlot: (@Composable () -> Unit)? = null,
    /** 输入框内部顶部左侧的速度读数来源（最近一轮）；为空则不显示。 */
    latestUsage: TokenUsageUi? = null,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val hapticFeedback = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }
    // 优先用上层提供的 composer：引用追问要把引用写进同一个输入框。
    // 无上层的场景（概览浮窗）退回自建，行为不变。
    val textFieldState = LocalCustomComposer.current ?: rememberTextFieldState(initialText = input)
    val speechSettings by remember { SpeechSettingsRepository.settingsFlow() }.collectAsState(initial = null)
    val recognitionEnabled = speechSettings?.recognitionEnabled == true
    val dictation = rememberSpeechInput(
        onHoldResult = { recognized ->
            val spoken = recognized.trim()
            if (spoken.isEmpty()) return@rememberSpeechInput
            val draft = textFieldState.text.toString()
            val toSubmit = if (draft.isBlank()) {
                spoken
            } else {
                textFieldState.edit {
                    val range = selection
                    replace(range.min, range.max, spoken)
                    selection = TextRange(range.min + spoken.length)
                }
                textFieldState.text.toString().trim()
            }
            textFieldState.clearText()
            onSubmit(toSubmit)
        },
        onResult = { recognized ->
            textFieldState.edit {
                val range = selection
                replace(range.min, range.max, recognized)
                selection = TextRange(range.min + recognized.length)
            }
        },
    )
    val dictationState by dictation.state.collectAsState()
    var holdCancelling by remember { mutableStateOf(false) }
    val isHoldingSpeech =
        dictationState.active && dictationState.holdToTalk &&
            dictationState.phase != io.github.mangi.eta.agent.voice.EtaSpeechPhase.RECOGNIZING
    LaunchedEffect(isHoldingSpeech) {
        if (!isHoldingSpeech) holdCancelling = false
    }
    LaunchedEffect(isEditingMessage, recognitionEnabled) { dictation.cancel() }
    var wasEditingMessage by remember { mutableStateOf(isEditingMessage) }
    var syncedVersion by remember { mutableIntStateOf(inputSyncVersion) }
    val canSend = textFieldState.text.isNotBlank() ||
        pendingImages.isNotEmpty() ||
        pendingFileReferences.isNotEmpty()

    LaunchedEffect(isEditingMessage, inputSyncVersion) {
        // 编辑态与排队消息撤回由外部业务状态驱动；普通输入只保留在本地，避免每个字符把聊天舞台
        // 的消息流、滚动和 Markdown 一起带入重组。
        val versionChanged = inputSyncVersion != syncedVersion
        if (isEditingMessage || wasEditingMessage || versionChanged) {
            textFieldState.setTextAndPlaceCursorAtEnd(input)
        }
        if (isEditingMessage || (versionChanged && input.isNotEmpty() && focusInputOnSync)) {
            focusRequester.requestFocus()
            keyboard?.show()
        }
        wasEditingMessage = isEditingMessage
        syncedVersion = inputSyncVersion
    }

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        if (recognitionEnabled) SpeechInputFeedback(dictation, holdCancelling = holdCancelling)
        AnimatedVisibility(
            visible = queuedMessages.isNotEmpty(),
            enter = fadeIn(tween(160)) + expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                expandFrom = Alignment.Bottom,
            ),
            exit = fadeOut(tween(100)) + shrinkVertically(tween(150)),
        ) {
            AgentChatQueueStrip(
                queuedMessages = queuedMessages,
                isStreaming = isStreaming,
                onSteer = onSteerQueuedMessage,
                onRecall = { id -> onRecallQueuedMessage(id, textFieldState.text.toString()) },
                onRemove = onRemoveQueuedMessage,
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }
        AnimatedVisibility(
            visible = pendingFileReferences.isNotEmpty(),
            enter = fadeIn(tween(180)) + expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                expandFrom = Alignment.Bottom,
            ),
            exit = fadeOut(tween(120)) + shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
                shrinkTowards = Alignment.Bottom,
            ),
        ) {
            PendingFileReferenceStrip(
                references = pendingFileReferences,
                onRemoveReference = onRemoveFileReference,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        AnimatedVisibility(
            visible = pendingImages.isNotEmpty(),
            enter = fadeIn(tween(180)) + expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                expandFrom = Alignment.Bottom,
            ),
            exit = fadeOut(tween(120)) + shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
                shrinkTowards = Alignment.Bottom,
            ),
        ) {
            PendingImageStrip(
                images = pendingImages,
                onRemoveImage = onRemoveImage,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        AnimatedVisibility(
            visible = isEditingMessage,
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(100)) + shrinkVertically(tween(140)),
        ) {
            Text(
                text = if (preserveFollowingMessages) {
                    stringResource(R.string.chat_edit_preserve_following)
                } else if (editHasLaterTurns) {
                    stringResource(R.string.chat_edit_replace_later)
                } else {
                    stringResource(R.string.chat_edit_replace_message)
                },
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                // 与输入框内正文同一起点（容器 10dp + 文本 8dp）。
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 8.dp),
            )
        }

        val isCoui = LocalUiStyle.current == AppearanceUiStyle.COUI
        val couiDark = CouiTheme.isDark
        val couiColors = CouiTheme.colorScheme
        // 两个主题共用同一个普通圆角轮廓：
        // - 阴影必须与卡片轮廓完全一致，否则边缘会露出两套轮廓，观感就是「阴影重叠」；
        // - 超椭圆（SquircleShape / GlassShape）在系统 elevation 阴影下无法保证轮廓一致。
        val inputCardShape = remember { RoundedCornerShape(24.dp) }
        val isDark = MiuixTheme.colorScheme.surface.luminance() < 0.5f
        val miuixInputBg = if (!isDark && MiuixTheme.colorScheme.surfaceContainer == MiuixTheme.colorScheme.surface) {
            Color(0xFFF5F6F8)
        } else {
            MiuixTheme.colorScheme.surfaceContainer
        }
        val defaultInputBorderColor = if (isCoui) {
            couiColors.divider
        } else {
            MiuixTheme.colorScheme.outline.copy(alpha = 0.5f)
        }
        val inputBorderColor by animateColorAsState(
            targetValue = when {
                isHoldingSpeech && holdCancelling -> StatusError.copy(alpha = 0.45f)
                isHoldingSpeech -> MiuixTheme.colorScheme.primary.copy(alpha = 0.38f)
                else -> defaultInputBorderColor
            },
            animationSpec = tween(durationMillis = 180),
            label = "input_container_border",
        )
        val inputBorderWidth by animateDpAsState(
            targetValue = if (isHoldingSpeech) 1.dp else 0.5.dp,
            animationSpec = tween(durationMillis = 160),
            label = "input_container_border_width",
        )
        var inputContainerTopPx by remember { mutableFloatStateOf(0f) }
        // 速度读数与能力胶囊现在都放进输入框内部顶部那一行，容器外不再留独立行。
        val speedText = latestUsage?.let { u ->
            val decodeMs = u.decodeMs
            val tokens = u.decodeTokens ?: u.outputTokens
            if (decodeMs != null && decodeMs > 0L && tokens != null && tokens > 0) {
                formatTokensPerSecond(tokens.toDouble() / (decodeMs.toDouble() / 1_000.0)) + " tok/s"
            } else {
                null
            }
        }
        // 外层用 Column 纵向排列：横条在上、输入卡片在下。
        // 之前用 Box，后绘制的输入卡片会把横条整个盖住（实测横条消失）。
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    inputContainerTopPx = coordinates.boundsInWindow().top
                },
        ) {
            // 轮次横条放在输入框外面的上方，居中。
            if (topStartSlot != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    topStartSlot()
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isCoui) {
                            Modifier
                                // 纯色卡片，不用 couiViewBlur：
                                // 模糊层引用整页 backdrop，从文件区滑回对话页时
                                // backdrop 内容已变但模糊层还留着一帧旧采样，
                                // 输入框右侧就会出现一块半透明重影（实测）。
                                // 输入框本来就是深色不透明卡片，去掉模糊视觉差异极小。
                                // 用系统 elevation 阴影（Modifier.shadow），不用 dropShadow：
                                // dropShadow 会在离屏 layer 上预先画好整块阴影贴图，
                                // 输入框宽度随输入法/侧栏变化时旧贴图不会立刻失效，
                                // 右侧会残留一条半透明色块（重影）。elevation 阴影由
                                // RenderNode 每帧实时绘制，不存在缓存残留。
                                .shadow(
                                    elevation = 6.dp,
                                    shape = inputCardShape,
                                    clip = false,
                                    ambientColor = Color.Black.copy(alpha = if (couiDark) 0.30f else 0.12f),
                                    spotColor = Color.Black.copy(alpha = if (couiDark) 0.30f else 0.12f),
                                )
                                .background(couiColors.surface, inputCardShape)
                                .clip(inputCardShape)
                                .border(
                                    width = inputBorderWidth,
                                    color = inputBorderColor,
                                    shape = inputCardShape,
                                )
                        } else {
                            // 与 COUI 分支同因同治：
                            // glassPanel 的玻璃层引用整页 backdrop，页面切换时旧采样会留一帧；
                            // 它的阴影同样是一次性烘焙的离屏贴图，输入框宽度随输入法变化时
                            // 右侧会露出阴影重叠。输入框本身就是接近不透明的纯色卡片，
                            // 换成纯色 + 系统 elevation 阴影后视觉差异极小。
                            Modifier
                                .shadow(
                                    elevation = 6.dp,
                                    shape = inputCardShape,
                                    clip = false,
                                    ambientColor = Color.Black.copy(alpha = if (isDark) 0.30f else 0.12f),
                                    spotColor = Color.Black.copy(alpha = if (isDark) 0.30f else 0.12f),
                                )
                                .background(miuixInputBg, inputCardShape)
                                .clip(inputCardShape)
                                .border(
                                    width = inputBorderWidth,
                                    color = inputBorderColor,
                                    shape = inputCardShape,
                                )
                        },
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                // 输入框内部顶部一行：左＝速度读数，右＝能力胶囊。
                // 轮次横条已移到框外上方居中，这里只放这两个只读指示。
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .padding(start = 6.dp, end = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (speedText != null) {
                        Text(
                            text = speedText,
                            style = MiuixTheme.textStyles.footnote2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.75f),
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    CapabilityCapsule()
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 36.dp)
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    contentAlignment = Alignment.TopStart,
                ) {
                    // 占位文字与输入正文共用同一行高，首字输入时基线不跳动。
                    val inputTextStyle = MiuixTheme.textStyles.body1.copy(
                        lineHeight = 22.sp,
                        lineHeightStyle = EditorialLineHeightStyle,
                    )
                    val character = LocalRoleplayChat.current
                    if (textFieldState.text.isBlank()) {
                        val hintColor by animateColorAsState(
                            targetValue = when {
                                isHoldingSpeech && holdCancelling -> StatusError
                                isHoldingSpeech -> MiuixTheme.colorScheme.primary
                                else -> MiuixTheme.colorScheme.onSurfaceVariantSummary
                            },
                            animationSpec = tween(durationMillis = 150),
                            label = "input_hint_color",
                        )
                        Text(
                            text = when {
                                isHoldingSpeech && holdCancelling ->
                                    stringResource(R.string.speech_hold_release_to_cancel)
                                isHoldingSpeech -> stringResource(R.string.voice_release_to_send)
                                // 角色对话的占位文字跟随角色，避免“交给 Eta”打断沉浸感。
                                character != null && isStreaming ->
                                    stringResource(R.string.chat_character_typing, character.characterName)
                                character != null -> stringResource(R.string.chat_character_input_hint, character.characterName)
                                isStreaming -> stringResource(R.string.chat_eta_working)
                                else -> stringResource(R.string.chat_input_hint)
                            },
                            style = inputTextStyle,
                            color = hintColor,
                        )
                    }
                    BasicTextField(
                        state = textFieldState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                        textStyle = inputTextStyle.copy(color = MiuixTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
                        lineLimits = TextFieldLineLimits.MultiLine(
                            minHeightInLines = 1,
                            maxHeightInLines = 6,
                        ),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                        if (isEditingMessage) {
                            IconButton(
                                onClick = onCancelMessageEdit,
                                minWidth = ChatInputActionSize,
                                minHeight = ChatInputActionSize,
                            ) {
                                Icon(
                                    imageVector = if (isCoui) Icons.Rounded.Close else MiuixIcons.Glass.Close,
                                    contentDescription = stringResource(R.string.ui_cancel_edit_c698df),
                                    modifier = Modifier.size(ChatInputActionIconSize),
                                    tint = MiuixTheme.colorScheme.onSurface,
                                )
                            }
                        } else {
                            AgentAttachmentPickerButton(
                                glassBackdrop = glassBackdrop,
                                inputContainerTopPx = inputContainerTopPx,
                                onAttachImage = onAttachImage,
                                onAttachFiles = onAttachFiles,
                                onAttachFolder = onAttachFolder,
                                onAttachFilePath = onAttachFilePath,
                            )

                            Spacer(modifier = Modifier.width(2.dp))

                            if (availableReasoningEfforts.isNotEmpty()) {
                                ThinkingEffortChip(
                                    effort = reasoningEffort,
                                    options = availableReasoningEfforts,
                                    enabled = !isStreaming,
                                    glassBackdrop = glassBackdrop,
                                    inputContainerTopPx = inputContainerTopPx,
                                    onEffortChange = onReasoningEffortChange,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (showContextUsage && contextUsage.contextTokens != null) {
                            AgentContextUsageButton(
                                usage = contextUsage,
                                onCompact = onCompactContext,
                                canCompact = canCompactContext && !isStreaming,
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                        }

                        if (recognitionEnabled) {
                            SpeechDictationButton(
                                controller = dictation,
                                enabled = !isCompacting,
                                holdCancelling = holdCancelling,
                                onHoldCancellingChange = { holdCancelling = it },
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                        }

                        if (isStreaming && !isCompacting) {
                            Spacer(modifier = Modifier.width(RunActionGap))
                            RunActionButton(
                                onClick = { if (isRunPaused) onResume() else onPause() },
                                contentDescription = if (isRunPaused) "继续" else "暂停",
                            ) {
                                Icon(
                                    imageVector = if (isRunPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                    contentDescription = null,
                                    modifier = Modifier.size(ActionIconSize),
                                    tint = MiuixTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        val showStreamingSend = isStreaming && !isCompacting && canSend
                        val submitCurrentDraft: (Boolean) -> Unit = { interrupt ->
                            if (canSend) {
                                dictation.cancel()
                                val submittedText = textFieldState.text.toString()
                                textFieldState.clearText()
                                if (interrupt) {
                                    onInterruptAndSubmit(submittedText)
                                } else {
                                    onSubmit(submittedText)
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = showStreamingSend,
                            enter = fadeIn(tween(140)) + scaleIn(tween(160), initialScale = 0.78f) + expandHorizontally(tween(160)),
                            exit = fadeOut(tween(100)) + scaleOut(tween(120), targetScale = 0.78f) + shrinkHorizontally(tween(140)),
                        ) {
                            IconButton(
                                onClick = onStop,
                                minWidth = ChatInputActionSize,
                                minHeight = ChatInputActionSize,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(RunActionVisualSize)
                                        .clip(CircleShape)
                                        .background(MiuixTheme.colorScheme.surfaceContainerHigh),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Stop,
                                        contentDescription = stringResource(R.string.chat_stop),
                                        modifier = Modifier.size(StopIconSize),
                                        tint = MiuixTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }

                        val showPrimaryStop = isStreaming && !showStreamingSend
                        val sendCircleSize = 32.dp
                        val ink = MiuixTheme.colorScheme.onSurface.copy(alpha = 1f)
                        val sendButtonColor by animateColorAsState(
                            targetValue = when {
                                showPrimaryStop -> MiuixTheme.colorScheme.onSurface
                                canSend -> MiuixTheme.colorScheme.primary
                                else -> ink.copy(alpha = if (isDark) 0.08f else 0.06f)
                            },
                            animationSpec = tween(durationMillis = 160),
                            label = "send_button_color",
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .combinedClickable(
                                    enabled = isStreaming || canSend,
                                    onClick = {
                                        if (showPrimaryStop) {
                                            onStop()
                                        } else {
                                            submitCurrentDraft(false)
                                        }
                                    },
                                    onLongClick = if (showStreamingSend) {
                                        {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            submitCurrentDraft(true)
                                        }
                                    } else {
                                        null
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(sendCircleSize)
                                    .clip(CircleShape)
                                    .background(sendButtonColor),
                                contentAlignment = Alignment.Center,
                            ) {
                                AnimatedContent(
                                    targetState = showPrimaryStop,
                                    transitionSpec = {
                                        (fadeIn(tween(130)) + scaleIn(tween(160), initialScale = 0.72f))
                                            .togetherWith(
                                                fadeOut(tween(90)) +
                                                    scaleOut(tween(110), targetScale = 0.72f)
                                            )
                                    },
                                    label = "send_stop_icon",
                                ) { stopIcon ->
                                    Icon(
                                        imageVector = if (stopIcon) {
                                            Icons.Rounded.Stop
                                        } else {
                                            Icons.Rounded.ArrowUpward
                                        },
                                        contentDescription = when {
                                            stopIcon -> stringResource(R.string.chat_stop)
                                            isEditingMessage && preserveFollowingMessages -> stringResource(R.string.chat_save_message)
                                            else -> stringResource(R.string.chat_send)
                                        },
                                        modifier = Modifier.size(if (stopIcon) 16.dp else 17.dp),
                                        tint = when {
                                            stopIcon -> MiuixTheme.colorScheme.surface
                                            canSend -> MiuixTheme.colorScheme.onPrimary
                                            else -> ink.copy(alpha = if (isDark) 0.28f else 0.24f)
                                        },
                                    )
                                }
                            }
                        }
                }
            }
        }
    }

}

/**
 * 思考强度胶囊：仅含开关两态时单击直接切换，支持多档强度时展开菜单并在胶囊上直接标明英文档位。
 */
@Composable
private fun ThinkingEffortChip(
    effort: ReasoningEffort,
    options: List<ReasoningEffort>,
    enabled: Boolean,
    glassBackdrop: Backdrop?,
    inputContainerTopPx: Float,
    onEffortChange: (ReasoningEffort) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPopup by remember { mutableStateOf(false) }
    var chipTopPx by remember { mutableFloatStateOf(0f) }
    var chipBottomPx by remember { mutableFloatStateOf(0f) }
    var measuredPopupHeightPx by remember { mutableFloatStateOf(0f) }
    val active = effort != ReasoningEffort.OFF
    val interactive = enabled && options.size > 1
    val binaryToggleTarget = remember(options, effort) {
        if (options.size == 2 && ReasoningEffort.OFF in options) {
            options.firstOrNull { it != effort }
        } else {
            null
        }
    }
    val anchor = rememberCouiMenuAnchor()
    val popupAnchor = rememberGlassPopupAnchor()
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(interactive) {
        if (!interactive) showPopup = false
    }

    val colors = MiuixTheme.colorScheme
    val isDark = colors.surface.luminance() < 0.5f
    val isCoui = LocalUiStyle.current == AppearanceUiStyle.COUI
    val ink = colors.onSurface.copy(alpha = 1f)
    val contentColor by animateColorAsState(
        targetValue = if (active) {
            colors.primary
        } else {
            ink.copy(alpha = if (isDark) 0.58f else 0.54f)
        },
        animationSpec = tween(durationMillis = 160),
        label = "thinking_content",
    )
    val bgColor by animateColorAsState(
        targetValue = if (active) {
            colors.primary.copy(alpha = if (isDark) 0.18f else 0.10f)
        } else {
            ink.copy(alpha = if (isDark) 0.06f else 0.038f)
        },
        animationSpec = tween(durationMillis = 160),
        label = "thinking_bg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (active) {
            colors.primary.copy(alpha = if (isDark) 0.30f else 0.22f)
        } else {
            ink.copy(alpha = if (isDark) 0.10f else 0.065f)
        },
        animationSpec = tween(durationMillis = 160),
        label = "thinking_border",
    )

    val chipLabel = when (effort) {
        ReasoningEffort.OFF -> stringResource(R.string.reasoning_chip_off)
        ReasoningEffort.DEFAULT -> stringResource(R.string.reasoning_chip_default)
        else -> effort.displayName
    }
    val accessibilityLabel = stringResource(R.string.chat_reasoning_effort, effort.displayName)
    val interactionSource = remember { MutableInteractionSource() }
    val useTransformPopup = !isCoui && binaryToggleTarget == null
    val popupLayout = rememberInputBarPopupLayout(
        inputContainerTopPx = inputContainerTopPx,
        anchorTopPx = chipTopPx,
        anchorBottomPx = chipBottomPx,
        measuredHeightPx = measuredPopupHeightPx,
    )
    val chipContent: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_atom),
                contentDescription = null,
                modifier = Modifier.size(14.5.dp),
                tint = contentColor,
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = chipLabel,
                style = MiuixTheme.textStyles.footnote1.copy(
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = contentColor,
                maxLines = 1,
            )
        }
    }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .then(
                    if (useTransformPopup) {
                        Modifier
                            .glassPopupAnchor(
                                anchor = popupAnchor,
                                cornerRadius = 15.dp,
                            )
                            .onGloballyPositioned {
                                val bounds = it.boundsInWindow()
                                chipTopPx = bounds.top
                                chipBottomPx = bounds.bottom
                            }
                    } else {
                        Modifier.couiMenuAnchor(anchor)
                    },
                )
                .heightIn(min = 30.dp)
                .clip(CircleShape)
                .background(bgColor, CircleShape)
                .border(0.5.dp, borderColor, CircleShape)
                .then(
                    if (isCoui) {
                        Modifier.couiButtonFeedback(isDark, interactionSource, enabled = interactive)
                    } else {
                        Modifier.glassPress(interactionSource, enabled = interactive)
                    },
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = interactive,
                    role = Role.Button,
                    onClick = {
                        if (binaryToggleTarget != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onEffortChange(binaryToggleTarget)
                        } else if (isCoui) {
                            anchor.open { showPopup = true }
                        } else {
                            showPopup = true
                        }
                    },
                )
                .semantics {
                    contentDescription = accessibilityLabel
                }
                .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            chipContent()
        }
        if (binaryToggleTarget == null) {
            when (LocalUiStyle.current) {
                AppearanceUiStyle.COUI -> {
                    val menuItems = remember(options, effort) {
                        options.map { option ->
                            CouiMenuItem(
                                value = option,
                                text = option.displayName,
                                selected = option == effort,
                            )
                        }
                    }
                    CouiMenu(
                        expanded = showPopup && interactive,
                        onDismissRequest = { showPopup = false },
                        items = menuItems,
                        onItemClick = { item ->
                            showPopup = false
                            onEffortChange(item.value)
                        },
                        anchor = anchor,
                    )
                }
                AppearanceUiStyle.MIUIX -> {
                    val overlayBackdrop = currentGlassOverlayBackdrop() ?: glassBackdrop
                    EtaTopLayer {
                        GlassTransformPopup(
                            show = showPopup && interactive,
                            onDismissRequest = { showPopup = false },
                            anchor = popupAnchor,
                            backdrop = overlayBackdrop,
                            gap = popupLayout.gap,
                            sizing = GlassPopupSizing(maxHeight = popupLayout.maxHeight),
                            onMeasured = { measuredPopupHeightPx = it.height },
                            anchorContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .heightIn(min = 30.dp)
                                        .clip(CircleShape)
                                        .background(bgColor, CircleShape)
                                        .border(0.5.dp, borderColor, CircleShape)
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                ) {
                                    chipContent()
                                }
                            },
                        ) {
                            options.forEach { option ->
                                GlassPopupItem(
                                    text = option.displayName,
                                    selected = option == effort,
                                    onClick = {
                                        showPopup = false
                                        onEffortChange(option)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingImageStrip(
    images: List<PendingImageUi>,
    onRemoveImage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val imagePreviewer = LocalChatImagePreviewer.current
    val itemBounds = remember { HashMap<String, Rect>() }
    var previousCount by remember { mutableIntStateOf(images.size) }
    LaunchedEffect(images.size) {
        if (images.size > previousCount && images.isNotEmpty()) {
            listState.animateScrollToItem(images.lastIndex)
        }
        previousCount = images.size
    }
    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(
            items = images,
            key = { _, item -> item.id },
        ) { index, image ->
            Box(
                modifier = Modifier
                    .animateItem(
                        fadeInSpec = null,
                        placementSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                        fadeOutSpec = tween(durationMillis = 120),
                    )
                    .attachmentItemEntrance()
                    .size(64.dp)
                    .onGloballyPositioned { coords ->
                        if (coords.isAttached) itemBounds[image.id] = coords.boundsInWindow()
                    }
                    .graphicsLayer {
                        alpha = if (imagePreviewer?.isPresenting(PendingImagesGroupKey, index) == true) 0f else 1f
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(MiuixTheme.colorScheme.surfaceContainer)
                    .border(
                        width = 0.5.dp,
                        color = MiuixTheme.colorScheme.outline.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .then(
                        if (imagePreviewer != null) {
                            Modifier.clickable {
                                imagePreviewer.open(
                                    images = images.map { it.dataUrl },
                                    initialIndex = index,
                                    groupKey = PendingImagesGroupKey,
                                    sourceBoundsProvider = { page -> images.getOrNull(page)?.let { itemBounds[it.id] } },
                                    sourceCornerRadius = 12.dp,
                                )
                            }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                rememberDataUrlBitmap(image.dataUrl)?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = stringResource(R.string.chat_view_image),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                // 可见圆点保持小巧，触控区放大到右上角 32dp，避免误触缩略图。
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .clickable(interactionSource = null, indication = null) { onRemoveImage(image.id) },
                    contentAlignment = Alignment.TopEnd,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.58f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.ui_remove_image_089db3),
                            modifier = Modifier.size(11.dp),
                            tint = Color.White,
                        )
                    }
                }
            }
        }
    }
}

private const val PendingImagesGroupKey = "pending_images_strip"
