package io.github.mangi.eta.ui.screens.prompts

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import io.github.mangi.eta.agent.prompt.PromptPreset
import io.github.mangi.eta.agent.prompt.PromptPresetRepository
import io.github.mangi.eta.data.db.EtaDatabase
import io.github.mangi.eta.ui.components.coui.CouiPageBottomSpacer
import io.github.mangi.eta.ui.components.coui.CouiPreference
import io.github.mangi.eta.ui.components.coui.CouiScaffoldPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 提示词预设：把 base / persona / append 合成一份系统提示词，写入所有已启用 provider 的 `system_prompt`。
 *
 * 官方版没有全局人格层，提示词挂在 provider 上，所以这里直接写那一列。
 * 空预设不跳过：它的 base 就是官方内置提示词，写入即等于恢复默认。
 */
private fun PromptPreset.effectiveSystemPrompt(): String {
    val parts = ArrayList<String>(3)
    if (baseSystemPrompt.isNotBlank()) parts += baseSystemPrompt.trim()
    if (personaName.isNotBlank() || personaStyle.isNotBlank()) {
        parts += buildString {
            if (personaName.isNotBlank()) append("身份：").append(personaName.trim())
            if (personaStyle.isNotBlank()) append("\n风格：").append(personaStyle.trim())
        }
    }
    if (appendInstructions.isNotBlank()) parts += appendInstructions.trim()
    return parts.joinToString("\n\n")
}

/** 生效提示词的摘要：首行 + 字数，用于「当前生效」那一行。 */
private fun summarize(text: String?): String {
    val body = text?.trim().orEmpty()
    if (body.isEmpty()) return "未设置"
    val firstLine = body.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
    val head = if (firstLine.length > 28) firstLine.take(28) + "…" else firstLine
    return "$head · ${body.length} 字"
}

@Composable
internal fun PromptPresetsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val presets = remember(context) {
        PromptPresetRepository.builtins(context) + PromptPresetRepository.imported()
    }
    var current by remember { mutableStateOf<String?>(null) }
    var appliedName by remember { mutableStateOf<String?>(null) }

    suspend fun readCurrent(): String? = withContext(Dispatchers.IO) {
        val dao = EtaDatabase.get(context.applicationContext).providerDao()
        dao.providers().firstOrNull { it.provider.isEnabled }?.provider?.systemPrompt
    }

    LaunchedEffect(Unit) {
        current = runCatching { readCurrent() }.getOrNull()
    }

    val applyPreset: (PromptPreset) -> Unit = { preset ->
        val text = preset.effectiveSystemPrompt()
        scope.launch {
            val count = runCatching {
                withContext(Dispatchers.IO) {
                    val dao = EtaDatabase.get(context.applicationContext).providerDao()
                    val targets = dao.providers().filter { it.provider.isEnabled }
                    targets.forEach { item ->
                        dao.updateProvider(item.provider.copy(systemPrompt = text))
                    }
                    targets.size
                }
            }.getOrElse { -1 }
            if (count >= 0) {
                current = text
                appliedName = preset.name
                Toast.makeText(context, "已应用到 $count 个提供商", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "写入失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val builtin = presets.filter { it.builtin }
    val imported = presets.filterNot { it.builtin }

    CouiScaffoldPage(title = "提示词预设", onBack = onBack, scrollState = scrollState) {
        Group(title = "当前生效") {
            CouiPreference(
                title = appliedName ?: "系统提示词",
                summary = summarize(current),
            )
        }

        if (builtin.isNotEmpty()) {
            Group(title = "内置预设") {
                builtin.forEachIndexed { index, preset ->
                    if (index > 0) Divider()
                    PresetRow(preset, onClick = { applyPreset(preset) })
                }
            }
        }

        if (imported.isNotEmpty()) {
            Group(title = "导入的预设") {
                imported.forEachIndexed { index, preset ->
                    if (index > 0) Divider()
                    PresetRow(preset, onClick = { applyPreset(preset) })
                }
            }
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
            Spacer(Modifier.height(8.dp))
            CouiPreference(
                title = "应用规则",
                summary = "点击预设会覆盖所有已启用提供商的系统提示词。官方版把提示词挂在提供商上，没有全局人格层；" +
                    "「系统默认」即恢复官方内置提示词。",
            )
        }

        CouiPageBottomSpacer()
    }
}

@Composable
private fun PresetRow(preset: PromptPreset, onClick: () -> Unit) {
    CouiPreference(
        title = preset.name,
        summary = preset.description.ifBlank {
            if (preset.hasContent) "${preset.contentLength} 字" else "空预设"
        },
        onClick = onClick,
    )
}