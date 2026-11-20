package io.github.mangi.eta.ui.screens.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.Text as CouiText
import io.github.mangi.eta.R
import io.github.mangi.eta.core.AndroidAgentLogger
import io.github.mangi.eta.core.safeLogType
import io.github.mangi.eta.core.safeStackTrace
import io.github.mangi.eta.data.model.AppearanceUiStyle
import io.github.mangi.eta.data.model.AutoBackupSettings
import io.github.mangi.eta.agent.automation.AutoBackupAlarm
import io.github.mangi.eta.data.datastore.SettingsDataStore
import io.github.mangi.eta.data.repository.AutoBackupResult
import io.github.mangi.eta.data.repository.AutoBackupRunner
import io.github.mangi.eta.data.repository.EtaBackupSummary
import io.github.mangi.eta.ui.app.LocalUiStyle
import io.github.mangi.eta.ui.components.MiuixDialogActions
import io.github.mangi.eta.ui.components.MiuixScaffoldPage
import io.github.mangi.eta.ui.components.coui.CouiConfirmDialog
import io.github.mangi.eta.ui.components.coui.CouiPageBottomSpacer
import io.github.mangi.eta.ui.components.coui.CouiPreference
import io.github.mangi.eta.ui.components.coui.CouiScaffoldPage
import io.github.mangi.eta.ui.components.coui.CouiSwitchPreference
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
import top.yukonga.miuix.kmp.preference.SwitchPreference

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

    // ---- 自动备份 ----
    var autoSettings by remember { mutableStateOf(AutoBackupSettings()) }
    var autoBusy by remember { mutableStateOf(false) }
    var autoTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(autoTick) {
        autoSettings = runCatching { SettingsDataStore.autoBackupSettings() }
            .getOrDefault(AutoBackupSettings())
    }

    val autoDirectoryText = remember(autoTick, autoSettings) {
        AutoBackupRunner.locationLabel(context, autoSettings)
    }

    fun applyAuto(transform: (AutoBackupSettings) -> AutoBackupSettings) {
        scope.launch {
            runCatching {
                SettingsDataStore.updateAutoBackupSettings { transform(it).normalized() }
                AutoBackupAlarm.refresh(context)
            }
            autoTick++
        }
    }

    fun backupNow() {
        scope.launch {
            autoBusy = true
            try {
                when (val result = AutoBackupRunner.maybeRun(context, force = true)) {
                    is AutoBackupResult.Done -> Toast.makeText(
                        context,
                        "已备份 " + result.conversationCount + " 个会话 · " + result.fileName,
                        Toast.LENGTH_LONG,
                    ).show()

                    is AutoBackupResult.Skipped ->
                        Toast.makeText(context, result.reason, Toast.LENGTH_SHORT).show()

                    is AutoBackupResult.Failed ->
                        Toast.makeText(context, "备份失败：" + result.message, Toast.LENGTH_LONG).show()
                }
            } catch (throwable: Throwable) {
                showFailure(throwable)
            } finally {
                autoBusy = false
                autoTick++
            }
        }
    }

    val autoTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
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

    val autoStatusText = when {
        autoSettings.lastError.isNotBlank() -> "上次失败：" + autoSettings.lastError
        autoSettings.lastAt > 0L -> "上次备份：" +
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date(autoSettings.lastAt)) +
            if (autoSettings.lastFileName.isNotBlank()) " · " + autoSettings.lastFileName else ""
        else -> "还没有备份过"
    }

    when (LocalUiStyle.current) {
        AppearanceUiStyle.COUI -> {
            CouiScaffoldPage(
                title = stringResource(R.string.data_backup_title),
                onBack = onBack,
            ) {
                Group {
                    CouiPreference(
                        title = stringResource(R.string.data_backup_warning_title),
                        summary = stringResource(R.string.data_backup_warning_summary),
                        showChevron = false,
                    )
                }
                Group(title = stringResource(R.string.data_backup_actions)) {
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
                    Divider()
                    CouiPreference(
                        title = stringResource(R.string.data_backup_import),
                        summary = stringResource(R.string.data_backup_import_summary),
                        enabled = !busy,
                        onClick = {
                            importLauncher.launch(arrayOf("application/json", "text/plain"))
                        },
                    )
                }
                Group(title = "自动备份") {
                    CouiSwitchPreference(
                        title = "启用自动备份",
                        summary = "按周期导出数据，内容没变化时自动跳过",
                        checked = autoSettings.enabled,
                        onCheckedChange = { enabled -> applyAuto { it.copy(enabled = enabled) } },
                    )
                    Divider()
                    CouiPreference(
                        title = "备份文件夹",
                        summary = autoDirectoryText,
                        value = "选择",
                        showChevron = true,
                        onClick = { autoTreeLauncher.launch(null) },
                    )
                    if (autoSettings.directoryUri.isNotBlank()) {
                        Divider()
                        CouiPreference(
                            title = "恢复默认文件夹",
                            summary = "改回默认的 Eta备份 目录",
                            onClick = { applyAuto { it.copy(directoryUri = "") } },
                        )
                    }
                    Divider()
                    CouiPreference(
                        title = "备份周期",
                        summary = autoStatusText,
                        value = autoIntervalLabel(autoSettings.intervalHours),
                        enabled = autoSettings.enabled,
                        onClick = {
                            applyAuto { settings ->
                                settings.copy(intervalHours = autoNextInterval(settings.intervalHours))
                            }
                        },
                    )
                    Divider()
                    CouiPreference(
                        title = "保留份数",
                        value = autoSettings.keepCount.toString() + " 份",
                        enabled = autoSettings.enabled,
                        onClick = {
                            applyAuto { settings ->
                                settings.copy(keepCount = autoNextKeepCount(settings.keepCount))
                            }
                        },
                    )
                    Divider()
                    CouiPreference(
                        title = "立即备份一次",
                        summary = if (autoBusy) stringResource(R.string.data_backup_working) else autoStatusText,
                        enabled = !autoBusy,
                        showChevron = false,
                        trailing = if (autoBusy) {
                            { InfiniteProgressIndicator(size = 20.dp) }
                        } else {
                            null
                        },
                        onClick = { if (!autoBusy) backupNow() },
                    )
                }
                CouiPageBottomSpacer()
            }

            if (showImportDialog) {
                CouiConfirmDialog(
                    visible = true,
                    title = stringResource(R.string.data_backup_import_confirm_title),
                    message = stringResource(R.string.data_backup_import_confirm_summary),
                    confirmText = confirmImportText,
                    confirmEnabled = !busy,
                    cancelEnabled = !busy,
                    destructive = true,
                    onConfirm = onConfirmImport,
                    onDismissRequest = onDismissImport,
                )
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
                            summary = "按周期导出数据，内容没变化时自动跳过",
                            checked = autoSettings.enabled,
                            onCheckedChange = { enabled -> applyAuto { it.copy(enabled = enabled) } },
                        )
                        ArrowPreference(
                            title = "备份文件夹",
                            summary = autoDirectoryText,
                            onClick = { autoTreeLauncher.launch(null) },
                        )
                        if (autoSettings.directoryUri.isNotBlank()) {
                            ArrowPreference(
                                title = "恢复默认文件夹",
                                summary = "改回默认的 Eta备份 目录",
                                onClick = { applyAuto { it.copy(directoryUri = "") } },
                            )
                        }
                        ArrowPreference(
                            title = "备份周期",
                            summary = "每 " + autoIntervalLabel(autoSettings.intervalHours) +
                                " · " + autoStatusText,
                            onClick = {
                                applyAuto { settings ->
                                    settings.copy(intervalHours = autoNextInterval(settings.intervalHours))
                                }
                            },
                        )
                        ArrowPreference(
                            title = "保留份数",
                            summary = "保留最近 " + autoSettings.keepCount + " 份，点击切换",
                            onClick = {
                                applyAuto { settings ->
                                    settings.copy(keepCount = autoNextKeepCount(settings.keepCount))
                                }
                            },
                        )
                        ArrowPreference(
                            title = "立即备份一次",
                            summary = if (autoBusy) stringResource(R.string.data_backup_working) else autoStatusText,
                            enabled = !autoBusy,
                            endActions = {
                                if (autoBusy) {
                                    InfiniteProgressIndicator(size = 20.dp)
                                }
                            },
                            onClick = { if (!autoBusy) backupNow() },
                        )
                    }
                }
            }
        }
    }
}

private fun autoIntervalLabel(hours: Int): String = when {
    hours % 24 == 0 && hours >= 24 -> (hours / 24).toString() + " 天"
    else -> hours.toString() + " 小时"
}

private fun autoNextInterval(current: Int): Int {
    val options = AutoBackupSettings.INTERVAL_OPTIONS
    val index = options.indexOf(current).let { if (it >= 0) it else options.indexOf(24).coerceAtLeast(0) }
    return options[(index + 1) % options.size]
}

private fun autoNextKeepCount(current: Int): Int {
    val options = AutoBackupSettings.KEEP_OPTIONS
    val index = options.indexOf(current).let { if (it >= 0) it else 0 }
    return options[(index + 1) % options.size]
}

private fun defaultBackupFileName(): String =
    "Eta-backup-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())}.eta-backup.json"
