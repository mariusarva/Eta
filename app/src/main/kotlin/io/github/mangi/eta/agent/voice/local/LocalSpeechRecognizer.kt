// SPDX-License-Identifier: Apache-2.0
package io.github.mangi.eta.agent.voice.local

import java.io.File

/**
 * Loaded exclusively on LocalSpeechService's serial worker. Never reference from the IPC client.
 *
 * 本 fork 只保留 Vosk 引擎：GGUF 需要 app/src/main/cpp/local-speech 的 30 MB C++ 与 CMake 构建链，
 * SenseVoice-ONNX 需要 sherpa-onnx 的 native AAR，两者都不在 Maven 上。Vosk 通过
 * `com.alphacephei:vosk-android` 直接可用，因此这里只分派到 Vosk。
 */
internal interface LocalSpeechRecognizer {
    fun recognize(pcm: FloatArray): String
    fun release()
    fun openStream(): LocalSpeechStreamingSession? = null
    companion object {
        fun create(dir: File, model: LocalSpeechModelDescriptor): LocalSpeechRecognizer = when (model.engine) {
            LocalSpeechBackend.VOSK -> VoskSpeechRecognizer.load(dir)
            LocalSpeechBackend.GGUF,
            LocalSpeechBackend.SENSEVOICE_ONNX ->
                throw UnsupportedOperationException(
                    "本版本仅支持 Vosk 离线模型；${model.engine} 需要额外的原生库支持",
                )
        }
    }
}

/** Session-owned streaming state, closed before returning the cached model lease. */
internal interface LocalSpeechStreamingSession : AutoCloseable {
    fun accept(pcm: ByteArray): String?
    fun finish(): String
}
