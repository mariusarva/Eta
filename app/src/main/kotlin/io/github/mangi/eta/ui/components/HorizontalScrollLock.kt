package io.github.mangi.eta.ui.components

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.ScrollState
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

/**
 * 记录「刚才这一小段时间内，有横向滚动容器在拖」。
 *
 * 可观察状态让 HorizontalPager 能实时关掉翻页，定时复位保证不会永久卡住。
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

    /** 手势结束立即释放，配合「按下即锁」使用。 */
    fun release() {
        handler.removeCallbacks(resetRunnable)
        active = false
    }
}

internal val LocalHorizontalScrollLock = staticCompositionLocalOf<HorizontalScrollLock?> { null }

/**
 * 只做一件事：把横向位移的**剩余量**吃掉，不让它冒泡给 HorizontalPager 翻页。
 *
 * 关键约束（踩过的坑）：
 * - 不能碰 onPostFling。横向滚动时 lock 一直处于激活状态，一旦在这里吃掉速度，
 *   内容的 fling 惯性就没了，表现就是「只能一帧一帧地滑」。
 *   翻页侧已由 `userScrollEnabled = false` 兜住，不需要靠吃 fling 来挡。
 * - 不再挂 pointerInput。每帧在 Initial pass 跑一遍手势循环纯属多余开销，
 *   声明 lock 用 onPostScroll 就够（子内容真在滚 = 这段手势归它）。
 */
internal fun Modifier.blockHorizontalPageSwipe(lock: HorizontalScrollLock?): Modifier {
    if (lock == null) return this
    // 按下即置位：只靠 nestedScroll 会死锁——Pager 手势在 Initial pass 先 consume，
    // scrollable 收不到事件就永远不会触发 onPreScroll/onPostScroll，lock 便一直 false，
    // Pager 就一直把拖代码块当成翻页。这里在 down 阶段直接锁住，抬手释放。
    val holdModifier = Modifier.pointerInput(lock) {
        awaitPointerEventScope {
            while (true) {
                val down = awaitPointerEvent(PointerEventPass.Initial)
                    .changes.firstOrNull { it.changedToDownIgnoreConsumed() }
                    ?: continue
                lock.markActive(HOLD_MS)
                var pressed = true
                while (pressed) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    pressed = event.changes.any { it.pressed }
                }
                lock.release()
            }
        }
    }
    return this.then(holdModifier).nestedScroll(
        object : NestedScrollConnection {
            // 一开始滚就标记，别等 onPostScroll。
            // Pager 的手势挂在更外层，只有尽早置位它才能及时让位。
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.x != 0f && !lock.isActive()) lock.markActive()
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.x == 0f) return Offset.Zero
                if (!lock.isActive()) lock.markActive()
                return Offset(available.x, 0f)
            }
        },
    )
}

/** 「按住即锁」的保底时长：远大于正常手势，抬手时由 release() 立刻清掉。 */
private const val HOLD_MS = 30_000L
