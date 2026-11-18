package io.github.mangi.eta.ui.screens.github

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.agent.github.GitHubAccountManager
import io.github.mangi.eta.agent.github.GitHubAccountState
import io.github.mangi.eta.ui.components.coui.CouiPreference
import io.github.mangi.eta.ui.components.coui.CouiScaffoldPage
import io.github.mangi.eta.ui.components.coui.CouiSwitchPreference
import kotlinx.coroutines.launch

/**
 * GitHub 账号：设备码授权，凭据进 Keystore，连接后自动注册 GitHub 官方远程 MCP 并同步给终端 git。
 */
@Composable
internal fun GitHubAccountScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by GitHubAccountManager.state.collectAsState()
    val clipboard = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        GitHubAccountManager.init(context)
        GitHubAccountManager.syncIfConfigured()
    }

    CouiScaffoldPage(title = "GitHub 账号", onBack = onBack) {
        when (val current = state) {
            is GitHubAccountState.Disconnected -> {
                CouiPreference(
                    title = "未连接",
                    summary = "用设备码授权，不需要在手机上注册回调地址。授权范围：repo、read:org、workflow、gist。",
                    onClick = { GitHubAccountManager.connect() },
                )
            }

            is GitHubAccountState.Connecting -> {
                CouiPreference(title = "正在申请设备码…")
            }

            is GitHubAccountState.Waiting -> {
                CouiPreference(
                    title = "验证码 ${current.userCode}",
                    summary = "在浏览器打开 ${current.verificationUri} 并输入上面的码。点这里复制验证码。",
                    onClick = {
                        clipboard.setText(AnnotatedString(current.userCode))
                        Toast.makeText(context, "已复制验证码", Toast.LENGTH_SHORT).show()
                    },
                )
                CouiPreference(
                    title = "打开授权页面",
                    onClick = { uriHandler.openUri(current.verificationUri) },
                )
                CouiPreference(
                    title = "取消",
                    onClick = { GitHubAccountManager.cancel() },
                )
            }

            is GitHubAccountState.Connected -> {
                CouiPreference(
                    title = current.login,
                    summary = "授权范围：${current.scopes}",
                )
                if (current.syncedTerminals.isNotEmpty()) {
                    CouiPreference(
                        title = "终端 git 已同步",
                        summary = current.syncedTerminals.joinToString("、"),
                    )
                }
                if (current.pendingTerminals.isNotEmpty()) {
                    CouiPreference(
                        title = "终端未就绪",
                        summary = current.pendingTerminals.joinToString("、") + "（装完 Linux 环境后回来会自动补）",
                    )
                }
                current.syncError?.let { error ->
                    CouiPreference(title = "同步失败", summary = error)
                }
                CouiSwitchPreference(
                    title = "同步到终端 git",
                    summary = "写入 ~/.git-credentials 与 .gitconfig，让终端里的 git 直接可用。",
                    checked = GitHubAccountManager.syncToTerminalEnabled(),
                    onCheckedChange = { GitHubAccountManager.setSyncToTerminal(it) },
                )
                CouiPreference(
                    title = "断开连接",
                    summary = "同时删除自动注册的 GitHub MCP 服务器与终端凭据。",
                    onClick = { GitHubAccountManager.disconnect() },
                )
            }

            is GitHubAccountState.Failed -> {
                CouiPreference(title = "连接失败", summary = current.message)
                CouiPreference(
                    title = "重试",
                    onClick = { GitHubAccountManager.connect() },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
