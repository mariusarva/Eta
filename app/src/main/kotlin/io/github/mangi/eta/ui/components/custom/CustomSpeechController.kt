package io.github.mangi.eta.ui.components.custom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.io.Closeable
import java.io.File
import java.lang.ref.WeakReference
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class CustomSpeechPhase { IDLE, PREPARING, PLAYING, PAUSED, ERROR }
internal data class CustomSpeechState(
    val messageId: String? = null,
    val phase: CustomSpeechPhase = CustomSpeechPhase.IDLE,
    val sentences: List<String> = emptyList(),
    val sentence: Int = 0,
    val positionMs: Int = 0,
    val durationMs: Int = 0,
    val error: String? = null,
)

/** Composition-owned, main-thread-only local TTS. Never touches official/cloud playback settings.
 * TTS writes a private WAV; only MediaPlayer plays it. Pause/resume retain the same player.
 * Both session and segment tokens are checked after every asynchronous boundary.
 */
internal class CustomSpeechController(context: Context, private val scope: CoroutineScope) : Closeable {
    private val app = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val mutableState = MutableStateFlow(CustomSpeechState())
    val state = mutableState.asStateFlow()
    private var generation = 0L
    private var segmentGeneration = 0L
    private var closed = false
    private var foreground = true
    private var engine: TextToSpeech? = null
    private var player: MediaPlayer? = null
    private var prepared = false
    private var wantsPlayback = true
    private var formatting: Job? = null
    private var watchdog: Runnable? = null
    private var directory: File? = null
    private val cache = mutableMapOf<Int, File>()
    private data class Pending(val id: String, val file: File, val session: Long, val segment: Long, val index: Int)
    private var pending: Pending? = null
    private val audio = app.getSystemService(AudioManager::class.java)
    private var focus: AudioFocusRequest? = null
    private var receiverRegistered = false
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { stop() }
    }

    fun setForeground(value: Boolean) {
        foreground = value
        if (!value) stop()
    }

    fun speak(messageId: String, text: String) {
        if (closed) return
        stop()
        if (!foreground) return
        active.get()?.takeIf { it !== this }?.stop()
        active = WeakReference(this)
        val session = generation
        mutableState.value = CustomSpeechState(messageId, CustomSpeechPhase.PREPARING)
        formatting = scope.launch {
            try {
                val sentences = withContext(Dispatchers.Default) { CustomSpeechText.sentences(text) }
                if (!valid(session)) return@launch
                require(sentences.isNotEmpty()) { "没有可朗读的正文（网址、表格和代码已略过）" }
                mutableState.value = state.value.copy(sentences = sentences)
                directory = java.nio.file.Files.createTempDirectory(app.cacheDir.toPath(), "eta-local-tts-").toFile()
                app.registerReceiver(receiver, IntentFilter().apply {
                    addAction(Intent.ACTION_SCREEN_OFF)
                    addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                }, Context.RECEIVER_NOT_EXPORTED)
                receiverRegistered = true
                armTimeout(session, "系统朗读引擎初始化超时")
                engine = TextToSpeech(app) { result -> handler.post {
                    if (!valid(session)) return@post
                    try {
                        check(result == TextToSpeech.SUCCESS) { "系统朗读引擎不可用" }
                        val tts = checkNotNull(engine)
                        // Never fall back to a network voice or trigger voice-data installation.
                        val voices = tts.voices.orEmpty()
                        val locale = io.github.mangi.eta.agent.voice.systemTtsLocale(Locale.getDefault().toLanguageTag())
                        val engineName = tts.defaultEngine
                        val voice = io.github.mangi.eta.agent.voice.chooseOfflineSystemVoice(
                            voices, locale, tts.defaultVoice?.name, engineName,
                        ) ?: error(if (voices.isEmpty()) {
                            // This MediaPlayer entry remains strict and does not read global TTS policy.
                            "引擎未提供音色元数据，无法确认离线；此朗读入口仅使用可确认的离线音色，请检查系统文字转语音设置"
                        } else io.github.mangi.eta.agent.voice.offlineSystemVoiceFailure(voices, locale, engineName))
                        check(tts.setVoice(voice) == TextToSpeech.SUCCESS) { "无法启用系统离线音色" }
                        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) = Unit
                            override fun onDone(utteranceId: String?) { handler.post { synthesized(utteranceId) } }
                            @Deprecated("Android callback")
                            override fun onError(utteranceId: String?) { synthesisError(utteranceId) }
                            override fun onError(utteranceId: String?, errorCode: Int) { synthesisError(utteranceId) }
                        })
                        loadSentence(0)
                    } catch (error: Exception) { fail(error.message ?: "系统离线朗读初始化失败") }
                } }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (valid(session)) fail(error.message ?: "本地朗读失败")
            }
        }
    }

    private fun synthesisError(id: String?) { handler.post {
        val request = pending
        if (request != null && request.id == id && valid(request.session, request.segment))
            fail("系统离线语音合成失败，请检查已安装音色")
    } }

    private fun loadSentence(index: Int) {
        if (closed || !foreground || index !in state.value.sentences.indices) return
        segmentGeneration++
        clearTimeout()
        releasePlayer()
        engine?.stop()
        pending?.file?.delete(); pending = null
        val session = generation
        val segment = segmentGeneration
        mutableState.value = state.value.copy(sentence = index, positionMs = 0, durationMs = 0,
            phase = if (wantsPlayback) CustomSpeechPhase.PREPARING else CustomSpeechPhase.PAUSED)
        cache[index]?.takeIf { it.isFile }?.let { prepare(it, session, segment); return }
        val file = File(checkNotNull(directory), "$index-$segment.wav")
        val request = Pending("$session:$segment:$index", file, session, segment, index)
        pending = request
        armTimeout(session, "系统离线语音合成超时")
        val result = engine?.synthesizeToFile(state.value.sentences[index], Bundle(), file, request.id)
        if (result != TextToSpeech.SUCCESS) fail("无法启动系统离线语音合成")
    }

    private fun synthesized(id: String?) {
        val request = pending ?: return
        if (request.id != id || !valid(request.session, request.segment)) return
        pending = null
        clearTimeout()
        if (!request.file.isFile || request.file.length() == 0L) {
            request.file.delete(); fail("系统朗读返回了空音频"); return
        }
        cache[request.index] = request.file
        prepare(request.file, request.session, request.segment)
    }

    private fun prepare(file: File, session: Long, segment: Long) {
        try {
            val created = MediaPlayer()
            player = created
            created.setAudioAttributes(ATTRIBUTES)
            created.setOnPreparedListener { ready ->
                if (!valid(session, segment) || player !== ready) return@setOnPreparedListener
                clearTimeout()
                try {
                    prepared = true
                    mutableState.value = state.value.copy(durationMs = ready.duration.coerceAtLeast(0))
                    if (wantsPlayback) startPrepared() else mutableState.value = state.value.copy(phase = CustomSpeechPhase.PAUSED)
                } catch (error: Exception) { fail(error.message ?: "本地音频准备失败") }
            }
            created.setOnCompletionListener { completed ->
                if (valid(session, segment) && player === completed) {
                    val next = state.value.sentence + 1
                    if (next in state.value.sentences.indices) loadSentence(next) else stop()
                }
            }
            created.setOnErrorListener { failed, _, _ ->
                if (valid(session, segment) && player === failed) fail("本地语音播放失败")
                true
            }
            created.setDataSource(file.absolutePath)
            armTimeout(session, "本地音频准备超时")
            created.prepareAsync()
        } catch (error: Exception) { if (valid(session, segment)) fail(error.message ?: "本地音频打开失败") }
    }

    private fun startPrepared() {
        try {
            if (!foreground || !prepared) return
            if (focus == null) {
                val session = generation
                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(ATTRIBUTES)
                    .setOnAudioFocusChangeListener({ change ->
                        if (valid(session) && change != AudioManager.AUDIOFOCUS_GAIN) stop()
                    }, handler).build()
                check(audio.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) { "无法获得音频焦点" }
                focus = request
            }
            player?.start()
            mutableState.value = state.value.copy(phase = CustomSpeechPhase.PLAYING)
        } catch (error: Exception) { fail(error.message ?: "本地语音播放失败") }
    }

    fun pause() {
        if (state.value.messageId == null || state.value.phase == CustomSpeechPhase.ERROR) return
        wantsPlayback = false
        try {
            if (prepared) player?.pause()
            refreshProgress()
            mutableState.value = state.value.copy(phase = CustomSpeechPhase.PAUSED)
        } catch (error: Exception) { fail(error.message ?: "暂停失败") }
    }

    fun resume() {
        if (!foreground || state.value.phase != CustomSpeechPhase.PAUSED) return
        wantsPlayback = true
        if (prepared) startPrepared() else mutableState.value = state.value.copy(phase = CustomSpeechPhase.PREPARING)
    }

    fun seekTo(positionMs: Int) {
        if (!prepared) return
        try {
            val position = positionMs.coerceIn(0, state.value.durationMs)
            player?.seekTo(position.toLong(), MediaPlayer.SEEK_CLOSEST)
            mutableState.value = state.value.copy(positionMs = position)
        } catch (error: Exception) { fail(error.message ?: "进度定位失败") }
    }

    /** Sentence-list navigation, independent from selection in the original message. */
    fun locateSentence(index: Int) {
        if (state.value.phase != CustomSpeechPhase.ERROR && engine != null && index in state.value.sentences.indices)
            loadSentence(index)
    }

    fun refreshProgress() {
        if (!prepared) return
        try { mutableState.value = state.value.copy(positionMs = (player?.currentPosition ?: 0).coerceIn(0, state.value.durationMs)) }
        catch (error: Exception) { fail(error.message ?: "无法读取播放进度") }
    }

    private fun valid(session: Long, segment: Long = segmentGeneration) =
        !closed && foreground && session == generation && segment == segmentGeneration

    private fun releasePlayer() {
        prepared = false
        player?.let { current ->
            current.setOnPreparedListener(null); current.setOnCompletionListener(null); current.setOnErrorListener(null)
            runCatching { current.release() }
        }
        player = null
    }

    private fun armTimeout(session: Long, message: String) {
        clearTimeout()
        val segment = segmentGeneration
        watchdog = Runnable { if (valid(session, segment)) fail(message) }.also { handler.postDelayed(it, 30_000) }
    }
    private fun clearTimeout() { watchdog?.let(handler::removeCallbacks); watchdog = null }

    private fun fail(message: String) {
        val owner = state.value.messageId
        stop()
        mutableState.value = CustomSpeechState(owner, CustomSpeechPhase.ERROR, error = message)
    }

    fun stop() {
        generation++; segmentGeneration++
        clearTimeout()
        formatting?.cancel(); formatting = null
        releasePlayer()
        engine?.let { runCatching { it.stop() }; runCatching { it.shutdown() } }; engine = null
        focus?.let { runCatching { audio.abandonAudioFocusRequest(it) } }; focus = null
        if (receiverRegistered) runCatching { app.unregisterReceiver(receiver) }
        receiverRegistered = false
        pending?.file?.delete(); pending = null
        cache.clear()
        directory?.deleteRecursively(); directory = null
        wantsPlayback = true
        if (active.get() === this) active.clear()
        mutableState.value = CustomSpeechState()
    }

    override fun close() { stop(); closed = true }

    private companion object {
        // Arbitration only; no static strong Activity/controller owner.
        var active = WeakReference<CustomSpeechController>(null)
        val ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
    }
}
