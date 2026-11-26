package io.github.mangi.eta.ui.components

import android.os.SystemClock
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

/**
 * 记录「刚才这一小段时间内，有横向滚动容器在拖」。
 *
 * 用时间戳而不是布尔标志：布尔标志依赖协程 finally 复位，一旦某个手势被取消或
 * 被后续手势覆盖，就可能永久卡在 true，导致页级右滑永远不响应。时间戳几帧后自动过期，
 * 不存在卡死。
 */
internal class HorizontalScrollLock {
    @Volatile
    private var activeUntil: Long = 0L

    fun markActive(durationMs: Long = 260L) {
        activeUntil = SystemClock.uptimeMillis() + durationMs
    }

    fun isActive(): Boolean = SystemClock.uptimeMillis() < activeUntil
}

internal val LocalHorizontalScrollLock = staticCompositionLocalOf<HorizontalScrollLock?> { null }

/**
 * 挂在横向滚动容器外层（和 horizontalScroll 同一节点即可）。不消费任何事件，只做方向声明。
 *
 * 只在容器**真的还能朝这个方向滚**时才声明，否则到边界的表格会把页级右滑永久吃掉：
 * - 手指左滑看右边内容 → 需要 canScrollForward
 * - 手指右滑看左边内容 → 需要 canScrollBackward
 *
 * 表格已经在最左端时 canScrollBackward=false，右滑不再声明，页级手势就能正常打开侧栏。
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
