package io.github.mangi.eta.ui.screens.terminal

import androidx.compose.foundation.layout.Row
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.R
import io.github.mangi.eta.data.model.AppearanceUiStyle
import io.github.mangi.eta.ui.app.LocalUiStyle
import io.github.mangi.eta.agent.terminal.WorkspaceRoot
import io.github.mangi.eta.ui.app.WorkspaceEntry
import io.github.mangi.eta.ui.app.WorkspaceFileStore
import io.github.mangi.eta.ui.components.EtaPreferenceGroupItem
import io.github.mangi.eta.ui.components.ListEmptyState
import io.github.mangi.eta.ui.components.AdaptiveTopAppBar
import io.github.mangi.eta.ui.components.MiuixBackButton
import io.github.mangi.eta.ui.components.coui.CouiScaffoldPage
import io.github.mangi.eta.ui.components.coui.CouiPageDefaults
import io.github.mangi.eta.ui.components.coui.CouiPreference
import io.github.mangi.eta.ui.components.coui.CouiPreferenceGroup
import io.github.mangi.eta.ui.components.coui.CouiSectionTitle
import io.github.mangi.eta.ui.components.coui.CouiSettingsIcon
import io.github.suqi8.coui.kmp.theme.COUITheme as CouiTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 工作文件夹的纯内容视图，不带自己的页面外壳。
 *
 * 抽出来是为了两处复用：
 * 1. 二级页 [WorkspaceScreen]（顶栏进入，有返回按钮）；
 * 2. 对话页横向 Pager 的左页（内嵌，无返回按钮，标题由外层提供）。
 *
 * 两处共用同一份状态逻辑，避免两套实现后续走偏。
 */
@Composable
internal fun WorkspacePanelContent(
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    // 自定义工作目录：切换后要重建 store 并回到根目录，否则列表还停在旧目录的相对路径上。
    var workspaceRootVersion by remember { mutableIntStateOf(0) }
    var customRoot by remember { mutableStateOf(WorkspaceRoot.custom(context)) }
    // 手动输入路径：Android 11+ 的 SAF 不允许把存储根（/storage/emulated/0 本身）
    // 选成 tree，只能选子目录；想直接用存储根当工作区必须手输。
    var manualOpen by remember { mutableStateOf(false) }
    var manualText by remember { mutableStateOf("") }
    // 设置区默认收起：四块设置曾占掉页面六成高度，文件列表只剩一屏不到。
    var settingsExpanded by rememberSaveable { mutableStateOf(false) }
    val store = remember(context.applicationContext, workspaceRootVersion) { WorkspaceFileStore(context) }
    val scope = rememberCoroutineScope()
    var path by rememberSaveable { mutableStateOf("") }
    var pendingExport by rememberSaveable { mutableStateOf<String?>(null) }
    var entries by remember { mutableStateOf<List<WorkspaceEntry>?>(null) }
    var revision by remember { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<WorkspaceMessage?>(null) }
    var publicAccess by remember { mutableStateOf(Environment.isExternalStorageManager()) }
    val accessLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        publicAccess = Environment.isExternalStorageManager()
    }
    val rootPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val mapped = WorkspaceRoot.fromTreeUri(context, uri)
        if (mapped == null) {
            message = WorkspaceMessage(resources.getString(R.string.capability_workspace_root_unsupported), isError = true)
            return@rememberLauncherForActivityResult
        }
        WorkspaceRoot.save(context, mapped)
        customRoot = mapped
        path = ""
        revision++
        workspaceRootVersion++
        message = WorkspaceMessage(
            resources.getString(R.string.capability_workspace_root_applied, mapped),
            isError = false,
        )
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) scope.launch {
            busy = true
            try {
                var succeeded = 0
                uris.forEach { if (store.importFile(it)) succeeded++ }
                message = if (succeeded == uris.size) {
                    WorkspaceMessage(resources.getString(R.string.capability_workspace_imported), isError = false)
                } else {
                    WorkspaceMessage(
                        resources.getQuantityString(R.plurals.capability_workspace_partial_import, succeeded, succeeded),
                        isError = true,
                    )
                }
                if (succeeded > 0) path = "imports"
                revision++
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                message = WorkspaceMessage(resources.getString(R.string.capability_workspace_failed), isError = true)
            } finally {
                busy = false
            }
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        val source = pendingExport
        pendingExport = null
        if (uri != null && source != null) scope.launch {
            busy = true
            try {
                store.exportFile(source, uri)
                message = WorkspaceMessage(resources.getString(R.string.capability_workspace_exported), isError = false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                message = WorkspaceMessage(resources.getString(R.string.capability_workspace_failed), isError = true)
            } finally {
                busy = false
            }
        }
    }
    LaunchedEffect(path) { entries = null }
    LaunchedEffect(path, revision) {
        try {
            entries = store.list(path)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            entries = emptyList()
            message = WorkspaceMessage(resources.getString(R.string.capability_workspace_failed), isError = true)
        }
    }
    val uiStyle = LocalUiStyle.current
    val onOpenPublicAccess: () -> Unit = {
        try {
            accessLauncher.launch(
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:${context.packageName}"),
                ),
            )
        } catch (_: android.content.ActivityNotFoundException) {
            message = WorkspaceMessage(resources.getString(R.string.capability_workspace_failed), isError = true)
        }
    }

    val listContent: LazyListScope.() -> Unit = {
        // 折叠头：一行显示当前生效路径 + 展开/收起入口。
        // 展开前不渲染下面四块设置，把高度全让给文件列表。
        item(key = "workspace-settings-header") {
            val currentPath = customRoot.ifBlank { WorkspaceRoot.displayPath(context) }
            val expandLabel = if (settingsExpanded) "收起设置" else "工作目录"
            if (uiStyle == AppearanceUiStyle.COUI) {
                CouiPreferenceGroup {
                    item {
                        CouiPreference(
                            title = expandLabel,
                            summary = currentPath,
                            onClick = { settingsExpanded = !settingsExpanded },
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 8.dp, bottom = 12.dp),
                ) {
                    ArrowPreference(
                        title = expandLabel,
                        summary = currentPath,
                        onClick = { settingsExpanded = !settingsExpanded },
                    )
                }
            }
        }
        // 换目录是高频操作，从折叠区里提出来单独一行：不用先展开设置再找入口。
        item(key = "workspace-change-root") {
            if (uiStyle == AppearanceUiStyle.COUI) {
                CouiPreferenceGroup {
                    item {
                        CouiPreference(
                            title = "更换目录",
                            summary = "点这里挑一个新位置，或展开设置手动输入路径",
                            onClick = { rootPicker.launch(null) },
                        )
                    }
                }
            } else {
                Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    ArrowPreference(
                        title = "更换目录",
                        summary = "点这里挑一个新位置，或展开设置手动输入路径",
                        onClick = { rootPicker.launch(null) },
                    )
                }
            }
        }
        if (settingsExpanded) {
        item(key = "workspace-root") {
            val rootSummary = if (customRoot.isBlank()) {
                stringResource(R.string.capability_workspace_root_summary_default)
            } else {
                stringResource(R.string.capability_workspace_root_summary_custom, customRoot)
            }
            val resetLabel = stringResource(R.string.capability_workspace_root_reset)
            if (uiStyle == AppearanceUiStyle.COUI) {
                CouiPreferenceGroup {
                    item {
                        CouiPreference(
                            title = stringResource(R.string.capability_workspace_root),
                            summary = rootSummary,
                            onClick = { rootPicker.launch(null) },
                        )
                    }
                    item {
                        CouiPreference(
                            title = stringResource(R.string.capability_workspace_root_manual),
                            onClick = { manualOpen = !manualOpen; manualText = customRoot },
                        )
                    }
                    if (manualOpen) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                BasicTextField(
                                    value = manualText,
                                    onValueChange = { manualText = it.take(400) },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = MiuixTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { inner ->
                                        if (manualText.isEmpty()) {
                                            Text(
                                                text = stringResource(R.string.capability_workspace_root_manual_hint),
                                                style = MiuixTheme.textStyles.footnote1,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                            )
                                        }
                                        inner()
                                    },
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    TextButton(
                                        text = stringResource(R.string.capability_workspace_root_cancel),
                                        onClick = { manualOpen = false },
                                        minWidth = 0.dp,
                                        minHeight = 34.dp,
                                        insideMargin = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.textButtonColors(),
                                    )
                                    TextButton(
                                        text = stringResource(R.string.capability_workspace_root_confirm),
                                        onClick = {
                                            val error = WorkspaceRoot.validateManualPath(manualText)
                                            if (error != null) {
                                                message = WorkspaceMessage(
                                                    resources.getString(
                                                        R.string.capability_workspace_root_manual_invalid,
                                                        manualText.trim(),
                                                    ),
                                                    isError = true,
                                                )
                                            } else {
                                                val target = manualText.trim()
                                                WorkspaceRoot.save(context, target)
                                                customRoot = target
                                                manualOpen = false
                                                path = ""
                                                revision++
                                                workspaceRootVersion++
                                                message = WorkspaceMessage(
                                                    resources.getString(R.string.capability_workspace_root_applied, target),
                                                    isError = false,
                                                )
                                            }
                                        },
                                        minWidth = 0.dp,
                                        minHeight = 34.dp,
                                        insideMargin = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.textButtonColorsPrimary(),
                                    )
                                }
                            }
                        }
                    }
                    if (customRoot.isNotBlank()) {
                        item {
                            CouiPreference(
                                title = resetLabel,
                                onClick = {
                                    WorkspaceRoot.save(context, "")
                                    customRoot = ""
                                    path = ""
                                    revision++
                                    workspaceRootVersion++
                                },
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp),
                ) {
                    ArrowPreference(
                        title = stringResource(R.string.capability_workspace_root),
                        summary = rootSummary,
                        onClick = { rootPicker.launch(null) },
                    )
                    ArrowPreference(
                        title = stringResource(R.string.capability_workspace_root_manual),
                        onClick = { manualOpen = !manualOpen; manualText = customRoot },
                    )
                    if (manualOpen) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            BasicTextField(
                                value = manualText,
                                onValueChange = { manualText = it.take(400) },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = MiuixTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    if (manualText.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.capability_workspace_root_manual_hint),
                                            style = MiuixTheme.textStyles.footnote1,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        )
                                    }
                                    inner()
                                },
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(
                                    text = stringResource(R.string.capability_workspace_root_cancel),
                                    onClick = { manualOpen = false },
                                    minWidth = 0.dp,
                                    minHeight = 34.dp,
                                    insideMargin = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.textButtonColors(),
                                )
                                TextButton(
                                    text = stringResource(R.string.capability_workspace_root_confirm),
                                    onClick = {
                                        val error = WorkspaceRoot.validateManualPath(manualText)
                                        if (error != null) {
                                            message = WorkspaceMessage(
                                                resources.getString(
                                                    R.string.capability_workspace_root_manual_invalid,
                                                    manualText.trim(),
                                                ),
                                                isError = true,
                                            )
                                        } else {
                                            val target = manualText.trim()
                                            WorkspaceRoot.save(context, target)
                                            customRoot = target
                                            manualOpen = false
                                            path = ""
                                            revision++
                                            workspaceRootVersion++
                                            message = WorkspaceMessage(
                                                resources.getString(R.string.capability_workspace_root_applied, target),
                                                isError = false,
                                            )
                                        }
                                    },
                                    minWidth = 0.dp,
                                    minHeight = 34.dp,
                                    insideMargin = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.textButtonColorsPrimary(),
                                )
                            }
                        }
                    }
                    if (customRoot.isNotBlank()) {
                        ArrowPreference(
                            title = resetLabel,
                            onClick = {
                                WorkspaceRoot.save(context, "")
                                customRoot = ""
                                path = ""
                                revision++
                                workspaceRootVersion++
                            },
                        )
                    }
                }
            }
        }
        item(key = "actions") {
            val publicSummary = if (publicAccess) {
                stringResource(R.string.capability_workspace_public_granted)
            } else {
                stringResource(R.string.capability_workspace_public_summary)
            }
            if (uiStyle == AppearanceUiStyle.COUI) {
                CouiPreferenceGroup {
                    item {
                        CouiPreference(
                            title = stringResource(R.string.capability_workspace_import),
                            enabled = !busy,
                            trailing = if (busy) {
                                { InfiniteProgressIndicator(size = 20.dp) }
                            } else {
                                null
                            },
                            onClick = { importLauncher.launch(arrayOf("*/*")) },
                        )
                    }
                    item {
                        CouiPreference(
                            title = stringResource(R.string.capability_workspace_public),
                            summary = publicSummary,
                            onClick = onOpenPublicAccess,
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp),
                ) {
                    ArrowPreference(
                        title = stringResource(R.string.capability_workspace_import),
                        enabled = !busy,
                        endActions = {
                            if (busy) {
                                InfiniteProgressIndicator(size = 20.dp)
                            }
                        },
                        onClick = { importLauncher.launch(arrayOf("*/*")) },
                    )
                    ArrowPreference(
                        title = stringResource(R.string.capability_workspace_public),
                        summary = publicSummary,
                        onClick = onOpenPublicAccess,
                    )
                }
            }
        }
        }  // end settingsExpanded
        message?.let { current ->
            item(key = "message") {
                if (uiStyle == AppearanceUiStyle.COUI) {

                    CouiPreferenceGroup(modifier = Modifier.animateItem()) {
                        item {
                            CouiPreference(
                                title = current.text,
                                titleColor = if (current.isError) CouiTheme.colorScheme.error else Color.Unspecified,
                                showChevron = false,
                                trailing = {
                                    IconButton(onClick = { message = null }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = stringResource(R.string.action_close),
                                            modifier = Modifier.size(18.dp),
                                            tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                                        )
                                    }
                                },
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp),
                    ) {
                        BasicComponent(
                            title = current.text,
                            titleColor = BasicComponentDefaults.titleColor(
                                color = if (current.isError) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onBackground,
                            ),
                            endActions = {
                                IconButton(onClick = { message = null }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.action_close),
                                        modifier = Modifier.size(18.dp),
                                        tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
        item(key = "path") {
            val sectionText = if (path.isBlank()) stringResource(R.string.capability_workspace_files) else path
            if (uiStyle == AppearanceUiStyle.COUI) {
                CouiSectionTitle(text = sectionText)
            } else {
                SmallTitle(sectionText)
            }
        }
        val currentEntries = entries
        val hasParent = path.isNotBlank()
        if (hasParent) {
            item(key = "parent") {
                EtaPreferenceGroupItem(
                    isFirst = true,
                    isLast = currentEntries.isNullOrEmpty(),
                    hasLeadingIcon = true,
                ) {
                    if (uiStyle == AppearanceUiStyle.COUI) {
                        val colors = CouiPageDefaults.themeColors()
                        CouiPreference(
                            title = stringResource(R.string.capability_workspace_parent),
                            icon = { CouiSettingsIcon(Icons.Rounded.FolderOpen, colors.accent) },
                            onClick = { path = path.substringBeforeLast('/', "") },
                        )
                    } else {
                        ArrowPreference(
                            title = stringResource(R.string.capability_workspace_parent),
                            startAction = {
                                Icon(
                                    imageVector = Icons.Rounded.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp).size(24.dp),
                                    tint = MiuixTheme.colorScheme.primary,
                                )
                            },
                            onClick = { path = path.substringBeforeLast('/', "") },
                        )
                    }
                }
            }
        }
        when {
            currentEntries == null -> item(key = "loading") {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    InfiniteProgressIndicator(size = 24.dp)
                }
            }
            currentEntries.isEmpty() -> item(key = "empty") {
                ListEmptyState(
                    title = stringResource(R.string.capability_workspace_empty),
                    summary = stringResource(R.string.capability_workspace_empty_summary),
                    icon = Icons.Rounded.FolderOpen,
                )
            }
            else -> itemsIndexed(currentEntries, key = { _, entry -> entry.path }) { index, entry ->
                val summary = if (entry.directory) stringResource(R.string.capability_workspace_directory)
                else stringResource(
                    R.string.capability_workspace_file_export,
                    Formatter.formatShortFileSize(context, entry.size),
                )
                val icon = if (entry.directory) Icons.Rounded.Folder else Icons.AutoMirrored.Rounded.InsertDriveFile
                val onEntryClick: () -> Unit = {
                    if (entry.directory) {
                        path = entry.path
                    } else {
                        pendingExport = entry.path
                        exportLauncher.launch(entry.name)
                    }
                }
                EtaPreferenceGroupItem(
                    isFirst = index == 0 && !hasParent,
                    isLast = index == currentEntries.lastIndex,
                    hasLeadingIcon = true,
                ) {
                    if (uiStyle == AppearanceUiStyle.COUI) {
                        val colors = CouiPageDefaults.themeColors()
                        CouiPreference(
                            title = entry.name,
                            summary = summary,
                            icon = {
                                CouiSettingsIcon(
                                    icon = icon,
                                    tint = if (entry.directory) colors.accent else colors.summary,
                                )
                            },
                            enabled = !busy,
                            onClick = onEntryClick,
                        )
                    } else {
                        ArrowPreference(
                            title = entry.name,
                            summary = summary,
                            startAction = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp).size(24.dp),
                                    tint = if (entry.directory) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantActions,
                                )
                            },
                            enabled = !busy,
                            onClick = onEntryClick,
                        )
                    }
                }
            }
        }
    }

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = androidx.compose.foundation.lazy.rememberLazyListState(),
    ) {
        if (header != null) {
            item(key = "workspace-header") { header() }
        }
        listContent()
    }
}

/**
 * 二级页外壳：顶栏进入，带返回按钮。
 *
 * 内容与 Pager 左页共用 [WorkspacePanelContent]（它自带 LazyColumn），
 * 所以这里用非 Lazy 的 [CouiScaffoldPage] 承接；MIUIX 侧没有等价的非 Lazy 外壳，
 * 用 Miuix Scaffold 手搭顶栏。两处都把标题栏作为 header 插槽传进同一个内容函数，
 * 避免把 LazyColumn 嵌进另一个 LazyColumn 造成无限高度测量崩溃。
 */
@Composable
internal fun WorkspaceScreen(onBack: () -> Unit) {
    when (LocalUiStyle.current) {
        AppearanceUiStyle.COUI -> {
            CouiScaffoldPage(
                title = stringResource(R.string.capability_workspace),
                onBack = onBack,
            ) {
                WorkspacePanelContent()
            }
        }
        AppearanceUiStyle.MIUIX -> {
            top.yukonga.miuix.kmp.basic.Scaffold(
                topBar = {
                    AdaptiveTopAppBar(
                        title = stringResource(R.string.capability_workspace),
                        navigationIcon = { MiuixBackButton(onClick = onBack) },
                    )
                },
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    WorkspacePanelContent()
                }
            }
        }
    }
}

private data class WorkspaceMessage(val text: String, val isError: Boolean)
