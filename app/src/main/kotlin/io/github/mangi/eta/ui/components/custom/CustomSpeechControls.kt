package io.github.mangi.eta.ui.components.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.itos.coui.slider.CouiSliderDefaults
import com.itos.coui.slider.Slider
import com.itos.coui.shape.CouiCapsuleShape
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.theme.CouiTheme
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Each message disposes its own lazy engine; only the active weak owner can play. */
@Composable
internal fun rememberCustomSpeechController(messageId: String, enabled: Boolean): CustomSpeechController {
    val app = LocalContext.current.applicationContext
    val owner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val controller = remember(app, messageId, owner) { CustomSpeechController(app, scope) }
    DisposableEffect(controller, owner) {
        controller.setForeground(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        val observer = LifecycleEventObserver { source, _ ->
            controller.setForeground(source.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); controller.close() }
    }
    LaunchedEffect(controller, enabled) { if (!enabled) controller.stop() }
    return controller
}

@Composable
internal fun CustomSpeechControls(controller: CustomSpeechController, messageId: String) {
    val state by controller.state.collectAsStateWithLifecycle()
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val foreground = lifecycle.isAtLeast(Lifecycle.State.RESUMED)
    // This effect cancels on pause, stop, background or disposal. Never a forever polling job.
    LaunchedEffect(controller, state.phase, foreground) {
        if (state.phase == CustomSpeechPhase.PLAYING && foreground) {
            while (true) { delay(400); controller.refreshProgress() }
        }
    }
    if (state.messageId != messageId || !foreground) return
    var locating by remember(controller, state.messageId) { mutableStateOf(false) }
    var dragging by remember(state.sentence, state.durationMs) { mutableStateOf<Float?>(null) }
    val dark = CouiTheme.isDark
    val colors = CouiPageDefaults.themeColors(dark)
    val density = LocalDensity.current
    val view = LocalView.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val navigationBottom = WindowInsets.navigationBars.getBottom(density)
    val inputReserve = with(density) { 80.dp.roundToPx() }
    // Align to the WINDOW, not the message anchor. Popup contributes no lazy-row height.
    // Android may already shrink its visible window for IME: subtract only the remainder.
    val positionProvider = remember(view, imeBottom, navigationBottom, inputReserve) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect, windowSize: IntSize,
                layoutDirection: LayoutDirection, popupContentSize: IntSize,
            ): IntOffset {
                val alreadyExcluded = (view.rootView.height - windowSize.height).coerceAtLeast(0)
                val bottomInset = (maxOf(imeBottom, navigationBottom) - alreadyExcluded).coerceAtLeast(0)
                val aligned = Alignment.BottomCenter.align(popupContentSize, windowSize, layoutDirection)
                return IntOffset(aligned.x, (aligned.y - bottomInset - inputReserve).coerceAtLeast(0))
            }
        }
    }
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = {},
        properties = PopupProperties(
            focusable = false, dismissOnBackPress = false, dismissOnClickOutside = false,
        ),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp)).background(if (dark) Color(0xFF303030) else Color(0xFFF5F5F5))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp), tint = MiuixTheme.colorScheme.onSurface)
                Text(
                    state.error ?: when (state.phase) {
                        CustomSpeechPhase.PREPARING -> "系统离线朗读 · 正在准备"
                        CustomSpeechPhase.PAUSED -> "系统离线朗读 · 已暂停"
                        else -> "系统离线朗读 · ${state.sentence + 1}/${state.sentences.size}"
                    },
                    fontSize = 16.sp,
                    color = if (state.error != null) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onSurface,
                )
            }
            state.sentences.getOrNull(state.sentence)?.let {
                Text(it, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (state.error == null) {
                Slider(
                    value = dragging ?: state.positionMs.toFloat(),
                    onValueChange = { dragging = it },
                    onValueChangeFinished = { dragging?.let { controller.seekTo(it.toInt()) }; dragging = null },
                    valueRange = 0f..state.durationMs.coerceAtLeast(1).toFloat(),
                    enabled = state.durationMs > 0,
                    dark = dark,
                    colors = CouiSliderDefaults.colors(colors.accent, dark, state.durationMs > 0),
                    shape = CouiCapsuleShape,
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "当前片段播放进度" },
                )
                Text("当前片段 ${state.positionMs / 1000} / ${state.durationMs / 1000} 秒", fontSize = 12.sp)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (state.error == null) {
                    if (state.phase == CustomSpeechPhase.PAUSED) {
                        SpeechButton("继续", Icons.Rounded.PlayArrow, controller::resume)
                    } else {
                        SpeechButton("暂停", Icons.Rounded.Pause, controller::pause)
                    }
                    SpeechButton("文字定位", Icons.Rounded.FormatListNumbered, { locating = true }, state.sentences.isNotEmpty())
                }
                SpeechButton("停止", Icons.Rounded.Stop, controller::stop)
            }
        }
    }
    if (locating && state.sentences.isNotEmpty() && state.error == null) {
        val locations = remember(state.sentences) { CustomSpeechLocations(state.sentences) }
        var textPosition by remember(controller, messageId, locations) { mutableStateOf<Float?>(null) }
        val displayedPosition = (textPosition?.toInt() ?: locations.startOffset(state.sentence))
            .coerceIn(0, locations.totalUtf16)
        val selectedSentence = locations.sentenceAt(displayedPosition)
        val sentenceListState = rememberLazyListState(initialFirstVisibleItemIndex = state.sentence)
        Dialog(onDismissRequest = { locating = false }) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .background(MiuixTheme.colorScheme.surface).padding(16.dp)) {
                Text("朗读文字定位", fontSize = 16.sp)
                Text("文字进度（文字位置，非时间）", fontSize = 14.sp)
                Text("$displayedPosition / ${locations.totalUtf16} · 第 ${selectedSentence + 1} / ${state.sentences.size} 句", fontSize = 14.sp)
                Slider(
                    value = displayedPosition.toFloat(),
                    onValueChange = { textPosition = it },
                    onValueChangeFinished = {
                        textPosition?.let { controller.locateSentence(locations.sentenceAt(it.toInt())) }
                        textPosition = null
                    },
                    valueRange = 0f..locations.totalUtf16.coerceAtLeast(1).toFloat(),
                    enabled = locations.totalUtf16 > 0,
                    dark = dark,
                    colors = CouiSliderDefaults.colors(colors.accent, dark, locations.totalUtf16 > 0),
                    shape = CouiCapsuleShape,
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "全文文字位置，非时间；松开跳到所选句首" },
                )
                Text(state.sentences.getOrNull(selectedSentence).orEmpty(), fontSize = 14.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("点选句子或拖动文字进度，松开后从所选句首朗读。", fontSize = 14.sp)
                LazyColumn(Modifier.heightIn(max = 420.dp).weight(1f, fill = false), state = sentenceListState) {
                    itemsIndexed(state.sentences) { index, sentence ->
                        Text(
                            "${if (index == state.sentence) "▶ " else ""}${index + 1}. $sentence",
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                .clickable(role = Role.Button) { controller.locateSentence(index); locating = false }
                                .padding(vertical = 10.dp),
                        )
                    }
                }
                Text("关闭", fontSize = 16.sp, modifier = Modifier
                    .clickable(role = Role.Button) { locating = false }.padding(12.dp))
            }
        }
    }
}

@Composable
private fun SpeechButton(label: String, icon: ImageVector, onClick: () -> Unit, enabled: Boolean = true) {
    Row(
        Modifier.heightIn(min = 48.dp).widthIn(min = 48.dp).clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MiuixTheme.colorScheme.onSurface)
        Text(label, fontSize = 14.sp)
    }
}
