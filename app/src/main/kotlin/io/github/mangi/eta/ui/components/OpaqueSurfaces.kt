package io.github.mangi.eta.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 弹窗/浮层的**不透明**表面色，且跟随当前主题（含 Monet 取色）。
 *
 * 为什么需要它：Miuix 的 `surface` / `surfaceContainer` 系列在深色下带 alpha
 * （MD3 的容器色本就是叠加色），直接拿来当弹窗底色会透出下层列表，文字糊成一片。
 * 早期各处用写死的 `#FFFFFF` / `#2A2A2E` 兜底，能挡住透底但**绕过了 Monet**——
 * 换了种子色弹窗仍是死白/死灰。
 *
 * 这里改为：把主题容器色按自身 alpha 合成到页面背景上，得到等价的不透明结果，
 * 同时保留主题色相。alpha 为 1 时合成结果就是原色，不会有任何偏移。
 *
 * @param container 想用的容器色，默认 `surfaceContainerHigh`（比页面底色高一档，
 *   弹窗与背景有层次）。需要更亮/更暗时传 `surfaceContainerHighest` / `surfaceContainer`。
 */
@Composable
@ReadOnlyComposable
internal fun opaqueThemedSurface(
    container: Color = MiuixTheme.colorScheme.surfaceContainerHigh,
): Color {
    val base = MiuixTheme.colorScheme.background
    return if (container.alpha >= 0.999f) container else container.compositeOver(base)
}

/**
 * 页面主色系的不透明表面：用于需要与页面同色但必须挡住的场景（如侧栏对话框）。
 * 优先用 `surfaceContainerHigh`，其不透明度不足时自动往上取一档。
 */
@Composable
@ReadOnlyComposable
internal fun opaquePageSurface(): Color {
    val colors = MiuixTheme.colorScheme
    val base = colors.background
    val high = colors.surfaceContainerHigh
    if (high.alpha >= 0.999f) return high
    val highest = colors.surfaceContainerHighest
    return (if (highest.alpha >= high.alpha) highest else high).compositeOver(base)
}

/** 当前是否深色主题；弹窗里做少量明暗微调时用，避免各页各写一套 luminance 判断。 */
@Composable
@ReadOnlyComposable
internal fun themedIsDark(): Boolean = MiuixTheme.colorScheme.surface.luminance() < 0.5f
