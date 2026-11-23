package io.github.mangi.eta.data.model

import kotlinx.serialization.Serializable

private const val MIMO_SPEECH_BASE_URL = "https://api.xiaomimimo.com/v1"
private const val DEEPSEEK_SPEECH_BASE_URL = "https://api.deepseek.com"

@Serializable
enum class AsrProvider { QWEN_REALTIME, QWEN_FLASH, QWEN_FILE, DOUBAO, MIMO, SHERPA_NCNN, QWEN3_LOCAL, LOCAL, DEEPSEEK;

    /** 保留旧枚举名与序数可读，新 UI 只提供 LOCAL。 */
    val canonical: AsrProvider get() = when (this) {
        SHERPA_NCNN, QWEN3_LOCAL -> LOCAL
        else -> this
    }
    val isLocal: Boolean get() = canonical == LOCAL
    companion object { val selectable: List<AsrProvider> = entries.filter { it == it.canonical } }
}

@Serializable
enum class TtsProvider { NONE, QWEN, DOUBAO, GEMINI, MINIMAX, MIMO, SYSTEM, DEEPSEEK }

@Serializable
enum class SpeechRegion { BEIJING, SINGAPORE }

@Serializable
data class QwenSpeechConfig(
    val region: SpeechRegion = SpeechRegion.BEIJING,
    val baseUrl: String = "",
) {
    fun endpoint(): String = baseUrl.trim().trimEnd('/').ifEmpty {
        if (region == SpeechRegion.BEIJING) "https://dashscope.aliyuncs.com"
        else "https://dashscope-intl.aliyuncs.com"
    }
}

@Serializable
enum class MimoAsrLanguage(val apiId: String) {
    AUTO("auto"),
    CHINESE("zh"),
    ENGLISH("en"),
}

@Serializable
data class MimoAsrConfig(
    val baseUrl: String = MIMO_SPEECH_BASE_URL,
    val language: MimoAsrLanguage = MimoAsrLanguage.AUTO,
)

@Serializable
data class DoubaoSpeechConfig(
    val baseUrl: String = "https://openspeech.bytedance.com",
    val legacyAuth: Boolean = false,
    val appId: String = "",
    val resourceId: String = "",
)

@Serializable
enum class GeminiTtsModel(val apiId: String) {
    FLASH("gemini-3.8-flash-tts"),
    FLASH_LITE("gemini-3.8-flash-lite-tts"),
}

@Serializable
data class GeminiTtsConfig(
    val baseUrl: String = "https://generativelanguage.googleapis.com",
    val model: GeminiTtsModel = GeminiTtsModel.FLASH,
    val voice: String = "Kore",
    val style: String = "",
)

@Serializable
enum class MiniMaxTtsModel(val apiId: String) {
    TURBO("speech-2.8-turbo"),
    HD("speech-2.8-hd"),
}

@Serializable
data class MiniMaxTtsConfig(
    val baseUrl: String = "https://api.minimax.cn",
    val model: MiniMaxTtsModel = MiniMaxTtsModel.TURBO,
    val voice: String = "female-chengshu",
    val speed: Float = 1f,
)

@Serializable
data class MimoTtsConfig(
    val baseUrl: String = MIMO_SPEECH_BASE_URL,
    val voice: String = "mimo_default",
    val style: String = "",
)

@Serializable
data class DeepSeekAsrConfig(
    val baseUrl: String = DEEPSEEK_SPEECH_BASE_URL,
)

@Serializable
data class DeepSeekTtsConfig(
    val baseUrl: String = DEEPSEEK_SPEECH_BASE_URL,
    val voice: String = "stella",
    val filterMarkdown: Boolean = true,
)

@Serializable
data class SpeechOssConfig(
    val region: String = "cn-beijing",
    val endpoint: String = "https://oss-cn-beijing.aliyuncs.com",
    val bucket: String = "",
    val prefix: String = "eta-speech/",
)

/** 纯离线 Android TTS：引擎留空表示系统默认，不做任何云端回退。 */
@Serializable
data class SystemTtsConfig(
    val engine: String = "",
    val language: String = "zh-CN",
    val rate: Float = 1f,
    val pitch: Float = 1f,
    /** 显式开启；false 时保留旧设置的严格离线元数据校验。 */
    val useSystemDefault: Boolean = false,
    /**
     * 指定音色名（TextToSpeech Voice.name）；留空表示按语言自动挑第一个已安装的离线音色。
     * 角色卡音色覆盖系统 TTS 时写这里——系统音色的唯一稳定标识就是 name。
     */
    val voiceName: String = "",
)

@Serializable
data class SpeechSettings(
    val asr: AsrProvider = AsrProvider.QWEN_REALTIME,
    val tts: TtsProvider = TtsProvider.NONE,
    val autoSpeak: Boolean = false,
    /** 聊天页回答完成后自动朗读最终回答；与助手浮窗的自动播报分开保存。 */
    val chatAutoSpeak: Boolean = false,
    val language: String = "",
    val qwenAsr: QwenSpeechConfig = QwenSpeechConfig(),
    val qwenTts: QwenSpeechConfig = QwenSpeechConfig(),
    val doubaoAsr: DoubaoSpeechConfig = DoubaoSpeechConfig(),
    val doubaoTts: DoubaoSpeechConfig = DoubaoSpeechConfig(),
    val qwenVoice: String = "Cherry",
    val doubaoVoice: String = "zh_female_vv_uranus_bigtts",
    val geminiTts: GeminiTtsConfig = GeminiTtsConfig(),
    val minimaxTts: MiniMaxTtsConfig = MiniMaxTtsConfig(),
    val oss: SpeechOssConfig = SpeechOssConfig(),
    /** 助手浮窗停顿多久后自动发送；0 表示只在用户点击完成或松手时发送。 */
    val autoSendSilenceMs: Int = DEFAULT_AUTO_SEND_SILENCE_MS,
    /** 识别结果交付前使用当前对话模型保守纠错。 */
    val refineTranscript: Boolean = false,
    /** 只控制 Eta 自身的语音输入，关闭时保留服务配置以便再次开启。 */
    val recognitionEnabled: Boolean = true,
    val mimoAsr: MimoAsrConfig = MimoAsrConfig(),
    val mimoTts: MimoTtsConfig = MimoTtsConfig(),
    val deepseekAsr: DeepSeekAsrConfig = DeepSeekAsrConfig(),
    val deepseekTts: DeepSeekTtsConfig = DeepSeekTtsConfig(),
    val systemTts: SystemTtsConfig = SystemTtsConfig(),
) {
    companion object {
        const val DEFAULT_AUTO_SEND_SILENCE_MS = 2_000
        val AUTO_SEND_SILENCE_OPTIONS = listOf(1_000, 1_500, 2_000, 3_000, 0)
    }

    /**
     * 用角色卡指定的服务商与音色覆盖朗读配置；两者都为空时原样返回。
     *
     * 服务商与音色必须成对覆盖：音色 id 是各家自己的标识（`Cherry` 是千问、
     * `zh_female_vv_uranus_bigtts` 是豆包），只覆盖音色会在全局切换服务商后失效。
     * providerName 无法解析成枚举时退回只覆盖音色，避免旧卡里的未知值让朗读整体失效。
     */
    fun withVoiceOverride(voiceId: String?, providerName: String? = null): SpeechSettings {
        val id = voiceId?.trim().orEmpty()
        val provider = providerName?.trim()
            ?.let { name -> TtsProvider.entries.firstOrNull { it.name.equals(name, ignoreCase = true) } }
        val base = if (provider != null && provider != TtsProvider.NONE) copy(tts = provider) else this
        if (id.isEmpty()) return base
        return base.withVoiceId(id)
    }

    private fun withVoiceId(id: String): SpeechSettings = when (tts) {
        TtsProvider.QWEN -> copy(qwenVoice = id)
        TtsProvider.DOUBAO -> copy(doubaoVoice = id)
        TtsProvider.GEMINI -> copy(geminiTts = geminiTts.copy(voice = id))
        TtsProvider.MINIMAX -> copy(minimaxTts = minimaxTts.copy(voice = id))
        TtsProvider.MIMO -> copy(mimoTts = mimoTts.copy(voice = id))
        TtsProvider.DEEPSEEK -> copy(deepseekTts = deepseekTts.copy(voice = id))
        // 系统 TTS 的音色就是引擎声明的 Voice.name，可直接用角色卡的音色 id 覆盖。
        TtsProvider.SYSTEM -> copy(systemTts = systemTts.copy(voiceName = id))
        TtsProvider.NONE -> this
    }
}

internal enum class SpeechCredentialField {
    QWEN_ASR, QWEN_TTS, DOUBAO_ASR, DOUBAO_TTS, GEMINI_TTS, MINIMAX_TTS, OSS_KEY_ID, OSS_KEY_SECRET,
    MIMO_ASR, MIMO_TTS, DEEPSEEK_ASR, DEEPSEEK_TTS,
}

/** 凭据不属于 Settings，避免被普通配置导出或调试输出带出。 */
@Serializable
class SpeechCredentials(
    val qwenAsr: String = "",
    val qwenTts: String = "",
    val doubaoAsr: String = "",
    val doubaoTts: String = "",
    val ossAccessKeyId: String = "",
    val ossAccessKeySecret: String = "",
    val geminiTts: String = "",
    val minimaxTts: String = "",
    val mimoAsr: String = "",
    val mimoTts: String = "",
    val deepseekAsr: String = "",
    val deepseekTts: String = "",
) {
    internal fun withValue(field: SpeechCredentialField, value: String): SpeechCredentials = SpeechCredentials(
        qwenAsr = if (field == SpeechCredentialField.QWEN_ASR) value else qwenAsr,
        qwenTts = if (field == SpeechCredentialField.QWEN_TTS) value else qwenTts,
        doubaoAsr = if (field == SpeechCredentialField.DOUBAO_ASR) value else doubaoAsr,
        doubaoTts = if (field == SpeechCredentialField.DOUBAO_TTS) value else doubaoTts,
        ossAccessKeyId = if (field == SpeechCredentialField.OSS_KEY_ID) value else ossAccessKeyId,
        ossAccessKeySecret = if (field == SpeechCredentialField.OSS_KEY_SECRET) value else ossAccessKeySecret,
        geminiTts = if (field == SpeechCredentialField.GEMINI_TTS) value else geminiTts,
        minimaxTts = if (field == SpeechCredentialField.MINIMAX_TTS) value else minimaxTts,
        mimoAsr = if (field == SpeechCredentialField.MIMO_ASR) value else mimoAsr,
        mimoTts = if (field == SpeechCredentialField.MIMO_TTS) value else mimoTts,
        deepseekAsr = if (field == SpeechCredentialField.DEEPSEEK_ASR) value else deepseekAsr,
        deepseekTts = if (field == SpeechCredentialField.DEEPSEEK_TTS) value else deepseekTts,
    )

    override fun toString(): String = "SpeechCredentials(redacted)"
}
