package io.github.mangi.eta.agent.voice

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import io.github.mangi.eta.data.model.SystemTtsConfig
import java.io.Closeable
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withTimeout

/** Native playback only; no PCM/network pipeline, mixing, pause or seek claims.
 * Owned by SpeechPlaybackController's existing audio lease. All mutations run on Main.
 * Offline by default. Engine-selected language requires explicit user opt-in.
 */
internal class SystemSpeechPlayback(private val context: Context) : Closeable {
    private val main = Handler(Looper.getMainLooper())
    private var engine: TextToSpeech? = null
    private var closed = false
    private var currentId: String? = null
    private var pending: CompletableDeferred<Unit>? = null

    suspend fun play(text: String, config: SystemTtsConfig, speechLanguage: String, onStart: () -> Unit) =
        withContext(Dispatchers.Main.immediate) {
            check(!closed)
            if (config.rate !in 0.5f..2f || config.pitch !in 0.5f..2f) {
                throw SpeechFailure(SpeechErrorCode.CONFIGURATION, "系统朗读语速和音调须在 0.5～2 倍之间")
            }
            val ready = CompletableDeferred<Int>()
            val tts = TextToSpeech(context.applicationContext, { ready.complete(it) }, config.engine.trim().ifEmpty { null })
            engine = tts
            try {
                if (withTimeout(15_000) { ready.await() } != TextToSpeech.SUCCESS) {
                    throw localFailure("系统语音引擎初始化失败，请在系统文字转语音设置中检查引擎")
                }
                if (config.engine.isNotBlank() && tts.engines.none { it.name == config.engine.trim() }) {
                    throw localFailure("所选系统语音引擎未安装，请在系统文字转语音设置中选择已安装引擎")
                }
                val tag = config.language.trim().ifEmpty { speechLanguage.trim().ifEmpty { "zh-CN" } }
                val locale = systemTtsLocale(tag)
                val metadata = withContext(Dispatchers.IO) { tts.voices.orEmpty() to tts.defaultVoice }
                val engineName = config.engine.trim().ifEmpty { tts.defaultEngine }
                val voiceResult = if (config.useSystemDefault) {
                    // Deliberate opt-in only. Never retry this path after a strict/playback failure.
                    if (withContext(Dispatchers.IO) { tts.isLanguageAvailable(locale) } < TextToSpeech.LANG_AVAILABLE) {
                        throw localFailure("系统引擎不支持所选语言，或缺少该语言数据；请检查系统文字转语音设置")
                    }
                    tts.setLanguage(locale)
                } else {
                    val voice = chooseOfflineSystemVoice(metadata.first, locale, metadata.second?.name, engineName)
                        ?: throw localFailure(offlineSystemVoiceFailure(metadata.first, locale, engineName))
                    tts.setVoice(voice)
                }
                if (voiceResult < TextToSpeech.SUCCESS ||
                    tts.setSpeechRate(config.rate) != TextToSpeech.SUCCESS ||
                    tts.setPitch(config.pitch) != TextToSpeech.SUCCESS ||
                    tts.setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()) != TextToSpeech.SUCCESS) {
                    throw localFailure("系统音色配置失败，请检查系统文字转语音设置")
                }
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    private fun dispatch(id: String?, action: () -> Unit) {
                        main.post {
                            if (!closed && id != null && id == currentId) {
                                try { action() } catch (error: Exception) { pending?.completeExceptionally(error) }
                            }
                        }
                    }
                    override fun onStart(utteranceId: String?) = dispatch(utteranceId, onStart)
                    override fun onDone(utteranceId: String?) = dispatch(utteranceId) { pending?.complete(Unit) }
                    @Deprecated("Android legacy callback")
                    override fun onError(utteranceId: String?) = fail(utteranceId)
                    override fun onError(utteranceId: String?, errorCode: Int) = fail(utteranceId)
                    override fun onStop(utteranceId: String?, interrupted: Boolean) = dispatch(utteranceId) {
                        pending?.completeExceptionally(CancellationException("System speech stopped"))
                    }
                    private fun fail(id: String?) = dispatch(id) {
                        pending?.completeExceptionally(localFailure("系统朗读失败，请检查系统语音引擎和语音数据"))
                    }
                })
                // Short sequential chunks avoid Android's per-utterance limit, including surrogate pairs.
                for (chunk in SpeechText.chunks(text, minOf(500, TextToSpeech.getMaxSpeechInputLength()))) {
                    check(!closed)
                    val done = CompletableDeferred<Unit>()
                    pending = done
                    val id = UUID.randomUUID().toString()
                    currentId = id
                    if (tts.speak(chunk, TextToSpeech.QUEUE_FLUSH, Bundle(), id) != TextToSpeech.SUCCESS) {
                        throw localFailure("系统朗读启动失败")
                    }
                    withTimeout(180_000) { done.await() }
                    currentId = null
                    pending = null
                }
            } finally {
                currentId = null
                pending?.cancel(); pending = null
                if (engine === tts) engine = null
                runCatching { tts.stop() }
                runCatching { tts.shutdown() }
            }
        }

    override fun close() {
        if (closed) return
        closed = true
        currentId = null
        pending?.cancel(); pending = null
        val tts = engine
        engine = null
        // Cleanup must not prevent the owner from releasing its lease and canceling its job.
        runCatching { tts?.stop() }
        runCatching { tts?.shutdown() }
    }
}

/** Canonicalize ISO3 language/country *before* BCP47 parsing: zho-CHN otherwise parses as chn. */
internal fun systemTtsLocale(tag: String): Locale {
    val parts = tag.trim().replace('_', '-').split('-').toMutableList()
    if (parts.isEmpty()) return Locale.ROOT
    parts[0] = canonicalTtsLanguage(parts[0])
    for (i in 1 until parts.size) {
        if (parts[i].length == 3 && parts[i].all(Char::isLetter)) {
            parts[i] = canonicalTtsCountry(parts[i])
        }
    }
    return Locale.forLanguageTag(parts.joinToString("-"))
}

private fun canonicalTtsLanguage(language: String): String = when (language.lowercase(Locale.ROOT)) {
    "zho", "cmn", "chi" -> "zh"
    "eng" -> "en"
    else -> language.lowercase(Locale.ROOT)
}

private fun canonicalTtsCountry(country: String): String {
    val upper = country.uppercase(Locale.ROOT)
    return if (upper.length == 3) Locale.getISOCountries().firstOrNull {
        runCatching { Locale("", it).isO3Country == upper }.getOrDefault(false)
    } ?: upper else upper
}

/** Only this proven Oplus metadata bug: APK builds voices from zho-CHN / eng-USA.
 * Android interprets those malformed tags as extlangs chn / usa, losing language/region.
 * Never apply these country-as-language aliases to unrelated engines or arbitrary languages.
 */
internal fun normalizedSystemVoiceLocale(locale: Locale, engine: String?): Locale {
    if (engine == "com.oplus.ttsaccessibilityengine" && locale.country.isEmpty() && locale.script.isEmpty()) {
        when (locale.language.lowercase(Locale.ROOT)) {
            "chn" -> return Locale.SIMPLIFIED_CHINESE
            "usa" -> return Locale.US
        }
    }
    return systemTtsLocale(locale.toLanguageTag().let {
        if (locale.country.length == 3) "${locale.language}-${locale.country}" else it
    })
}

internal fun isInstalledOfflineSystemVoice(voice: Voice): Boolean =
    !voice.isNetworkConnectionRequired &&
        TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS !in voice.features.orEmpty() &&
        TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in voice.features.orEmpty()

internal fun chooseOfflineSystemVoice(
    voices: Set<Voice>, locale: Locale, defaultName: String?, engine: String? = null,
): Voice? {
    val target = normalizedSystemVoiceLocale(locale, null)
    return voices.asSequence().filter {
        val candidate = normalizedSystemVoiceLocale(it.locale, engine)
        isInstalledOfflineSystemVoice(it) && target.language.isNotBlank() &&
            candidate.language == target.language &&
            (target.script.isEmpty() || candidate.script.isEmpty() || target.script == candidate.script)
    }.sortedWith(compareByDescending<Voice> { normalizedSystemVoiceLocale(it.locale, engine) == target }
        .thenByDescending { it.name == defaultName }
        .thenBy { it.name }).firstOrNull()
}

internal fun offlineSystemVoiceFailure(voices: Set<Voice>, locale: Locale, engine: String?): String = when {
    voices.isEmpty() -> "引擎未提供音色元数据，无法确认离线；可在系统朗读设置中显式允许使用系统默认（联网由引擎决定）"
    voices.any { normalizedSystemVoiceLocale(it.locale, engine).language == locale.language &&
        TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED in it.features.orEmpty() } ->
        "引擎将该语言音色标记为未安装，不能确认可离线使用；请在系统文字转语音设置中检查语音数据"
    else -> "引擎未提供该语言可确认已安装的离线音色；不会自动下载或切换联网音色，请检查系统文字转语音设置"
}

internal data class SystemTtsDiagnostics(val text: String)

/** Metadata only: never speak, synthesize, install/download data, or read credentials. */
internal suspend fun inspectSystemTts(context: Context, config: SystemTtsConfig): SystemTtsDiagnostics =
    withContext(Dispatchers.IO) {
        var tts: TextToSpeech? = null
        try {
            withTimeout(15_000) {
                val ready = CompletableDeferred<Int>()
                val instance = withContext(Dispatchers.Main.immediate) {
                    TextToSpeech(context.applicationContext, { ready.complete(it) }, config.engine.trim().ifEmpty { null })
                        .also { tts = it }
                }
                check(ready.await() == TextToSpeech.SUCCESS) { "系统语音引擎初始化失败" }
                val engine = config.engine.trim().ifEmpty { instance.defaultEngine }
                val voices = instance.voices.orEmpty()
                val default = instance.defaultVoice
                val locale = systemTtsLocale(config.language.ifBlank { "zh-CN" })
                val selected = chooseOfflineSystemVoice(voices, locale, default?.name, engine)
                SystemTtsDiagnostics(buildString {
                    appendLine("引擎：$engine")
                    appendLine("默认音色：${default?.name ?: "未提供"}")
                    appendLine("默认语言：${instance.language?.toLanguageTag() ?: "未提供"}")
                    appendLine("默认音色 locale：${default?.locale?.toLanguageTag() ?: "未提供"}")
                    appendLine("目标语言：${locale.toLanguageTag()}；可用性：${instance.isLanguageAvailable(locale)}")
                    appendLine("音色总数：${voices.size}；已安装标记：${voices.count { TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty() }}；离线可选：${voices.count(::isInstalledOfflineSystemVoice)}")
                    if (selected != null) appendLine("离线匹配：${selected.name}（${normalizedSystemVoiceLocale(selected.locale, engine).toLanguageTag()}）")
                    else appendLine(offlineSystemVoiceFailure(voices, locale, engine))
                    voices.sortedBy { it.name }.forEach {
                        appendLine("${it.name}：${it.locale.toLanguageTag()} → ${normalizedSystemVoiceLocale(it.locale, engine).toLanguageTag()}；network=${it.isNetworkConnectionRequired}；features=${it.features}")
                    }
                    append("仅检测元数据，不发声、不录音、不下载。离线判断依据引擎声明，不是流量审计。")
                })
            }
        } finally {
            withContext(NonCancellable + Dispatchers.Main.immediate) { runCatching { tts?.shutdown() } }
        }
    }

private fun localFailure(message: String) = SpeechFailure(SpeechErrorCode.AUDIO, message)
