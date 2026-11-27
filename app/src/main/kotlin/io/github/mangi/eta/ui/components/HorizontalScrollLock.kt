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
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
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
/** 全局唯一的拦截器：nestedScroll 每次拿到新对象都会重建节点并打断滚动，必须复用。 */
private object SharedHorizontalBlocker : NestedScrollConnection {

    @Volatile
    var lock: HorizontalScrollLock? = null

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val current = lock ?: return Offset.Zero
        if (available.x != 0f && !current.isActive()) current.markActive()
        return Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        val current = lock ?: return Offset.Zero
        if (available.x == 0f) return Offset.Zero
        if (!current.isActive()) current.markActive()
        // 吃掉剩余水平位移，不让父级 Pager 翻页。
        return Offset(available.x, 0f)
    }
}

/**
 * 让横向滚动容器独占这段手势：
 * - 复用 [SharedHorizontalBlocker] 这一唯一实例，避免 nestedScroll 节点反复重建（卡顿根因）
 * - down 阶段就置位 lock，抬手释放；否则 Pager 手势会在 Initial pass 先 consume，
 *   scrollable 收不到事件，lock 永远激活不了
 */
internal fun Modifier.blockHorizontalPageSwipe(lock: HorizontalScrollLock?): Modifier {
    if (lock == null) return this
    SharedHorizontalBlocker.lock = lock
    return this
        .nestedScroll(SharedHorizontalBlocker)
        .pointerInput(lock) {
            awaitPointerEventScope {
                while (true) {
                    awaitPointerEvent(PointerEventPass.Initial)
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
}

/** 「按住即锁」的保底时长：远大于正常手势，抬手时由 release() 立刻清掉。 */
private const val HOLD_MS = 30_000L
