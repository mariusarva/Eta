package io.github.mangi.eta.ui.screens.github

import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import io.github.mangi.eta.agent.github.GitHubAccountManager
import io.github.mangi.eta.agent.github.GitHubAccountState
import io.github.mangi.eta.ui.components.coui.CouiCardDivider
import io.github.mangi.eta.ui.components.coui.CouiPageBottomSpacer
import io.github.mangi.eta.ui.components.coui.CouiPreference
import io.github.mangi.eta.ui.components.coui.CouiPreferenceGroup
import io.github.mangi.eta.ui.components.coui.CouiScaffoldPage
import io.github.mangi.eta.ui.components.coui.CouiSettingsIcon
import io.github.mangi.eta.ui.components.coui.CouiSwitchPreference
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import io.github.mangi.eta.data.model.AppearanceUiStyle
import io.github.mangi.eta.ui.app.LocalUiStyle
import io.github.mangi.eta.ui.components.MiuixScaffoldPage
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.basic.BasicComponent
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 账号页图标色：与设置页保持一致的蓝。 */
private object GitHubIconColors {
    val Blue = androidx.compose.ui.graphics.Color(0xFF4285F4)
}

/**
 * GitHub 账号：设备码授权，凭据进 Keystore，连接后自动注册 GitHub 官方远程 MCP 并同步给终端 git。
 *
 * 分组沿用上游的 CouiPreferenceGroup，与其余设置页保持同一套卡片语义。
 */
@Composable
internal fun GitHubAccountScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val state by GitHubAccountManager.state.collectAsState()
    val clipboard = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        GitHubAccountManager.init(context)
        GitHubAccountManager.syncIfConfigured()
    }

    when (LocalUiStyle.current) {
        AppearanceUiStyle.COUI -> CouiScaffoldPage(title = "GitHub 账号", onBack = onBack, scrollState = scrollState) {
        when (val current = state) {
            is GitHubAccountState.Disconnected -> {
                CouiPreferenceGroup(title = "账号") {
                    item {
                        CouiPreference(
                            title = "未连接",
                            summary = "用设备码授权，手机上不需要注册回调地址。",
                            icon = { CouiSettingsIcon(Icons.Rounded.AccountCircle, GitHubIconColors.Blue) },
                            onClick = { GitHubAccountManager.connect() },
                        )
                    }
                }
                CouiPreferenceGroup(title = "说明") {
                    item {
                        CouiPreference(
                            title = "授权范围",
                            summary = "repo、read:org、workflow、gist。授权记录会出现在 GitHub " +
                                "Settings → Applications，可随时撤销。",
                        )
                    }
                }
            }

            is GitHubAccountState.Connecting -> {
                CouiPreferenceGroup(title = "账号") {
                    item {
                        CouiPreference(
                            title = "正在申请设备码…",
                            summary = "马上会显示一个 8 位验证码。",
                        )
                    }
                }
            }

            is GitHubAccountState.Waiting -> {
                CouiPreferenceGroup(title = "验证码") {
                    item {
                        CouiPreference(
                            title = current.userCode,
                            summary = "点这里复制",
                            onClick = {
                                clipboard.setText(AnnotatedString(current.userCode))
                                Toast.makeText(context, "已复制验证码", Toast.LENGTH_SHORT).show()
                            },
                        )
                    }
                    item { CouiCardDivider() }
                    item {
                        CouiPreference(
                            title = "打开授权页面",
                            summary = current.verificationUri,
                            onClick = { uriHandler.openUri(current.verificationUri) },
                        )
                    }
                }
                CouiPreferenceGroup(title = "操作") {
                    item {
                        CouiPreference(
                            title = "取消连接",
                            onClick = { GitHubAccountManager.cancel() },
                        )
                    }
                }
                CouiPreferenceGroup(title = "说明") {
                    item {
                        CouiPreference(
                            title = "下一步",
                            summary = "在浏览器里输入上面的验证码并确认，这里会自动完成连接。",
                        )
                    }
                }
            }

            is GitHubAccountState.Connected -> {
                CouiPreferenceGroup(title = "账号") {
                    item {
                        CouiPreference(
                            title = current.login,
                            summary = "授权范围：${current.scopes}",
                            icon = { CouiSettingsIcon(Icons.Rounded.AccountCircle, GitHubIconColors.Blue) },
                        )
                    }
                }

                CouiPreferenceGroup(title = "终端同步") {
                    item {
                        CouiSwitchPreference(
                            title = "同步到终端 git",
                            summary = "写入 ~/.git-credentials 与 .gitconfig，让 Linux 环境里的 git 直接可用。",
                            checked = GitHubAccountManager.syncToTerminalEnabled(),
                            onCheckedChange = { GitHubAccountManager.setSyncToTerminal(it) },
                        )
                    }
                    if (current.syncedTerminals.isNotEmpty()) {
                        item { CouiCardDivider() }
                        item {
                            CouiPreference(
                                title = "已同步",
                                summary = current.syncedTerminals.joinToString("、"),
                            )
                        }
                    }
                    if (current.pendingTerminals.isNotEmpty()) {
                        item { CouiCardDivider() }
                        item {
                            CouiPreference(
                                title = "等待环境就绪",
                                summary = current.pendingTerminals.joinToString("、") +
                                    "（装完 Linux 环境后回到本页会自动补）",
                            )
                        }
                    }
                    current.syncError?.let { error ->
                        item { CouiCardDivider() }
                        item { CouiPreference(title = "同步失败", summary = error) }
                    }
                }

                CouiPreferenceGroup(title = "MCP") {
                    item {
                        CouiPreference(
                            title = "GitHub MCP 服务器",
                            summary = "连接时自动注册，断开时一并删除。工具列表里可直接使用。",
                        )
                    }
                }

                CouiPreferenceGroup(title = "危险操作") {
                    item {
                        CouiPreference(
                            title = "断开连接",
                            summary = "删除凭据、自动注册的 MCP 服务器与终端 git 配置。",
                            onClick = { GitHubAccountManager.disconnect() },
                        )
                    }
                }
            }

            is GitHubAccountState.Failed -> {
                CouiPreferenceGroup(title = "账号") {
                    item { CouiPreference(title = "连接失败", summary = current.message) }
                    item { CouiCardDivider() }
                    item {
                        CouiPreference(
                            title = "重试",
                            onClick = { GitHubAccountManager.connect() },
                        )
                    }
                }
                CouiPreferenceGroup(title = "说明") {
                    item {
                        CouiPreference(
                            title = "常见原因",
                            summary = "网络不通、设备码过期（15 分钟）、或浏览器里取消了授权。",
                        )
                    }
                }
            }
        }

        CouiPageBottomSpacer()
        }

        AppearanceUiStyle.MIUIX -> MiuixScaffoldPage(title = "GitHub 账号", onBack = onBack) {
            when (val current = state) {
                is GitHubAccountState.Disconnected -> {
                    item(key = "account") {
                        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(top = 8.dp, bottom = 12.dp)) {
                            ArrowPreference(
                                title = "未连接",
                                summary = "用设备码授权，手机上不需要注册回调地址。",
                                onClick = { GitHubAccountManager.connect() },
                            )
                        }
                    }
                    item(key = "scopes") {
                        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                            BasicComponent(
                                title = "授权范围",
                                summary = "repo、read:org、workflow、gist。授权记录会出现在 GitHub " +
                                    "Settings → Applications，可随时撤销。",
                            )
                        }
                    }
                }

                is GitHubAccountState.Connecting -> {
                    item(key = "connecting") {
                        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(top = 8.dp, bottom = 12.dp)) {
                            BasicComponent(title = "正在申请设备码…", summary = "马上会显示一个 8 位验证码。")
                        }
                    }
                }

                is GitHubAccountState.Waiting -> {
                    item(key = "code") {
                        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(top = 8.dp, bottom = 12.dp)) {
                            ArrowPreference(
                                title = current.userCode,
                                summary = "点这里复制",
                                onClick = {
                                    clipboard.setText(AnnotatedString(current.userCode))
                                    Toast.makeText(context, "已复制验证码", Toast.LENGTH_SHORT).show()
                                },
                            )
                            HorizontalDivider()
                            ArrowPreference(
                                title = "打开授权页面",
                                summary = current.verificationUri,
                                onClick = { uriHandler.openUri(current.verificationUri) },
                            )
                        }
                    }
                    item(key = "cancel") {
                        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                            ArrowPreference(title = "取消连接", onClick = { GitHubAccountManager.cancel() })
                        }
                    }
                    item(key = "next-step") {
                        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                            BasicComponent(
                                title = "下一步",
                                summary = "在浏览器里输入上面的验证码并确认，这里会自动完成连接。",
                            )
                        }
                    }
                }

                is GitHubAccountState.Connected -> {
                    item(key = "login") {
                        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(top = 8.dp, bottom = 12.dp)) {
                            BasicComponent(
                                title = current.login,
                                summary = "授权范围：" + current.scopes,
                            )
                        }
                    }
                    item(key = "terminal-sync") {
                        SmallTitle("终端同步")
                        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                            SwitchPreference(
                                title = "同步到终端 git",
                                summary = "写入 ~/.git-credentials 与 .gitconfig，让 Linux 环境里的 git 直接可用。",
                                checked = GitHubAccountManager.syncToTerminalEnabled(),
                                onCheckedChange = { GitHubAccountManager.setSyncToTerminal(it) },
                            )
                            if (current.syncedTerminals.isNotEmpty()) {
                                HorizontalDivider()
                                BasicComponent(
                                    title = "已同步",
                                    summary = current.syncedTerminals.joinToString("、"),
                                )
                            }
                            if (current.pendingTerminals.isNotEmpty()) {
                                HorizontalDivider()
                                BasicComponent(
                                    title = "等待环境就绪",
                                    summary = current.pendingTerminals.joinToString("、") +
                                        "（装完 Linux 环境后回到本页会自动补）",
                                )
                            }
                            current.syncError?.let { error ->
                                HorizontalDivider()
                                BasicComponent(title = "同步失败", summary = error)
                            }
                        }
                    }
                    item(key = "mcp") {
                        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                            BasicComponent(
                                title = "GitHub MCP 服务器",
                                summary = "连接时自动注册，断开时一并删除。工具列表里可直接使用。",
                            )
                        }
                    }
                    item(key = "danger") {
                        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                            ArrowPreference(
                                title = "断开连接",
                                summary = "删除凭据、自动注册的 MCP 服务器与终端 git 配置。",
                                onClick = { GitHubAccountManager.disconnect() },
                            )
                        }
                    }
                }

                is GitHubAccountState.Failed -> {
                    item(key = "failed") {
                        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(top = 8.dp, bottom = 12.dp)) {
                            BasicComponent(title = "连接失败", summary = current.message)
                            HorizontalDivider()
                            ArrowPreference(title = "重试", onClick = { GitHubAccountManager.connect() })
                        }
                    }
                    item(key = "reason") {
                        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                            BasicComponent(
                                title = "常见原因",
                                summary = "网络不通、设备码过期（15 分钟）、或浏览器里取消了授权。",
                            )
                        }
                    }
                }
            }
            item(key = "bottom") { Spacer(Modifier.height(24.dp)) }
        }
    }
}
