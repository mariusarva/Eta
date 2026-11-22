package io.github.mangi.eta.ui.voice

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.mangi.eta.ui.components.coui.CouiPageDefaults
import io.github.mangi.eta.ui.components.coui.CouiTextKind
import io.github.mangi.eta.ui.components.coui.couiTextStyle
import io.github.mangi.eta.agent.voice.local.LocalSpeechInstallation
import io.github.mangi.eta.agent.voice.local.LocalSpeechModelDescriptor
import io.github.mangi.eta.agent.voice.local.LocalSpeechProgress
import io.github.mangi.eta.agent.voice.local.LocalSpeechStage
import io.github.mangi.eta.data.model.AppearanceUiStyle
import io.github.mangi.eta.ui.app.LocalUiStyle
import io.github.mangi.eta.ui.components.StatusError
import io.github.mangi.eta.ui.components.StatusSuccess
import io.github.mangi.eta.ui.components.StatusWarning
import io.github.mangi.eta.ui.components.coui.CouiIcon
import java.util.Locale
import org.json.JSONObject
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal data class LocalSpeechMeasuredModel(val peakRssKiB: Long, val factoryMs: Long, val decodeMs: Long)
internal data class LocalSpeechMeasurements(
    val models: Map<String, LocalSpeechMeasuredModel>,
    val fixture: String,
    val audioSeconds: Double,
    val diskCacheCold: Boolean,
    val serviceIpcTested: Boolean,
)

/** Called on IO; parse the measurement object, not catalog deviceValidation history. */
internal fun readLocalSpeechMeasurements(context: Context): LocalSpeechMeasurements? = runCatching {
    parseLocalSpeechMeasurements(context.assets.open("local_speech/measurements.json").bufferedReader().use { it.readText() })
}.getOrNull()

internal fun parseLocalSpeechMeasurements(json: String): LocalSpeechMeasurements {
    val root = JSONObject(json)
    require(root.getInt("schemaVersion") == 1)
    val array = root.getJSONArray("models")
    val models = buildMap {
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            put(item.getString("id"), LocalSpeechMeasuredModel(item.getLong("peakRssKiB"),
                item.getLong("factoryMs"), item.getLong("decodeMs")))
        }
    }
    return LocalSpeechMeasurements(models, root.getString("fixture"), root.getDouble("audioSeconds"),
        root.getBoolean("diskCacheCold"), root.getBoolean("serviceIpcTested"))
}

internal fun LocalSpeechModelDescriptor.overview(): String = when (id) {
    "vosk_small_cn_022" -> "Vosk 中文 Small 0.22，面向移动设备的流式离线模型；本机耗时与内存尚未实测，不改变默认模型。"
    "vosk_cn_022" -> "Vosk 中文 0.22 大模型，官方定位服务器；高运行内存需求，非低内存或默认推荐，手机可能因内存不足加载失败。"
    "sensevoice_small_f16" -> "SenseVoice 半精度权重，多语言说完识别；文件体积较大。"
    "sensevoice_small_q8_0" -> "SenseVoice Q8_0，当前唯一推荐；GGUF 8 位量化、多语言说完识别。推荐不代表速度、准确率或内存排名。"
    "sensevoice_small_q4_k_m" -> "SenseVoice 混合量化，多语言说完识别；下载体积更小。"
    "sensevoice_small_int8" -> "SenseVoice INT8 ONNX，多语言说完识别；可选双文件格式，含模型与 tokens。"
    else -> "Qwen3 0.6B 混合量化，多语言说完识别；运行开销较大，非实时逐字。"
}

internal fun localInstallationLabel(status: LocalSpeechInstallation?): String = when {
    status == null -> "待检测"
    status.installed -> "就绪"
    status.detail.startsWith("尚未安装") -> "未安装"
    else -> "损坏 / 缺失"
}

internal fun friendlyModelSize(bytes: Long): String = String.format(Locale.ROOT, "%.0fMB", bytes / 1_000_000.0)

@Composable
internal fun LocalSpeechText(text: String, modifier: Modifier = Modifier, secondary: Boolean = false,
    heading: Boolean = false, tint: Color = Color.Unspecified) {
    val coui = LocalUiStyle.current == AppearanceUiStyle.COUI
    val color = if (tint != Color.Unspecified) tint else if (coui) {
        val colors = CouiPageDefaults.themeColors()
        if (secondary) colors.summary else colors.title
    } else if (secondary) MiuixTheme.colorScheme.onSurfaceVariantSummary else MiuixTheme.colorScheme.onSurface
    val baseStyle = if (coui) couiTextStyle(if (secondary) CouiTextKind.ItemSummary else CouiTextKind.ItemTitle)
        else if (secondary) MiuixTheme.textStyles.footnote2 else MiuixTheme.textStyles.body1
    Text(text = text, modifier = modifier, color = color,
        style = baseStyle.copy(
            fontSize = if (secondary) 13.sp else 16.sp,
            lineHeight = if (secondary) 20.sp else 24.sp,
            fontWeight = if (heading) FontWeight.SemiBold else FontWeight.Normal))
}

@Composable
internal fun LocalSpeechMetrics(model: LocalSpeechModelDescriptor, measured: LocalSpeechMeasuredModel?) {
    val items = listOf(
        "下载大小" to friendlyModelSize(model.sizeBytes),
        "RAM 参考（含测试）" to (measured?.let { String.format(Locale.ROOT, "约%.0fMiB", it.peakRssKiB / 1024.0) } ?: "暂无数据"),
        "引擎 / 量化" to "${when (model.engine.name) { "GGUF" -> "GGUF"; "VOSK" -> "Vosk / Kaldi"; else -> "ONNX" }} ${model.quantization}",
    )
    val scale = LocalDensity.current.fontScale
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // Use available width AND the user's text scale; never override Density/fontScale.
        if (maxWidth / scale < 300.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { (label, value) -> LocalSpeechDataRow(label, value) }
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEach { (label, value) ->
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LocalSpeechText(label, secondary = true)
                        LocalSpeechText(value, heading = true)
                    }
                }
            }
        }
    }
}

@Composable
internal fun LocalSpeechStatus(model: LocalSpeechModelDescriptor, status: LocalSpeechInstallation?, busy: Boolean, message: String?) {
    val label = if (busy) "处理中" else localInstallationLabel(status)
    val tint = when {
        busy || status == null -> if (LocalUiStyle.current == AppearanceUiStyle.COUI) CouiPageDefaults.themeColors().summary
            else MiuixTheme.colorScheme.onSurfaceVariantSummary
        status.installed -> StatusSuccess
        label == "未安装" -> StatusWarning
        else -> StatusError
    }
    val detail = when {
        busy -> "校验、安装或删除期间请稍候。"
        !model.available -> model.availabilityReason ?: "此档暂不可用。"
        status == null -> "检测完成后才可使用。"
        status.installed -> "已通过完整校验，可离线使用。"
        label == "未安装" -> "请手动下载或导入此模型。"
        else -> "文件缺失或校验失败，请展开参数重新检测。"
    }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(tint.copy(alpha = 0.09f))
        .padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LocalSpeechText(label, heading = true, tint = tint)
        LocalSpeechText(detail, secondary = true)
        message?.let { LocalSpeechText(it, secondary = true) }
    }
}

@Composable
internal fun LocalSpeechProgressBar(progress: LocalSpeechProgress?) {
    val stage = when (progress?.stage) {
        LocalSpeechStage.DOWNLOADING -> "下载"
        LocalSpeechStage.IMPORTING -> "导入"
        LocalSpeechStage.VERIFYING -> "SHA 校验"
        LocalSpeechStage.INSTALLING -> "安装"
        LocalSpeechStage.COMPLETE -> "完成"
        null -> "完整校验"
    }
    val fraction = progress?.takeIf { it.total > 0 }?.let {
        (it.bytes.toDouble() / it.total.toDouble()).coerceIn(0.0, 1.0).toFloat()
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LocalSpeechText(if (fraction == null) "$stage · 等待进度…"
            else String.format(Locale.ROOT, "%s · %.1f%%", stage, fraction * 100), secondary = true)
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().semantics {
            progressBarRangeInfo = fraction?.let { ProgressBarRangeInfo(it, 0f..1f) }
                ?: ProgressBarRangeInfo.Indeterminate
        }, progress = fraction)
    }
}

@Composable
private fun LocalSpeechDataRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        LocalSpeechText(label, Modifier.weight(0.42f), secondary = true)
        LocalSpeechText(value, Modifier.weight(0.58f), secondary = true)
    }
}

@Composable
private fun LocalSpeechFold(modelId: String, title: String, content: @Composable ColumnScope.() -> Unit) {
    var expanded by rememberSaveable(modelId, title) { mutableStateOf(false) }
    SpeechGroupCard {
        Row(Modifier.fillMaxWidth().semantics { stateDescription = if (expanded) "已展开" else "已收起" }
            .clickable(role = Role.Button, onClickLabel = if (expanded) "收起$title" else "展开$title") { expanded = !expanded }
            .padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LocalSpeechText(title, Modifier.weight(1f))
            val iconModifier = Modifier.size(20.dp).rotate(if (expanded) 180f else 0f)
            if (LocalUiStyle.current == AppearanceUiStyle.COUI) {
                CouiIcon(Icons.Rounded.ExpandMore, contentDescription = null, modifier = iconModifier)
            } else Icon(Icons.Rounded.ExpandMore, contentDescription = null, modifier = iconModifier)
        }
        if (expanded) Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
internal fun LocalSpeechModelInfo(model: LocalSpeechModelDescriptor, measurements: LocalSpeechMeasurements?,
    installation: LocalSpeechInstallation?, operationDetail: String?, enabled: Boolean, onInspect: () -> Unit) {
    LocalSpeechFold(model.id, "更多参数与测试数据") {
        LocalSpeechText("${model.displayName} · ${model.recommendationLabel()}", secondary = true)
        installation?.let { LocalSpeechText(it.detail, secondary = true) }
        operationDetail?.let { LocalSpeechText(it, secondary = true) }
        LocalSpeechDataRow("精确文件合计", "${model.sizeBytes} bytes${if (model.archive != null) "（官方 ZIP）" else ""}")
        if (model.archive != null) {
            LocalSpeechDataRow("解压后文件", "${friendlyModelSize(model.installedSizeBytes)} · ${model.installedSizeBytes} bytes")
            LocalSpeechDataRow("安装空闲空间", "至少 ${friendlyModelSize(model.installationSpaceBytes + 16L * 1024 * 1024)}，另留文件系统余量")
            LocalSpeechText("下载压缩包、解压存储空间与运行 RAM 是三项不同指标。安装会暂存原 ZIP，完成后仅清理本次临时包；不会删除已有模型或用户文件。", secondary = true)
            LocalSpeechText(if (model.id == "vosk_cn_022")
                "官方大模型面向服务器，可能需要数 GB 运行内存；本机需求与速度未实测，不是低内存或首选方案。内存不足或加载失败仅本地提示，请改选已安装的小模型；绝不回退云端。"
                else "官方小模型面向移动设备，但下载大小不代表 RAM；本机运行内存和速度未实测。内存不足或加载失败仅本地提示，不回退云端。", secondary = true)
        }
        LocalSpeechDataRow("架构", model.architecture)
        LocalSpeechDataRow("格式 / 量化", "${model.engine.name} / ${model.quantization}")
        if (model.quantization.endsWith("_K_M")) LocalSpeechText("M 表示混合量化，并非纯 Q4_K / Q5_K。", secondary = true)
        val measured = measurements?.models?.get(model.id)
        if (measurements != null && measured != null) {
            LocalSpeechDataRow("加载", "${measured.factoryMs} ms")
            LocalSpeechDataRow("识别", "${measured.decodeMs} ms")
            LocalSpeechDataRow("测试音频", "${measurements.fixture} · ${measurements.audioSeconds} s")
            LocalSpeechDataRow("文件缓存", if (measurements.diskCacheCold) "冷态" else "非冷态")
            LocalSpeechDataRow("服务 IPC", if (measurements.serviceIpcTested) "已测试" else "未测试")
            LocalSpeechText("RAM 为本机单次测试进程峰值（VmHWM）；测试不代表准确率排名、常驻内存或内存需求保证。", secondary = true)
        } else LocalSpeechText("暂无可读取的实测数据，不影响模型安装与使用。", secondary = true)
        LocalSpeechText("缓存一个模型，空闲 120 秒释放；再次使用更快，但空闲期间暂占 RAM。", secondary = true)
        LocalModelAction("重新检测（完整 SHA）", enabled, onInspect)
    }
    LocalSpeechFold(model.id, "导入说明") {
        LocalSpeechText(if (model.archive != null)
            "仅导入所选 Vosk 官方原始 ZIP（不可重打包或只选解压目录）；压缩包及每个解压文件均严格校验大小与 SHA-256，安全解压到 vosk/。安装后在同一进程复用完整校验结果；新进程首次加载完整校验，文件指纹不变时复用验证；重新检测始终执行完整 SHA。"
            else "GGUF 档导入对应 GGUF 或 ZIP；INT8 档导入含 ONNX 和 tokens 的 ZIP。大小与 SHA 均严格校验，不支持原始 safetensors。", secondary = true)
        model.archive?.let { LocalSpeechDataRow("官方原 ZIP", it.name) }
        model.files.forEach { LocalSpeechDataRow(it.role, it.name) }
        LocalSpeechText("推荐 SenseVoice Q8_0，其余七款均为可选；仅未保存或已保存选择无效时缺省使用 Q8_0。已有合法选择保持不变，不自动下载、迁移或删除已有文件。", secondary = true)
        LocalSpeechText("单次录音不超过 30 秒（Qwen 每段不超过 10 秒）。仅本机识别，无云端回退或网络校对，不自动下载；选择仅在下次录音生效。", secondary = true)
    }
    LocalSpeechFold(model.id, "许可与来源") {
        LocalSpeechDataRow("许可", model.licenseName)
        LocalSpeechText("许可随模型安装保存为 LICENSE.txt；应用内原文：${model.licenseAsset}", secondary = true)
        model.licenseSource?.let { LocalSpeechDataRow("官方许可声明", it) }
        (model.archive?.let { listOf(it) } ?: model.files).forEach { file ->
            val uri = java.net.URI(file.pinnedUrl)
            LocalSpeechDataRow("来源", "${uri.host}/${uri.path.trim('/').substringBefore("/resolve/")}")
            LocalSpeechDataRow("固定修订", file.revision)
            LocalSpeechDataRow("SHA-256", file.sha256)
        }
    }
}
