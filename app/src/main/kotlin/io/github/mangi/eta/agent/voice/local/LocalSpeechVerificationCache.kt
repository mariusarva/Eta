// SPDX-License-Identifier: Apache-2.0
package io.github.mangi.eta.agent.voice.local

/** Not persisted: a new process must hash once. Caller holds the model-store lock. */
internal class LocalSpeechVerificationCache<T : Any> {
    private val verified = HashMap<String, T>()
    fun needsFullVerification(id: String, fingerprint: T): Boolean = verified[id] != fingerprint
    fun forget(id: String) { verified.remove(id) }
    suspend fun verify(id: String, forceFull: Boolean, fingerprint: () -> T, fullVerify: suspend () -> Unit) {
        val before = fingerprint()
        if (forceFull || needsFullVerification(id, before)) {
            forget(id)
            fullVerify()
            record(id, before, fingerprint())
        }
    }
    fun record(id: String, before: T, after: T) {
        require(before == after) { "模型在校验时发生变化" }
        // 目录指纹（inode / ctime / mtime / nanos）变了才会重算 SHA；
        // 8 个 catalog 模型全部适用，避免每次冷加载重复哈希数百 MB。
        verified[id] = after
    }
}