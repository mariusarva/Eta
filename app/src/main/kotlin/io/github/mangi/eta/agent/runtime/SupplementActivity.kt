package io.github.mangi.eta.agent.runtime

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.core.AndroidAgentLogger
import io.github.mangi.eta.data.repository.AppearanceSettingsRepository
import io.github.mangi.eta.ui.app.AgentAppTheme
import kotlinx.coroutines.runBlocking
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 「插入对话」的轻量输入弹层。
 *
 * 由运行通知上的「插入对话」拉起：独立任务栈 + 透明背景，背后仍是原来的桌面或应用，
 * 不会把 Eta 主界面带到前台。提交后走 [AgentRuntimeClient.steerRun] 插进当前运行。
 */
internal class SupplementActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        val runId = intent?.getStringExtra(EXTRA_RUN_ID).orEmpty()
        val title = intent?.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "当前运行" }
        if (runId.isBlank()) {
            finish()
            return
        }
        val appearance = runBlocking { AppearanceSettingsRepository.settings() }
        setContent {
            AgentAppTheme(appearance = appearance, systemOverlay = true) {
                SupplementSheet(
                    title = title,
                    onDismiss = { finish() },
                    onSubmit = { text ->
                        val trimmed = text.trim()
                        if (trimmed.isNotEmpty()) {
                            runCatching {
                                AgentRuntimeClient(this, AndroidAgentLogger).steerRun(runId, trimmed)
                            }
                        }
                        finish()
                    },
                )
            }
        }
    }

    companion object {
        const val EXTRA_RUN_ID = "io.github.mangi.eta.extra.SUPPLEMENT_RUN_ID"
        const val EXTRA_TITLE = "io.github.mangi.eta.extra.SUPPLEMENT_TITLE"

        fun intent(context: android.content.Context, runId: String, title: String): Intent =
            Intent(context, SupplementActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra(EXTRA_RUN_ID, runId)
                .putExtra(EXTRA_TITLE, title)
    }
}

@androidx.compose.runtime.Composable
private fun SupplementSheet(
    title: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 20.dp)
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MiuixTheme.colorScheme.surfaceContainer)
                .padding(16.dp),
        ) {
            Text(
                text = "插入到「" + title + "」",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(10.dp))
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = MiuixTheme.textStyles.body1.copy(color = MiuixTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focus)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                decorationBox = { inner ->
                    Box {
                        if (text.isEmpty()) {
                            Text(
                                text = "补充一句，插进当前运行…",
                                style = MiuixTheme.textStyles.body1,
                                color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            )
                        }
                        inner()
                    }
                },
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompactChip(text = "取消", onClick = onDismiss)
                Spacer(Modifier.width(8.dp))
                CompactChip(
                    text = "插入",
                    enabled = text.isNotBlank(),
                    onClick = { onSubmit(text) },
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun CompactChip(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.92f else 0.35f),
        )
    }
}
