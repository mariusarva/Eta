package io.github.mangi.eta.ui.components

import android.app.ActivityManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.glass.GlassDialog
import top.yukonga.miuix.kmp.glass.GlassDialogButton
import top.yukonga.miuix.kmp.glass.GlassDialogButtonRole
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 输入框右上角的能力胶囊：MCP 服务数 / 技能数 / 内存占用。
 *
 * 三个数字都是只读快照：
 * - MCP 与技能数来自仓库与技能索引（启用的数量 / 总数）；
 * - 内存走 `ActivityManager.MemoryInfo`，不需要 Root，也不改系统设置。
 *
 * 点一下弹出明细，避免把长文案塞进输入框上方那一行。
 */
@Composable
internal fun CapabilityCapsule(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var usage by remember { mutableStateOf(readMemoryUsage(context)) }
    var showDetail by remember { mutableStateOf(false) }
    var counts by remember { mutableStateOf(CapabilityCounts()) }
    LaunchedEffect(context) {
        while (true) {
            usage = readMemoryUsage(context)
            delay(CAPSULE_MEMORY_REFRESH_MILLIS)
        }
    }
    // 数量变化不频繁，但用户随时可能在设置里开关：定时重读一次，失败就保留旧值。
    LaunchedEffect(context) {
        while (true) {
            counts = runCatching { readCapabilityCounts(context) }.getOrDefault(counts)
            delay(CAPSULE_COUNTS_REFRESH_MILLIS)
        }
    }
    // 打开明细时再强制读一次，避免刚改完配置点进来还是旧数字。
    LaunchedEffect(showDetail) {
        if (showDetail) {
            counts = runCatching { readCapabilityCounts(context) }.getOrDefault(counts)
        }
    }
    val mcpEnabled = counts.mcpEnabled
    val mcpTotal = counts.mcpTotal
    val skillEnabled = counts.skillEnabled
    val skillTotal = counts.skillTotal
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { showDetail = true }
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CapsuleNumber(text = "MCP " + mcpEnabled + "/" + mcpTotal)
            CapsuleNumber(text = "技能 " + skillEnabled + "/" + skillTotal)
            CapsuleNumber(text = "内存 " + usage.usedPercent + "%", warning = usage.usedPercent >= 85)
        }
        GlassDialog(
            show = showDetail,
            title = "能力与占用",
            backdrop = currentGlassOverlayBackdrop(),
            onDismissRequest = { showDetail = false },
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                DetailRow("MCP 服务", mcpEnabled.toString() + " 个启用 / 共 " + mcpTotal + " 个")
                DetailRow("技能", skillEnabled.toString() + " 个启用 / 共 " + skillTotal + " 个")
                Spacer(Modifier.height(8.dp))
                DetailRow("内存已用", usage.usedPercent.toString() + "%")
                DetailRow("已用", formatMegabytes(usage.usedBytes))
                DetailRow("可用", formatMegabytes(usage.availableBytes))
                DetailRow("物理总量", formatGigabytes(usage.totalBytes))
                if (usage.swapTotalBytes > 0L) {
                    DetailRow(
                        "内存拓展",
                        formatGigabytes(usage.swapUsedBytes) + " / " + formatGigabytes(usage.swapTotalBytes),
                    )
                }
                Spacer(Modifier.height(12.dp))
                GlassDialogButton(
                    onClick = { showDetail = false },
                    role = GlassDialogButtonRole.Normal,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "关闭", style = MiuixTheme.textStyles.button, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun CapsuleNumber(text: String, warning: Boolean = false) {
    Text(
        text = text,
        style = MiuixTheme.textStyles.footnote2,
        color = if (warning) {
            MiuixTheme.colorScheme.error
        } else {
            MiuixTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        },
        maxLines = 1,
        softWrap = false,
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.width(96.dp),
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurface,
        )
    }
}

internal data class CapsuleMemoryUsage(
    val usedPercent: Int,
    val usedBytes: Long,
    val availableBytes: Long,
    val totalBytes: Long,
    val swapTotalBytes: Long = 0L,
    val swapUsedBytes: Long = 0L,
)

internal fun readMemoryUsage(context: Context): CapsuleMemoryUsage {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val info = ActivityManager.MemoryInfo()
    runCatching { manager?.getMemoryInfo(info) }
    val total = info.totalMem.takeIf { it > 0 } ?: 1L
    val available = info.availMem.coerceAtLeast(0L)
    val used = (total - available).coerceAtLeast(0L)
    val percent = ((used.toDouble() / total.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
    val (swapTotal, swapUsed) = readSwapBytes()
    return CapsuleMemoryUsage(percent, used, available, total, swapTotal, swapUsed)
}

/** 内存拓展（ZRAM）只作参考展示，不参与物理内存百分比。 */
private fun readSwapBytes(): Pair<Long, Long> = runCatching {
    var total = 0L
    var free = 0L
    java.io.File("/proc/meminfo").useLines { lines ->
        lines.forEach { line ->
            when {
                line.startsWith("SwapTotal:") -> total = line.filter(Char::isDigit).toLongOrNull() ?: 0L
                line.startsWith("SwapFree:") -> free = line.filter(Char::isDigit).toLongOrNull() ?: 0L
            }
        }
    }
    total * 1024L to (total - free).coerceAtLeast(0L) * 1024L
}.getOrDefault(0L to 0L)

internal fun formatGigabytes(bytes: Long): String =
    String.format(Locale.US, "%.1f GB", bytes.toDouble() / (1024.0 * 1024.0 * 1024.0))

internal fun formatMegabytes(bytes: Long): String =
    String.format(Locale.US, "%.0f MB", bytes.toDouble() / (1024.0 * 1024.0))

private const val CAPSULE_MEMORY_REFRESH_MILLIS = 5000L
private const val CAPSULE_COUNTS_REFRESH_MILLIS = 15000L

/** 能力胶囊上的三个计数：MCP 与技能的启用数 / 总数。 */
internal data class CapabilityCounts(
    val mcpEnabled: Int = 0,
    val mcpTotal: Int = 0,
    val skillEnabled: Int = 0,
    val skillTotal: Int = 0,
)

private suspend fun readCapabilityCounts(context: Context): CapabilityCounts {
    val servers = runCatching {
        io.github.mangi.eta.data.repository.McpServerRepository.init(context)
        io.github.mangi.eta.data.repository.McpServerRepository.servers()
    }.getOrDefault(emptyList())
    // 技能必须读「管理视图」：listInstalledSkills 已经过滤掉未启用的，拿它统计会永远 N/N。
    val skills = runCatching {
        io.github.mangi.eta.agent.skill.SkillRuntime.createIndexService(context)
            .listSkillsForManagement()
            .filter { it.installed }
    }.getOrDefault(emptyList())
    return CapabilityCounts(
        mcpEnabled = servers.count { it.enabled },
        mcpTotal = servers.size,
        skillEnabled = skills.count { it.enabled },
        skillTotal = skills.size,
    )
}
