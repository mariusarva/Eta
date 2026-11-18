package io.github.mangi.eta.ui.screens.github

import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import com.itos.coui.icons.CouiIcons
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.layout.CouiPageDefaults
import io.github.mangi.eta.agent.github.GitHubAccountManager
import io.github.mangi.eta.agent.github.GitHubAccountState
import io.github.mangi.eta.ui.components.coui.CouiPageBottomSpacer
import io.github.mangi.eta.ui.components.coui.CouiPreference
import io.github.mangi.eta.ui.components.coui.CouiScaffoldPage
import io.github.mangi.eta.ui.components.coui.CouiSettingsIcon
import io.github.mangi.eta.ui.components.coui.CouiSwitchPreference

/**
 * GitHub 账号：设备码授权，凭据进 Keystore，连接后自动注册 GitHub 官方远程 MCP 并同步给终端 git。
 *
 * 页面按 COUI 的分组式布局组织：状态 / 授权 / 终端同步 / 危险操作，各段独立成组。
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

    CouiScaffoldPage(title = "GitHub 账号", onBack = onBack, scrollState = scrollState) {
        when (val current = state) {
            is GitHubAccountState.Disconnected -> {
                Group(title = "账号") {
                    CouiPreference(
                        title = "未连接",
                        summary = "用设备码授权，手机上不需要注册回调地址。",
                        icon = { CouiSettingsIcon(CouiIcons.Filled.UserCircle, CouiPageDefaults.themeColors().accent) },
                        onClick = { GitHubAccountManager.connect() },
                    )
                }
                Group(title = "说明") {
                    CouiPreference(
                        title = "授权范围",
                        summary = "repo、read:org、workflow、gist。授权记录会出现在 GitHub " +
                            "Settings → Applications，可随时撤销。",
                    )
                }
            }

            is GitHubAccountState.Connecting -> {
                Group(title = "账号") {
                    CouiPreference(
                        title = "正在申请设备码…",
                        summary = "马上会显示一个 8 位验证码。",
                    )
                }
            }

            is GitHubAccountState.Waiting -> {
                Group(title = "验证码") {
                    CouiPreference(
                        title = current.userCode,
                        summary = "点这里复制",
                        onClick = {
                            clipboard.setText(AnnotatedString(current.userCode))
                            Toast.makeText(context, "已复制验证码", Toast.LENGTH_SHORT).show()
                        },
                    )
                    Divider()
                    CouiPreference(
                        title = "打开授权页面",
                        summary = current.verificationUri,
                        onClick = { uriHandler.openUri(current.verificationUri) },
                    )
                }
                Group(title = "操作") {
                    CouiPreference(
                        title = "取消连接",
                        onClick = { GitHubAccountManager.cancel() },
                    )
                }
                Group(title = "说明") {
                    CouiPreference(
                        title = "下一步",
                        summary = "在浏览器里输入上面的验证码并确认，这里会自动完成连接。",
                    )
                }
            }

            is GitHubAccountState.Connected -> {
                Group(title = "账号") {
                    CouiPreference(
                        title = current.login,
                        summary = "授权范围：${current.scopes}",
                        icon = { CouiSettingsIcon(CouiIcons.Filled.UserCircle, CouiPageDefaults.themeColors().accent) },
                    )
                }

                Group(title = "终端同步") {
                    CouiSwitchPreference(
                        title = "同步到终端 git",
                        summary = "写入 ~/.git-credentials 与 .gitconfig，让 Linux 环境里的 git 直接可用。",
                        checked = GitHubAccountManager.syncToTerminalEnabled(),
                        onCheckedChange = { GitHubAccountManager.setSyncToTerminal(it) },
                    )
                    if (current.syncedTerminals.isNotEmpty()) {
                        Divider()
                        CouiPreference(
                            title = "已同步",
                            summary = current.syncedTerminals.joinToString("、"),
                        )
                    }
                    if (current.pendingTerminals.isNotEmpty()) {
                        Divider()
                        CouiPreference(
                            title = "等待环境就绪",
                            summary = current.pendingTerminals.joinToString("、") +
                                "（装完 Linux 环境后回到本页会自动补）",
                        )
                    }
                    current.syncError?.let { error ->
                        Divider()
                        CouiPreference(title = "同步失败", summary = error)
                    }
                }

                Group(title = "MCP") {
                    CouiPreference(
                        title = "GitHub MCP 服务器",
                        summary = "连接时自动注册，断开时一并删除。工具列表里可直接使用。",
                    )
                }

                Group(title = "危险操作") {
                    CouiPreference(
                        title = "断开连接",
                        summary = "删除凭据、自动注册的 MCP 服务器与终端 git 配置。",
                        onClick = { GitHubAccountManager.disconnect() },
                    )
                }
            }

            is GitHubAccountState.Failed -> {
                Group(title = "账号") {
                    CouiPreference(title = "连接失败", summary = current.message)
                    Divider()
                    CouiPreference(
                        title = "重试",
                        onClick = { GitHubAccountManager.connect() },
                    )
                }
                Group(title = "说明") {
                    CouiPreference(
                        title = "常见原因",
                        summary = "网络不通、设备码过期（15 分钟）、或浏览器里取消了授权。",
                    )
                }
            }
        }

        CouiPageBottomSpacer()
    }
}
