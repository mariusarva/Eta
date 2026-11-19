// SPDX-License-Identifier: Apache-2.0
package io.github.mangi.eta.agent.voice.local

import android.content.Context
import io.github.mangi.eta.agent.voice.SpeechErrorCode
import io.github.mangi.eta.agent.voice.SpeechFailure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * 已录好的 16 kHz / 单声道 / PCM16 WAV 走本地离线引擎。
 *
 * 本地服务端单次会话上限 30 秒 PCM，因此按 28 秒切段、逐段通过 [LocalSpeechEngine] 识别后拼接。
 * 全程不进云端：IPC 只到 `:local_speech`，音频不落盘也不出网。
 */
internal object LocalSpeechFileTranscription {
    /** 服务端 MAX_AUDIO_BYTES = 30 秒；这里留 2 秒余量。 */
    private const val SEGMENT_BYTES = 28 * 16_000 * 2

    suspend fun transcribe(context: Context, wav: ByteArray, onProgress: (String) -> Unit): String {
        val pcm = pcmPayload(wav)
        if (pcm.isEmpty()) throw SpeechFailure(SpeechErrorCode.NO_SPEECH, "没有录到语音，请重试")
        val segments = (pcm.size + SEGMENT_BYTES - 1) / SEGMENT_BYTES
        val parts = ArrayList<String>(segments)
        for (index in 0 until segments) {
            val start = index * SEGMENT_BYTES
            val end = minOf(start + SEGMENT_BYTES, pcm.size)
            onProgress(if (segments == 1) "正在本地识别，音频不会上传"
                else "正在本地识别第 ${index + 1}/$segments 段，音频不会上传")
            val text = transcribeSegment(context, pcm.copyOfRange(start, end))
            if (text.isNotBlank()) parts += text.trim()
        }
        return parts.joinToString(" ").trim()
    }

    private suspend fun transcribeSegment(context: Context, pcm: ByteArray): String = coroutineScope {
        val frames = Channel<ByteArray>(1)
        val feeder = launch(Dispatchers.IO) {
            try { frames.send(pcm) } finally { frames.close() }
        }
        try {
            LocalSpeechEngine.recognize(
                context = context,
                frames = frames,
                begin = {},
                onPartial = {},
                onEndpoint = {},
                endpointSilenceMs = 0,
            )
        } finally {
            feeder.cancel()
        }
    }

    /**
     * 取 RIFF/WAVE 的 `data` 块。只接受 16 kHz / 单声道 / PCM16：
     * 本地模型固定 16 kHz 输入，格式不符时明确报错，不做静默重采样。
     */
    private fun pcmPayload(wav: ByteArray): ByteArray {
        fun invalid(): Nothing = throw SpeechFailure(SpeechErrorCode.AUDIO,
            "本地离线转写需要 16 kHz 单声道 PCM16 录音")
        if (wav.size < 44 || wav[0] != 'R'.code.toByte() || wav[1] != 'I'.code.toByte() ||
            wav[2] != 'F'.code.toByte() || wav[3] != 'F'.code.toByte() ||
            wav[8] != 'W'.code.toByte() || wav[9] != 'A'.code.toByte() ||
            wav[10] != 'V'.code.toByte() || wav[11] != 'E'.code.toByte()) invalid()
        fun int(at: Int) = (wav[at].toInt() and 255) or ((wav[at + 1].toInt() and 255) shl 8) or
            ((wav[at + 2].toInt() and 255) shl 16) or ((wav[at + 3].toInt() and 255) shl 24)
        fun short(at: Int) = (wav[at].toInt() and 255) or ((wav[at + 1].toInt() and 255) shl 8)
        var offset = 12
        while (offset + 8 <= wav.size) {
            val id = String(wav, offset, 4, Charsets.US_ASCII)
            val size = int(offset + 4)
            val body = offset + 8
            if (size < 0 || body + size > wav.size) invalid()
            when (id) {
                "fmt " -> {
                    if (size < 16 || short(body) != 1 || short(body + 2) != 1 ||
                        int(body + 4) != 16_000 || short(body + 14) != 16) invalid()
                }
                "data" -> return wav.copyOfRange(body, body + (size and 1.inv()))
            }
            offset = body + size + (size and 1)
        }
        invalid()
    }
}