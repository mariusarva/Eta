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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

/**
 * 记录「刚才这一小段时间内，有横向滚动容器在拖」。
 *
 * 用可观察状态 + 定时复位：可观察让 HorizontalPager 能实时关掉翻页，
 * 定时复位保证不会永久卡住。
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
 * 给横向滚动容器（代码块 / 表格 / 公式）加的两件事：
 *
 * 1. `nestedScroll`：把横向位移的**剩余量**全部吃掉。子内容先正常滚，滚到边界后
 *    剩下的 dx 不会再冒泡给 HorizontalPager —— 这就是「拖表格文字不会翻页」的关键。
 *    `userScrollEnabled` 要等重组才生效，当帧来不及；nestedScroll 是同步的。
 *
 * 2. `pointerInput`：声明「这段手势归横向滚动」，让侧栏右滑手势让位。
 *    只在容器真的还能朝该方向滚时才声明，避免到边界的表格长期吃掉页级手势。
 */
internal fun Modifier.declareHorizontalScroll(
    lock: HorizontalScrollLock?,
    scrollState: ScrollState,
): Modifier {
    if (lock == null) return this
    val blocker = object : NestedScrollConnection {
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            if (available.x == 0f) return Offset.Zero
            // 声明过横向滚动就吃掉剩余水平位移，不交给父级 Pager。
            if (lock.isActive() || scrollState.canScrollBackward || scrollState.canScrollForward) {
                lock.markActive()
                return Offset(available.x, 0f)
            }
            return Offset.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            if (available.x == 0f) return Velocity.Zero
            if (lock.isActive()) return Velocity(available.x, 0f)
            return Velocity.Zero
        }
    }
    return this
        .nestedScroll(blocker)
        .pointerInput(lock, scrollState) {
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
