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
import androidx.compose.runtime.remember
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
import top.yukonga.miuix.kmp.theme.MiuixTheme
import io.github.mangi.eta.data.model.AppearanceUiStyle
import io.github.mangi.eta.ui.app.LocalUiStyle
import io.github.mangi.eta.ui.components.coui.LocalCouiPageBackdrop
import io.github.mangi.eta.ui.components.currentGlassOverlayBackdrop
import io.github.mangi.eta.ui.components.DemandSceneBackdrop
import io.github.suqi8.coui.kmp.blur.ViewBlurDefaults
import io.github.suqi8.coui.kmp.blur.couiViewBlur
import top.yukonga.miuix.kmp.glass.glassPanel
import top.yukonga.miuix.kmp.material.GlassDefaults
import top.yukonga.miuix.kmp.material.GlassEdgeStrokes
import top.yukonga.miuix.kmp.material.GlassMaterials
import top.yukonga.miuix.kmp.material.GlassShape
import top.yukonga.miuix.kmp.material.GlassShadows
import top.yukonga.miuix.kmp.material.isRuntimeShaderSupported
import io.github.suqi8.coui.kmp.theme.COUITheme as CouiTheme

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
    // 两个主题各自的明暗判断：COUI 走它自己的 isDark，MIUIX 看当前 surface 亮度。
    // 之前这里写死 0xFFF4F4F7 / 0xFF202020，夜间模式弹出来还是一张白卡。
    val isCoui = LocalUiStyle.current == AppearanceUiStyle.COUI
    val menuDark = if (isCoui) {
        CouiTheme.isDark
    } else {
        MiuixTheme.colorScheme.surface.luminance() < 0.5f
    }
    val menuBackground = if (menuDark) Color(0xFF23262E) else Color(0xFFF4F4F7)
    val menuForeground = if (menuDark) Color(0xFFE9E9EC) else Color(0xFF202020)
    val menuShape = remember { RoundedCornerShape(24.dp) }
    // 模糊材质：两个主题都从各自的页面 backdrop 采样。
    // 这是临时浮层、弹出时页面静止，不会有输入框那种「切页时旧采样残留」的重影；
    // 采样后端不可用时回退到不透明纯色卡片（窗口级 FLAG_BLUR_BEHIND 仍在兜底）。
    val couiBackdrop = LocalCouiPageBackdrop.current
    val miuixBackdrop = currentGlassOverlayBackdrop()
    // MIUIX 的 backdrop 是按需采样（DemandSceneBackdrop）：自绘 Dialog 不在
    // EtaTopLayer / MiuixPopupHost 里，不 pin 的话 350ms 空闲超时会反复掐断 attach，
    // 模糊材质就会一闪一闪。菜单显示期间 pin 住，退出时 unpin。
    val pinnedBackdrop = miuixBackdrop as? DemandSceneBackdrop
    DisposableEffect(pinnedBackdrop) {
        pinnedBackdrop?.updatePinned(true)
        onDispose { pinnedBackdrop?.updatePinned(false) }
    }
    val hasMaterial = if (isCoui) {
        couiBackdrop != null
    } else {
        miuixBackdrop != null && isRuntimeShaderSupported()
    }
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
        val surfaceModifier = if (isCoui) {
            if (couiBackdrop != null) {
                Modifier.couiViewBlur(
                    backdrop = couiBackdrop,
                    style = ViewBlurDefaults.frameworkBottomBar(menuDark),
                    shape = menuShape,
                )
            } else {
                Modifier.background(menuBackground, menuShape)
            }
        } else {
            if (miuixBackdrop != null && isRuntimeShaderSupported()) {
                Modifier.glassPanel(
                    backdrop = miuixBackdrop,
                    shape = GlassShape(24.dp),
                    style = GlassDefaults.style(menuDark),
                    material = GlassMaterials.puredThinGlass(menuDark),
                    stroke = GlassEdgeStrokes.forTheme(
                        isDark = menuDark,
                        light = GlassEdgeStrokes.SmallLight,
                        dark = GlassEdgeStrokes.SmallDark,
                    ),
                    shadow = GlassShadows.Regular,
                    shading = false,
                    fallback = Modifier.background(menuBackground, menuShape),
                )
            } else {
                Modifier.background(menuBackground, menuShape)
            }
        }
        Column(
            modifier = Modifier.width(width)
                .heightIn(max = (configuration.screenHeightDp * 0.75f).dp)
                .then(
                    if (!hasMaterial) {
                        Modifier.shadow(
                            8.dp,
                            menuShape,
                            ambientColor = if (menuDark) Color.Black.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.18f),
                            spotColor = if (menuDark) Color.Black.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.18f),
                        ).clip(menuShape)
                    } else {
                        Modifier
                    },
                )
                .then(surfaceModifier)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            actions.forEach { action ->
                val tint = menuForeground.copy(alpha = if (action.unavailable == null) 1f else 0.45f)
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
