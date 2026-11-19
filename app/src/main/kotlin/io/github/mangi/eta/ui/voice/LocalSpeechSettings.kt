package io.github.mangi.eta.ui.voice

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.agent.voice.local.LocalSpeechInstallation
import io.github.mangi.eta.agent.voice.local.LocalSpeechModelDescriptor
import io.github.mangi.eta.agent.voice.local.LocalSpeechModelStore
import io.github.mangi.eta.agent.voice.local.LocalSpeechProgress
import io.github.mangi.eta.agent.voice.local.LocalSpeechStage
import io.github.mangi.eta.data.model.AppearanceUiStyle
import io.github.mangi.eta.data.model.AsrProvider
import io.github.mangi.eta.data.repository.SpeechSettingsRepository
import io.github.mangi.eta.ui.app.LocalUiStyle
import io.github.mangi.eta.ui.components.MiuixDialogActions
import io.github.mangi.eta.ui.components.coui.CouiConfirmDialog
import io.github.mangi.eta.ui.components.coui.CouiButton
import io.github.mangi.eta.ui.components.coui.CouiDropdownPreference
import io.github.mangi.eta.ui.components.coui.CouiPreference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.window.WindowDialog

/** Catalog/measurements and complete installation inspection stay off the UI thread. */
@Composable
internal fun LocalSpeechConnectionSection(store: SpeechSettingsStore) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    var models by remember(context) { mutableStateOf<List<LocalSpeechModelDescriptor>>(emptyList()) }
    var selected by remember(context) { mutableStateOf<String?>(null) }
    var target by rememberSaveable { mutableStateOf<String?>(null) }
    // SAF may recreate the activity. Never infer the import destination from the current dropdown.
    var importTarget by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteTarget by rememberSaveable { mutableStateOf<String?>(null) }
    var deleting by remember(context) { mutableStateOf(false) }
    var installations by remember(context) { mutableStateOf<Map<String, LocalSpeechInstallation>>(emptyMap()) }
    var progress by remember(context) { mutableStateOf<LocalSpeechProgress?>(null) }
    var message by remember(context) { mutableStateOf<String?>(null) }
    var operationDetail by remember(target) { mutableStateOf<String?>(null) }
    var busy by remember(context) { mutableStateOf(false) }
    var cancelling by remember(context) { mutableStateOf(false) }
    var job by remember(context) { mutableStateOf<Job?>(null) }
    var generation by remember(context) { mutableStateOf(0) }
    var measurements by remember(context) { mutableStateOf<LocalSpeechMeasurements?>(null) }
    var metadataFailure by remember(context) { mutableStateOf(false) }
    var reload by remember { mutableStateOf(0) }
    LaunchedEffect(context, reload) {
        metadataFailure = false
        try {
            val catalog = withContext(Dispatchers.IO) { LocalSpeechModelStore.availableModels(context) }
            val saved = withContext(Dispatchers.IO) { LocalSpeechModelStore.selectedId(context) }
            models = catalog
            selected = saved
            target = target?.takeIf { id -> catalog.any { it.id == id } }
                ?: saved.takeIf { id -> catalog.any { it.id == id } }
                ?: catalog.first { it.recommended }.id
            measurements = withContext(Dispatchers.IO) { readLocalSpeechMeasurements(context) }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { metadataFailure = true }
    }
    fun operate(id: String, forceFullCheck: Boolean = false, refreshAfter: Boolean = true,
        background: Boolean = false, action: suspend ((LocalSpeechProgress) -> Unit) -> Unit) {
        if (busy || store.saving || !store.loaded || target != id) return
        val token = ++generation
        busy = true
        cancelling = false
        progress = null
        message = null
        operationDetail = null
        // 下载用进程级 scope：离开设置页/切后台不再取消，未下完的部分靠 stage 目录续传。
        job = (if (background) LocalSpeechDownloads.scope else scope).launch {
            try {
                coroutineScope {
                    // One bounded consumer; at most one visible update per 100 ms, plus completion.
                    val updates = Channel<LocalSpeechProgress>(Channel.CONFLATED)
                    val reporter = launch(Dispatchers.Main.immediate) {
                        for (first in updates) {
                            var latest = first
                            if (latest.stage != LocalSpeechStage.COMPLETE) delay(100)
                            while (true) latest = updates.tryReceive().getOrNull() ?: break
                            if (generation == token && busy && target == id) progress = latest
                        }
                    }
                    try { action { updates.trySend(it) } }
                    finally { updates.close(); reporter.join() }
                }
                if (refreshAfter) {
                    val inspected = withContext(Dispatchers.IO) { LocalSpeechModelStore.inspect(context, id, forceFull = forceFullCheck) }
                    if (generation == token && target == id) installations = installations + (id to inspected)
                }
            } catch (cancelled: CancellationException) {
                if (generation == token) message = "操作已取消，可重新检测安装状态。"
                throw cancelled
            } catch (error: Exception) {
                if (generation == token) {
                    operationDetail = error.message?.take(240)
                    installations = installations - id
                    message = "操作未完成，请检查文件、网络或存储后重新检测。"
                }
            } finally {
                if (generation == token) {
                    busy = false
                    cancelling = false
                    progress = null
                    job = null
                }
            }
        }
    }
    fun deleteInstalledModel(model: LocalSpeechModelDescriptor) {
        val id = model.id
        if (busy || store.saving || !store.loaded || importTarget != null || target != id) return
        val token = ++generation
        busy = true
        deleting = true
        progress = null
        operationDetail = null
        message = "正在等待删除确认，最长约 90 秒；此阶段不可取消。"
        // A confirmed destructive request must outlive UI cancellation/disposal until its outcome
        // and full inspection are known. Never route this through cancellable operate().
        job = scope.launch(context = NonCancellable) {
            try {
                val result = LocalSpeechModelStore.deleteModel(context, id)
                check(result.modelId == id && !result.installed) { "删除返回状态未确认" }
                if (generation == token) {
                    operationDetail = result.detail
                    message = "已删除「${model.displayName}」的已安装权重。" +
                        if (selected == id) "当前选择保留；重新导入或手动选择其他已安装模型前不可录音。"
                        else "原始文件和引擎保留，可随时重新导入或下载。"
                }
            } catch (error: Exception) {
                if (generation == token) {
                    operationDetail = error.message?.take(240)
                    message = "删除未完成或结果未确认，请重新检测；不会自动切换模型。"
                }
            } finally {
                try {
                    val inspected = withContext(NonCancellable + Dispatchers.IO) {
                        LocalSpeechModelStore.inspect(context, id, forceFull = true)
                    }
                    if (generation == token) installations = installations + (id to inspected)
                    // installed=false is ONLY an inspection result, never deletion-success evidence.
                } catch (error: Exception) {
                    if (generation == token) {
                        installations = installations - id
                        operationDetail = listOfNotNull(operationDetail, error.message?.take(240)).joinToString("；")
                        message = "$message 安装状态刷新失败，请重新检测。"
                    }
                } finally {
                    if (generation == token) {
                        busy = false
                        deleting = false
                        progress = null
                        job = null
                    }
                }
            }
        }
    }
    LaunchedEffect(target, store.loaded) {
        val id = target ?: return@LaunchedEffect
        if (store.loaded) operate(id) { }
    }
    // Inspect a different saved selection independently, without claiming that selection means installed.
    LaunchedEffect(selected, target) {
        val id = selected ?: return@LaunchedEffect
        if (id != target && installations[id] == null && models.any { it.id == id }) {
            val inspected = withContext(Dispatchers.IO) { LocalSpeechModelStore.inspect(context, id) }
            installations = installations + (id to inspected)
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val id = importTarget
        importTarget = null
        if (uri != null && id != null && id == target) operate(id) { report ->
            LocalSpeechModelStore.importModel(context, id, opener = {
                context.contentResolver.openInputStream(uri) ?: error("无法读取所选文件")
            }, onProgress = report)
            message = "导入校验完成，尚未切换识别模型。"
        }
    }
    val model = models.firstOrNull { it.id == target }
    if (model == null) {
        SpeechGroupCard(title = "本机离线") {
            SpeechNote(if (metadataFailure) "模型目录读取失败，请重试。" else "正在读取模型目录…")
            if (metadataFailure) LocalModelAction("重试", true) { reload++ }
        }
        return
    }
    val enabled = !busy && !store.saving && store.loaded && importTarget == null
    val status = installations[model.id]
    SpeechGroupCard(title = "正在使用") {
        val current = models.firstOrNull { it.id == selected }
        val summary = "${current?.selectionLabel() ?: "未知已保存模型"} · 已选 · ${localInstallationLabel(installations[selected])}"
        if (LocalUiStyle.current == AppearanceUiStyle.COUI) {
            CouiPreference(title = "下次录音", summary = summary, showChevron = false)
        } else {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                LocalSpeechText(summary)
            }
        }
    }
    SpeechGroupCard(title = "模型概览") {
        val largeText = LocalDensity.current.fontScale > 1.25f
        val labels = models.map { if (largeText) it.selectionLabel().replace("SenseVoice", "SV") else it.selectionLabel() }
        val choose: (Int) -> Unit = { index ->
            if (enabled) models.getOrNull(index)?.let {
                target = it.id
                message = null
                progress = null
            }
        }
        if (LocalUiStyle.current == AppearanceUiStyle.COUI) {
            CouiDropdownPreference(title = "模型", items = labels, enabled = enabled,
                selectedIndex = models.indexOfFirst { it.id == target }, onSelectedIndexChange = choose)
        } else {
            OverlayDropdownPreference(title = "模型", items = labels, enabled = enabled,
                selectedIndex = models.indexOfFirst { it.id == target }, onSelectedIndexChange = choose)
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LocalSpeechText("${model.displayName} · ${model.recommendationLabel()}", heading = true)
            LocalSpeechText(model.overview(), secondary = true)
            LocalSpeechMetrics(model, measurements?.models?.get(model.id))
            LocalSpeechStatus(model, status, busy, message)
            if (busy && !deleting) LocalSpeechProgressBar(progress)
        }
        LocalModelAction(if (selected == target) "确认使用并保存" else "使用此模型并保存",
            enabled && model.available && status?.installed == true && status.modelId == target) {
            val id = model.id
            operate(id, refreshAfter = false) {
                check(withContext(Dispatchers.IO) { LocalSpeechModelStore.inspect(context, id).installed }) { "模型未通过校验" }
                val config = store.settings.copy(asr = AsrProvider.LOCAL)
                SpeechSettingsRepository.save(config)
                check(SpeechSettingsRepository.settings().asr == AsrProvider.LOCAL) { "本机离线设置保存未确认" }
                withContext(Dispatchers.IO) {
                    LocalSpeechModelStore.selectModel(context, id)
                    check(LocalSpeechModelStore.selectedId(context) == id) { "模型选择保存未确认" }
                }
                selected = id
                store.edit(config)
                message = "已保存，下次录音生效。"
            }
        }
        LocalModelAction("下载模型（联网）", enabled && model.available) {
            operate(model.id, background = true) { report ->
                LocalSpeechModelStore.download(context.applicationContext, model.id, onProgress = report)
            }
        }
        LocalModelAction("导入 GGUF / ZIP", enabled && model.available) {
            importTarget = model.id
            picker.launch(arrayOf("application/octet-stream", "application/zip", "application/x-zip-compressed"))
        }
        // Visible for all eight entries, including absent, corrupt and unavailable payloads.
        LocalModelAction("删除已安装权重", enabled) { deleteTarget = model.id }
        if (deleting) LocalModelAction("正在确认删除结果…", false) { }
        if (busy && !deleting) LocalModelAction(if (cancelling) "正在取消…" else "取消操作", !cancelling) {
            cancelling = true
            job?.cancel() // Keep the handle and busy state until the operation's finally block.
        }
    }
    LocalSpeechModelInfo(model, measurements, status, operationDetail, enabled, onInspect = { operate(model.id, forceFullCheck = true) { } })
    models.firstOrNull { it.id == deleteTarget }?.let { toDelete ->
        LocalSpeechDeleteConfirmation(
            model = toDelete,
            selected = selected == toDelete.id,
            enabled = enabled && target == toDelete.id,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                if (enabled && target == toDelete.id) {
                    deleteTarget = null
                    deleteInstalledModel(toDelete)
                }
            },
        )
    }
}

internal fun LocalSpeechModelDescriptor.recommendationLabel(): String = if (recommended) "推荐" else "可选"
internal fun LocalSpeechModelDescriptor.selectionLabel(): String = "${shortLabel()} · ${recommendationLabel()}"

internal fun localSpeechDeleteConfirmationText(model: LocalSpeechModelDescriptor, selected: Boolean): String =
    "删除「${model.displayName}」？\n\n" +
        "只删除此模型在应用内已安装的权重及关联安装元数据，不删除识别引擎，也不删除您保留的原 ZIP / GGUF；之后可随时重新导入或下载。\n\n" +
        (if (selected) "此模型当前已选中。删除后保留该选择，重新安装或手动选择其他已安装模型前不可录音。\n\n"
        else "不会改变当前已保存的模型选择。\n\n") +
        "损坏或校验失败的权重也可删除。加载、录音或识别中会拒绝删除；确认后最长约 90 秒等待结果，期间不可取消。"

@Composable
private fun LocalSpeechDeleteConfirmation(model: LocalSpeechModelDescriptor, selected: Boolean,
    enabled: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val body = localSpeechDeleteConfirmationText(model, selected)
    val bodyHeight = (LocalConfiguration.current.screenHeightDp * 0.35f).dp.coerceAtMost(280.dp)
    when (LocalUiStyle.current) {
        AppearanceUiStyle.COUI -> CouiConfirmDialog(
            visible = true, title = "删除模型权重", message = null,
            confirmText = "删除", confirmEnabled = enabled, destructive = true,
            onConfirm = onConfirm, onDismissRequest = onDismiss,
        ) {
            // Keep the library's action area separate from the bounded, scrollable text.
            LocalSpeechText(body, Modifier.fillMaxWidth().heightIn(max = bodyHeight)
                .verticalScroll(rememberScrollState()), secondary = true)
        }
        AppearanceUiStyle.MIUIX -> WindowDialog(
            show = true, title = "删除模型权重", onDismissRequest = onDismiss,
        ) {
            Column(Modifier.fillMaxWidth().heightIn(max = bodyHeight + 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                LocalSpeechText(body, Modifier.fillMaxWidth().weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()), secondary = true)
                MiuixDialogActions(confirmText = "删除", destructive = true,
                    confirmEnabled = enabled, onCancel = onDismiss, onConfirm = onConfirm)
            }
        }
    }
}

internal fun LocalSpeechModelDescriptor.shortLabel(): String = when (id) {
    "qwen3_asr_06b_q4_k_m" -> "Qwen Q4_K_M"
    "qwen3_asr_06b_q5_k_m" -> "Qwen Q5_K_M"
    "vosk_small_cn_022" -> "Vosk 中文 Small"
    "vosk_cn_022" -> "Vosk 中文大模型"
    "sensevoice_small_f16" -> "SenseVoice F16"
    "sensevoice_small_q8_0" -> "SenseVoice Q8_0"
    "sensevoice_small_int8" -> "SenseVoice INT8"
    "sensevoice_small_q4_k_m" -> "SV Q4_K_M"
    else -> displayName
}

@Composable
internal fun LocalModelAction(text: String, enabled: Boolean, onClick: () -> Unit) {
    val modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
    if (LocalUiStyle.current == AppearanceUiStyle.COUI) {
        CouiButton(text = text, enabled = enabled, onClick = onClick, modifier = modifier)
    } else TextButton(text = text, enabled = enabled, onClick = onClick, modifier = modifier)
}
