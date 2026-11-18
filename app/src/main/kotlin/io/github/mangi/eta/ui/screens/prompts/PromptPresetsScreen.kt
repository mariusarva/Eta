package io.github.mangi.eta.ui.screens.prompts

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.agent.prompt.PromptPreset
import io.github.mangi.eta.agent.prompt.PromptPresetRepository
import io.github.mangi.eta.data.db.EtaDatabase
import io.github.mangi.eta.ui.components.coui.CouiPreference
import io.github.mangi.eta.ui.components.coui.CouiScaffoldPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 把预设合成为一份系统提示词。
 *
 * 官方版没有全局人格层，提示词挂在 provider 的 `system_prompt` 列上，所以这里直接产出该列的值。
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

@Composable
internal fun PromptPresetsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val presets = remember(context) {
        PromptPresetRepository.builtins(context) + PromptPresetRepository.imported()
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
            val message = if (count >= 0) {
                "已写入 $count 个提供商（${text.length} 字）"
            } else {
                "写入失败"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    CouiScaffoldPage(title = "提示词预设", onBack = onBack) {
        CouiPreference(
            title = "应用方式",
            summary = "点击任意预设会覆盖所有已启用提供商的系统提示词；官方版的提示词挂在 provider 上，没有全局人格层。选「系统默认」即恢复官方内置提示词。",
        )
        Spacer(Modifier.height(8.dp))
        presets.forEach { preset ->
            CouiPreference(
                title = preset.name,
                summary = preset.description.ifBlank {
                    if (preset.hasContent) "${preset.contentLength} 字" else "空预设"
                },
                onClick = { applyPreset(preset) },
            )
        }
    }
}