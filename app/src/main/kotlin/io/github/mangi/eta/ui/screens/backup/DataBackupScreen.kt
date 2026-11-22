package io.github.mangi.eta.ui.screens.backup

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.R
import io.github.mangi.eta.core.AndroidAgentLogger
import io.github.mangi.eta.core.safeLogType
import io.github.mangi.eta.core.safeStackTrace
import io.github.mangi.eta.data.model.AppearanceUiStyle
import io.github.mangi.eta.data.repository.EtaBackupSummary
import io.github.mangi.eta.ui.app.LocalUiStyle
import io.github.mangi.eta.ui.components.MiuixDialogActions
import io.github.mangi.eta.ui.components.MiuixScaffoldPage
import io.github.mangi.eta.ui.components.coui.CouiConfirmDialog
import io.github.mangi.eta.ui.components.coui.CouiPageBottomSpacer
import io.github.mangi.eta.ui.components.coui.CouiPreference
import io.github.mangi.eta.ui.components.coui.CouiPreferenceGroup
import io.github.mangi.eta.ui.components.coui.CouiScaffoldPage
import io.github.mangi.eta.ui.components.currentGlassOverlayBackdrop
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.glass.GlassDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import io.github.mangi.eta.data.model.AutoBackupSettings
import io.github.mangi.eta.agent.automation.AutoBackupAlarm
import io.github.mangi.eta.data.repository.AutoBackupRunner
import io.github.mangi.eta.data.repository.AutoBackupResult
import io.github.mangi.eta.data.repository.AutoBackupFile
import io.github.mangi.eta.data.datastore.SettingsDataStore
import io.github.mangi.eta.ui.components.coui.CouiSwitchPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.basic.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import io.github.mangi.eta.ui.app.currentGlassOverlayBackdrop
import android.content.Intent
import androidx.compose.runtime.LaunchedEffect

@Composable
internal fun DataBackupScreen(
    context: Context,
    onBack: () -> Unit,
    onExport: suspend (OutputStream) -> EtaBackupSummary,
    onImport: suspend (InputStream) -> EtaBackupSummary,
) {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }

    // 自动备份
    var autoSettings by remember { mutableStateOf(AutoBackupSettings()) }
    var autoBusy by remember { mutableStateOf(false) }
    var autoStatusText by remember { mutableStateOf("") }
    var autoLocation by remember { mutableStateOf("") }
    var showIntervalDialog by remember { mutableStateOf(false) }
    var showKeepDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var autoBackups by remember { mutableStateOf<List<AutoBackupFile>>(emptyList()) }
    fun applyAuto(transform: (AutoBackupSettings) -> AutoBackupSettings) {
        scope.launch {
            autoSettings = SettingsDataStore.updateAutoBackupSettings { transform(it).normalized() }
            AutoBackupAlarm.refresh(context)
            autoLocation = AutoBackupRunner.locationLabel(context, autoSettings)
            autoStatusText = autoStatusLabel(autoSettings)
        }
    }

    fun runAutoNow() {
        if (autoBusy) return
        scope.launch {
            autoBusy = true
            val result = runCatching { AutoBackupRunner.maybeRun(context, force = true) }
                .getOrElse { AutoBackupResult.Failed(it.message ?: context.getString(R.string.data_backup_failed)) }
            autoSettings = runCatching { SettingsDataStore.autoBackupSettings() }
                .getOrDefault(autoSettings)
            autoBackups = runCatching { AutoBackupRunner.backups(context, autoSettings) }.getOrDefault(emptyList())
            autoStatusText = when (result) {
                is AutoBackupResult.Done -> "已备份 ${result.fileName}（${result.sizeBytes / 1024} KB）"
                is AutoBackupResult.Skipped -> "已跳过：${result.reason}"
                is AutoBackupResult.Failed -> "失败：${result.message}"
            }
            autoBusy = false
        }
    }

    val directoryPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
            applyAuto { it.copy(directoryUri = uri.toString()) }
        }
    }

    LaunchedEffect(Unit) {
        autoSettings = runCatching { SettingsDataStore.autoBackupSettings() }
            .getOrDefault(AutoBackupSettings())
        autoLocation = AutoBackupRunner.locationLabel(context, autoSettings)
        autoBackups = runCatching { AutoBackupRunner.backups(context, autoSettings) }.getOrDefault(emptyList())
        autoStatusText = autoStatusLabel(autoSettings)
    }



    fun showFailure(throwable: Throwable) {
        if (throwable is CancellationException) throw throwable
        AndroidAgentLogger.error("Agent backup UI failed: type=${throwable.safeLogType()}\n${throwable.safeStackTrace()}")
        Toast.makeText(
            context,
            throwable.message ?: context.getString(R.string.data_backup_failed),
            Toast.LENGTH_LONG,
        ).show()
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            try {
                val output = context.contentResolver.openOutputStream(uri)
                    ?: error(context.getString(R.string.data_backup_file_open_failed))
                val summary = output.use { onExport(it) }
                Toast.makeText(
                    context,
                    context.getString(
                        R.string.data_backup_exported,
                        summary.conversationCount,
                        summary.providerCount,
                    ),
                    Toast.LENGTH_SHORT,
                ).show()
            } catch (throwable: Throwable) {
                showFailure(throwable)
            } finally {
                busy = false
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportDialog = true
        }
    }

    val confirmImportText = if (busy) {
        stringResource(R.string.data_backup_working)
    } else {
        stringResource(R.string.action_import)
    }
    val onDismissImport: () -> Unit = {
        if (!busy) {
            showImportDialog = false
            pendingImportUri = null
        }
    }
    val onConfirmImport: () -> Unit = {
        val uri = pendingImportUri
        if (uri != null) {
            showImportDialog = false
            scope.launch {
                busy = true
                try {
                    val input = context.contentResolver.openInputStream(uri)
                        ?: error(context.getString(R.string.data_backup_file_open_failed))
                    val summary = input.use { onImport(it) }
                    Toast.makeText(
                        context,
                        context.getString(
                            R.string.data_backup_imported,
                            summary.conversationCount,
                            summary.providerCount,
                        ),
                        Toast.LENGTH_SHORT,
                    ).show()
                } catch (throwable: Throwable) {
                    showFailure(throwable)
                } finally {
                    pendingImportUri = null
                    busy = false
                }
            }
        }
    }

    when (LocalUiStyle.current) {
        AppearanceUiStyle.COUI -> {
            CouiScaffoldPage(
                title = stringResource(R.string.data_backup_title),
                onBack = onBack,
                overlay = {
                    CouiConfirmDialog(
                        visible = showImportDialog,
                        title = stringResource(R.string.data_backup_import_confirm_title),
                        message = stringResource(R.string.data_backup_import_confirm_summary),
                        confirmText = confirmImportText,
                        confirmEnabled = !busy,
                        cancelEnabled = !busy,
                        destructive = true,
                        onConfirm = onConfirmImport,
                        onDismissRequest = onDismissImport,
                    )
                },
            ) {
                CouiPreferenceGroup {
                    item {
                        CouiPreference(
                            title = stringResource(R.string.data_backup_warning_title),
                            summary = stringResource(R.string.data_backup_warning_summary),
                            showChevron = false,
                        )
                    }
                }
                CouiPreferenceGroup(title = stringResource(R.string.data_backup_actions)) {
                    item {
                        CouiPreference(
                            title = stringResource(R.string.data_backup_export),
                            summary = if (busy) {
                                stringResource(R.string.data_backup_working)
                            } else {
                                stringResource(R.string.data_backup_export_summary)
                            },
                            enabled = !busy,
                            trailing = if (busy) {
                                { InfiniteProgressIndicator(size = 20.dp) }
                            } else {
                                null
                            },
                            onClick = { exportLauncher.launch(defaultBackupFileName()) },
                        )
                    }
                    item {
                        CouiPreference(
                            title = stringResource(R.string.data_backup_import),
                            summary = stringResource(R.string.data_backup_import_summary),
                            enabled = !busy,
                            onClick = {
                                importLauncher.launch(arrayOf("application/json", "text/plain"))
                            },
                        )
                    }
                }
                CouiPreferenceGroup(title = "自动备份") {
                    item {
                        CouiSwitchPreference(
                            title = "启用自动备份",
                            summary = if (autoBusy) stringResource(R.string.data_backup_working) else autoStatusText,
                            checked = autoSettings.enabled,
                            enabled = !autoBusy,
                            onCheckedChange = { value -> applyAuto { it.copy(enabled = value) } },
                        )
                    }
                    item {
                        CouiPreference(
                            title = "备份间隔",
                            value = "${autoSettings.intervalHours} 小时",
                            onClick = { showIntervalDialog = true },
                        )
                    }
                    item {
                        CouiPreference(
                            title = "保留份数",
                            value = "${autoSettings.keepCount} 份",
                            onClick = { showKeepDialog = true },
                        )
                    }
                    item {
                        CouiPreference(
                            title = "保存位置",
                            summary = autoLocation,
                            onClick = { directoryPicker.launch(null) },
                        )
                    }
                    item {
                        CouiPreference(
                            title = "立即备份一次",
                            enabled = !autoBusy,
                            onClick = { runAutoNow() },
                        )
                    }
                    item {
                        CouiPreference(
                            title = "备份历史",
                            value = "${autoBackups.size} 份",
                            onClick = { showHistoryDialog = true },
                        )
                    }
                }
                CouiPageBottomSpacer()
            }
        }

        AppearanceUiStyle.MIUIX -> {
            MiuixScaffoldPage(
                title = stringResource(R.string.data_backup_title),
                onBack = onBack,
                overlay = {
                    GlassDialog(
                        show = showImportDialog,
                        backdrop = currentGlassOverlayBackdrop(),
                        title = stringResource(R.string.data_backup_import_confirm_title),
                        summary = stringResource(R.string.data_backup_import_confirm_summary),
                        onDismissRequest = onDismissImport,
                    ) {
                        MiuixDialogActions(
                            confirmText = confirmImportText,
                            destructive = true,
                            cancelEnabled = !busy,
                            confirmEnabled = !busy,
                            onCancel = onDismissImport,
                            onConfirm = onConfirmImport,
                        )
                    }
                },
            ) {
                item(key = "warning") {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(top = 8.dp, bottom = 12.dp),
                    ) {
                        BasicComponent(
                            title = stringResource(R.string.data_backup_warning_title),
                            summary = stringResource(R.string.data_backup_warning_summary),
                        )
                    }
                }
                item(key = "actions") {
                    SmallTitle(stringResource(R.string.data_backup_actions))
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp),
                    ) {
                        ArrowPreference(
                            title = stringResource(R.string.data_backup_export),
                            summary = if (busy) {
                                stringResource(R.string.data_backup_working)
                            } else {
                                stringResource(R.string.data_backup_export_summary)
                            },
                            enabled = !busy,
                            endActions = {
                                if (busy) {
                                    InfiniteProgressIndicator(size = 20.dp)
                                }
                            },
                            onClick = {
                                exportLauncher.launch(defaultBackupFileName())
                            },
                        )
                        ArrowPreference(
                            title = stringResource(R.string.data_backup_import),
                            summary = stringResource(R.string.data_backup_import_summary),
                            enabled = !busy,
                            onClick = {
                                importLauncher.launch(arrayOf("application/json", "text/plain"))
                            },
                        )
                    }
                }
                item(key = "auto") {
                    SmallTitle("自动备份")
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp),
                    ) {
                        SwitchPreference(
                            title = "启用自动备份",
                            summary = if (autoBusy) {
                                stringResource(R.string.data_backup_working)
                            } else {
                                autoStatusText
                            },
                            checked = autoSettings.enabled,
                            enabled = !autoBusy,
                            onCheckedChange = { value -> applyAuto { it.copy(enabled = value) } },
                        )
                        ArrowPreference(
                            title = "备份间隔",
                            endActions = { Text("${autoSettings.intervalHours} 小时") },
                            onClick = { showIntervalDialog = true },
                        )
                        ArrowPreference(
                            title = "保留份数",
                            endActions = { Text("${autoSettings.keepCount} 份") },
                            onClick = { showKeepDialog = true },
                        )
                        ArrowPreference(
                            title = "保存位置",
                            summary = autoLocation,
                            onClick = { directoryPicker.launch(null) },
                        )
                        ArrowPreference(
                            title = "立即备份一次",
                            enabled = !autoBusy,
                            onClick = { runAutoNow() },
                        )
                        ArrowPreference(
                            title = "备份历史",
                            endActions = { Text("${autoBackups.size} 份") },
                            onClick = { showHistoryDialog = true },
                        )
                    }
                }
            }
        }
    }

    // ---- 自动备份对话框 ----
    AutoBackupChoiceDialog(
        show = showIntervalDialog,
        title = "备份间隔",
        options = AutoBackupSettings.INTERVAL_OPTIONS.map { it to "$it 小时" },
        selected = autoSettings.intervalHours,
        onDismiss = { showIntervalDialog = false },
        onPick = { value ->
            showIntervalDialog = false
            applyAuto { it.copy(intervalHours = value) }
        },
    )
    AutoBackupChoiceDialog(
        show = showKeepDialog,
        title = "保留份数",
        options = AutoBackupSettings.KEEP_OPTIONS.map { it to "$it 份" },
        selected = autoSettings.keepCount,
        onDismiss = { showKeepDialog = false },
        onPick = { value ->
            showKeepDialog = false
            applyAuto { it.copy(keepCount = value) }
        },
    )
    GlassDialog(
        show = showHistoryDialog,
        backdrop = currentGlassOverlayBackdrop(),
        title = "备份历史",
        summary = if (autoBackups.isEmpty()) "还没有备份文件。" else "点击删除单份备份。",
        onDismissRequest = { showHistoryDialog = false },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            autoBackups.take(20).forEach { backup ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = backup.name,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "删除",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            runCatching { backup.delete() }
                            autoBackups = runCatching {
                                AutoBackupRunner.backups(context, autoSettings)
                            }.getOrDefault(emptyList())
                        },
                    )
                }
            }
        }
    }
}

/** 自动备份的档位选择弹窗：值与文案成对传入，选中后立即落库。 */
@Composable
private fun AutoBackupChoiceDialog(
    show: Boolean,
    title: String,
    options: List<Pair<Int, String>>,
    selected: Int,
    onDismiss: () -> Unit,
    onPick: (Int) -> Unit,
) {
    GlassDialog(
        show = show,
        backdrop = currentGlassOverlayBackdrop(),
        title = title,
        onDismissRequest = onDismiss,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            options.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(value) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    if (value == selected) {
                        Text(
                            text = "已选",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

/** 自动备份状态一行文案：优先显示失败原因，其次显示上次成功时间。 */
private fun autoStatusLabel(settings: AutoBackupSettings): String = when {
    settings.lastError.isNotBlank() -> "上次失败：${settings.lastError}"
    settings.lastAt > 0L -> "上次备份：" + SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        .format(Date(settings.lastAt))
    else -> "还没有备份过。"
}

private fun defaultBackupFileName(): String =
    "Eta-backup-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())}.eta-backup.json"
