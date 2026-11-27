package io.github.mangi.eta.ui.components

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

/**
 * 记录「刚才这一小段时间内，有横向滚动容器在拖」。
 *
 * 用可观察状态 + 定时复位：可观察让 HorizontalPager 能实时关掉翻页
 * （横向拖动代码块/表格时不该连带翻页），定时复位保证不会永久卡住。
 */
internal class HorizontalScrollLock {
    var active by mutableStateOf(false)
        private set

    private val handler = Handler(Looper.getMainLooper())
    private val resetRunnable = Runnable { active = false }

    fun markActive(durationMs: Long = 320L) {
        active = true
        handler.removeCallbacks(resetRunnable)
        handler.postDelayed(resetRunnable, durationMs)
    }

    fun isActive(): Boolean = active
}

internal val LocalHorizontalScrollLock = staticCompositionLocalOf<HorizontalScrollLock?> { null }

/**
 * 挂在横向滚动容器外层（和 horizontalScroll 同一节点即可）。不消费任何事件，只做方向声明。
 *
 * 只在容器**真的还能朝这个方向滚**时才声明，否则到边界的表格会把页级右滑永久吃掉：
 * - 手指左滑看右边内容 → 需要 canScrollForward
 * - 手指右滑看左边内容 → 需要 canScrollBackward
 */
internal fun Modifier.declareHorizontalScroll(
    lock: HorizontalScrollLock?,
    scrollState: ScrollState,
): Modifier {
    if (lock == null) return this
    return this.pointerInput(lock, scrollState) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var totalX = 0f
            var totalY = 0f
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) break
                val delta = change.position - change.previousPosition
                totalX += delta.x
                totalY += delta.y
                if (abs(totalX) > 6f && abs(totalX) > abs(totalY) * 1.2f) {
                    val canConsume = if (delta.x < 0f) {
                        scrollState.canScrollForward
                    } else {
                        scrollState.canScrollBackward
                    }
                    if (canConsume) lock.markActive()
                }
            }
        }
    }
}
