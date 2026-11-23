package io.github.mangi.eta.ui.voice

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.mangi.eta.agent.voice.inspectSystemTts
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import io.github.mangi.eta.ui.components.coui.CouiSwitchPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.mangi.eta.R
import io.github.mangi.eta.data.model.AppearanceUiStyle
import io.github.mangi.eta.ui.app.LocalUiStyle
import io.github.mangi.eta.ui.components.coui.CouiButton
import io.github.mangi.eta.ui.components.coui.CouiDropdownPreference
import io.github.mangi.eta.ui.components.SettingsTextField
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import io.github.mangi.eta.agent.voice.SystemTtsVoiceOption
import io.github.mangi.eta.agent.voice.listSystemTtsVoices

@Composable
internal fun SystemTtsSettingsSection(store: SpeechSettingsStore) {
    val context = LocalContext.current
    val settings = store.settings
    val config = settings.systemTts
    var diagnostic by remember { mutableStateOf("尚未检测") }
    var detecting by remember { mutableStateOf(false) }
    var revision by remember { mutableStateOf(0) }
    // 系统引擎音色列表：引擎或语言变化后重新读，供音色下拉使用。
    var systemVoices by remember { mutableStateOf<List<SystemTtsVoiceOption>>(emptyList()) }
    LaunchedEffect(config.engine, config.useSystemDefault, revision) {
        systemVoices = runCatching { listSystemTtsVoices(context, config.engine, offlineOnly = !config.useSystemDefault) }
            .getOrDefault(emptyList())
    }
    LaunchedEffect(config.engine, config.language, revision) {
        detecting = true
        try {
            diagnostic = inspectSystemTts(context, config).text
        } catch (_: TimeoutCancellationException) {
            diagnostic = "检测超时，已释放语音引擎。可重新检测或打开系统文字转语音设置；未发声或下载。"
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            diagnostic = "检测失败：${error.message ?: error.javaClass.simpleName}。可打开系统文字转语音设置检查；未发声或下载。"
        } finally { detecting = false }
    }
    val steps = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
    val labels = steps.map { "${it}×" }
    val openSettings = {
        store.stop()
        try {
            context.startActivity(Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            Toast.makeText(context, R.string.speech_system_tts_settings_missing, Toast.LENGTH_LONG).show()
        }
    }
    Column {
        SpeechGroupCard(title = stringResource(R.string.speech_group_voice)) {
            val policySummary = "开启：只使用引擎声明已安装且无需联网的音色。关闭：显式允许使用系统默认（联网由引擎决定）；不会在播放失败后自动切换。"
            if (LocalUiStyle.current == AppearanceUiStyle.COUI) {
                CouiSwitchPreference(title = "仅离线音色", summary = policySummary,
                    checked = !config.useSystemDefault,
                    onCheckedChange = { store.stop(); store.edit(settings.copy(systemTts = config.copy(useSystemDefault = !it))) })
            } else {
                SwitchPreference(title = "仅离线音色", summary = policySummary,
                    checked = !config.useSystemDefault,
                    onCheckedChange = { store.stop(); store.edit(settings.copy(systemTts = config.copy(useSystemDefault = !it))) })
            }
            if (config.useSystemDefault) SpeechNote("已允许使用系统默认（联网由引擎决定）。文本可能交由引擎联网处理；不代表离线。")
            SpeechFieldColumn {
                SettingsTextField(stringResource(R.string.speech_system_tts_engine), config.engine,
                    onChange = { store.edit(settings.copy(systemTts = config.copy(engine = it.trim()))) })
                SettingsTextField(stringResource(R.string.speech_system_tts_language), config.language,
                    onChange = { store.edit(settings.copy(systemTts = config.copy(language = it.trim()))) })
            }
            val rateTitle = stringResource(R.string.speech_system_tts_rate)
            val pitchTitle = stringResource(R.string.speech_system_tts_pitch)
            val settingsTitle = stringResource(R.string.speech_system_tts_settings)
            // 音色下拉：留空表示按语言自动挑第一个已安装的离线音色。
            val autoVoiceLabel = "自动（按语言挑选）"
            val voiceLabels = listOf(autoVoiceLabel) + systemVoices.map { it.label }
            val voiceIndex = systemVoices.indexOfFirst { it.name == config.voiceName.trim() }
                .let { if (it >= 0) it + 1 else 0 }
            val onVoiceSelected: (Int) -> Unit = { index ->
                store.stop()
                val picked = if (index <= 0) "" else systemVoices.getOrNull(index - 1)?.name.orEmpty()
                store.edit(settings.copy(systemTts = config.copy(voiceName = picked)))
            }
            if (LocalUiStyle.current == AppearanceUiStyle.COUI) {
                CouiDropdownPreference(title = stringResource(R.string.speech_voice), items = voiceLabels,
                    selectedIndex = voiceIndex, onSelectedIndexChange = onVoiceSelected)
                CouiDropdownPreference(title = rateTitle, items = labels, selectedIndex = steps.indexOf(config.rate),
                    onSelectedIndexChange = { index -> steps.getOrNull(index)?.let { store.edit(settings.copy(systemTts = config.copy(rate = it))) } })
                CouiDropdownPreference(title = pitchTitle, items = labels, selectedIndex = steps.indexOf(config.pitch),
                    onSelectedIndexChange = { index -> steps.getOrNull(index)?.let { store.edit(settings.copy(systemTts = config.copy(pitch = it))) } })
            } else {
                OverlayDropdownPreference(title = stringResource(R.string.speech_voice), items = voiceLabels,
                    selectedIndex = voiceIndex, onSelectedIndexChange = onVoiceSelected)
                OverlayDropdownPreference(title = rateTitle, items = labels, selectedIndex = steps.indexOf(config.rate),
                    onSelectedIndexChange = { index -> steps.getOrNull(index)?.let { store.edit(settings.copy(systemTts = config.copy(rate = it))) } })
                OverlayDropdownPreference(title = pitchTitle, items = labels, selectedIndex = steps.indexOf(config.pitch),
                    onSelectedIndexChange = { index -> steps.getOrNull(index)?.let { store.edit(settings.copy(systemTts = config.copy(pitch = it))) } })
            }
            SystemTtsCenteredAction(
                text = settingsTitle,
                enabled = !store.saving,
                onClick = openSettings,
            )
        }
        SpeechGroupCard(title = "系统 TTS 诊断") {
            SpeechNote(if (detecting) "正在读取引擎音色元数据…" else diagnostic)
            SystemTtsCenteredAction(
                text = "重新检测",
                enabled = !detecting && !store.saving,
                onClick = { revision++ },
            )
        }
        SpeechNote("仅离线音色默认开启；系统默认模式须显式关闭此限制并保存。不自动安装语言包；诊断不发声、不录音。")
    }
}

/** Card-local action: symmetric gutters, centered label and a full-width touch target. */
@Composable
private fun SystemTtsCenteredAction(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        val buttonModifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        if (LocalUiStyle.current == AppearanceUiStyle.COUI) {
            CouiButton(text = text, enabled = enabled, onClick = onClick, modifier = buttonModifier)
        } else {
            TextButton(text = text, enabled = enabled, onClick = onClick, modifier = buttonModifier)
        }
    }
}
