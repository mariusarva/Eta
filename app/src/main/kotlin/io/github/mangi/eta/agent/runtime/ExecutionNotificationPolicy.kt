package io.github.mangi.eta.agent.runtime

import io.github.mangi.eta.data.datastore.SettingsDataStore
import kotlinx.coroutines.runBlocking

/**
 * 「运行通知大图标」开关的进程内快照。
 *
 * 通知在状态变化时重建，不能每次都读 DataStore；这里缓存一份，设置页切换时同步写入。
 */
internal object ExecutionNotificationPolicy {

    @Volatile
    private var largeIconEnabled = true

    @Volatile
    private var loaded = false

    fun isLargeIconEnabled(): Boolean {
        if (!loaded) refresh()
        return largeIconEnabled
    }

    fun refresh(): Boolean {
        largeIconEnabled = runCatching {
            runBlocking { SettingsDataStore.executionNotificationLargeIcon() }
        }.getOrDefault(true)
        loaded = true
        return largeIconEnabled
    }

    fun set(value: Boolean) {
        largeIconEnabled = value
        loaded = true
    }
}
