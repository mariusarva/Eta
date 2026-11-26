package io.github.mangi.eta.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

/**
 * 记录「当前手势已经被内层横向滚动容器接管」。
 *
 * 对话页整页有一个右滑手势（打开侧边工作台），它在 [PointerEventPass.Initial] 阶段抢在
 * 所有子节点之前判定，因此会抢走落在表格 / 代码块上的右滑。表现为：长表格滑到最右后
 * 想再滑回左边，手指一动就被当成「打开侧栏」，表格纹丝不动。
 *
 * 这里让内层横向容器先声明方向：只要位移以水平为主就把 [dragging] 置位，页级手势看到后
 * 立刻让位。子节点在该 pass 中晚于父节点执行，因此父节点最早在下一帧读到标记，而页级阈值
 * 远大于内层阈值，不会出现一帧误判。
 */
internal class HorizontalScrollLock {
    @Volatile
    var dragging: Boolean = false
        private set

    internal fun setDragging(value: Boolean) {
        dragging = value
    }
}

internal val LocalHorizontalScrollLock = staticCompositionLocalOf<HorizontalScrollLock?> { null }

/**
 * 挂在横向滚动容器外层（顺序无所谓，只要和 horizontalScroll 同一节点）。
 * 不消费任何事件，只做方向声明。
 */
internal fun Modifier.declareHorizontalScroll(lock: HorizontalScrollLock?): Modifier {
    if (lock == null) return this
    return this.pointerInput(lock) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var totalX = 0f
            var totalY = 0f
            var claimed = false
            try {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break
                    val delta = change.position - change.previousPosition
                    totalX += delta.x
                    totalY += delta.y
                    if (!claimed && abs(totalX) > 6f && abs(totalX) > abs(totalY) * 1.2f) {
                        claimed = true
                        lock.setDragging(true)
                    }
                }
            } finally {
                lock.setDragging(false)
            }
        }
    }
}
