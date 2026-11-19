package io.github.mangi.eta.ui.components.custom

import android.os.Build
import android.view.WindowManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text

internal data class MessageMenuAction(
    val title: String,
    val icon: ImageVector,
    val unavailable: String? = null,
    val invoke: () -> Unit,
)

/** Content slot only: the existing message host still owns the single menu and controllers. */
internal val LocalCustomMessageActionSlots = compositionLocalOf<(@Composable () -> Unit)?> { null }

/** v342 metrics, with no full-width row forcing an almost full-screen card. */
@Composable
internal fun CustomMessageMenuDialog(
    actions: List<MessageMenuAction>,
    onDismiss: () -> Unit,
    onUnavailable: (String) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        fontSize = 16.sp, lineHeight = 20.sp, letterSpacing = 0.sp,
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )
    val textWidth = actions.maxOfOrNull { measurer.measure(it.title, textStyle).size.width } ?: 0
    val width = with(density) { textWidth.toDp() + 56.dp }
        .coerceAtMost((configuration.screenWidthDp.dp - 32.dp).coerceAtLeast(1.dp))
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true, usePlatformDefaultWidth = false),
    ) {
        val view = LocalView.current
        DisposableEffect(view) {
            val window = (view.parent as? DialogWindowProvider)?.window
            window?.setWindowAnimations(0)
            window?.setDimAmount(0f)
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            // Best effort only: Android/ROM may disable cross-window blur. Never replace with dim.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    window?.setBackgroundBlurRadius(24)
                    window?.let {
                        val attributes = it.attributes
                        attributes.setBlurBehindRadius(24)
                        it.attributes = attributes
                    }
                } catch (error: RuntimeException) {
                    Log.w("CustomMessageMenu", "Background blur unsupported; dim remains disabled", error)
                }
            }
            onDispose { }
        }
        Column(
            modifier = Modifier.width(width)
                .heightIn(max = (configuration.screenHeightDp * 0.75f).dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF4F4F7))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            actions.forEach { action ->
                val tint = Color(0xFF202020).copy(alpha = if (action.unavailable == null) 1f else 0.45f)
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .semantics { action.unavailable?.let { stateDescription = "不可用：$it" } }
                        .clickable {
                            val reason = action.unavailable
                            if (reason != null) onUnavailable(reason)
                            else { onDismiss(); action.invoke() }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(action.icon, null, Modifier.size(16.dp), tint)
                    Text(action.title, style = textStyle, color = tint)
                }
            }
        }
    }
}
