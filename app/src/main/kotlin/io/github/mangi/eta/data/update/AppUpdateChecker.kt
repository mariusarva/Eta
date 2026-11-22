package io.github.mangi.eta.data.update

import io.github.mangi.eta.R
import io.github.mangi.eta.core.AppStrings
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/** GitHub 最新 Release 信息，[version] 已去掉 tag 的 v 前缀。 */
internal data class AppLatestRelease(
    val version: String,
    val url: String,
)

/**
 * 应用更新检查：读取 GitHub 最新 Release 并与当前版本比较。
 *
 * 版本号遵循发版流程的 v 前缀 + 点分数字约定（v3.0.6），比较按段进行：两段都是数字时
 * 按数值比较，否则按字符串比较。当前版本的构建类型后缀（-debug）与 CI 构建元数据（+提交号）不参与比较。
 * 所有网络与响应错误统一抛 [IOException]，由调用方提示。
 */
internal object AppUpdateChecker {

    fun fetchLatest(): AppLatestRelease {
        val request = Request.Builder()
            .url(LATEST_RELEASE_URL)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", USER_AGENT)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException(
                    AppStrings.get(
                        R.string.update_err_github_release_http,
                        "GitHub Release 请求失败（HTTP %1\$d）",
                        response.code,
                    ),
                )
            }
            val json = runCatching { JSONObject(response.body.string()) }
                .getOrElse {
                    throw IOException(
                        AppStrings.get(
                            R.string.update_err_github_release_invalid_json,
                            "GitHub Release 响应不是有效 JSON",
                        ),
                        it,
                    )
                }
            val tag = json.optString("tag_name").trim()
            val url = json.optString("html_url").trim()
            if (tag.isEmpty() || url.isEmpty()) {
                throw IOException(
                    AppStrings.get(
                        R.string.update_err_github_release_missing_fields,
                        "GitHub Release 响应缺少 tag_name/html_url",
                    ),
                )
            }
            return AppLatestRelease(version = tag.removePrefix("v"), url = url)
        }
    }

    /**
     * 只保留版本号前三段纯数字，去掉一切非数字后缀再比较。
     *
     * 本 fork 的 tag 带 `-fork.N` 序号（如 `v3.4.4-fork.28`），而 versionName 也是
     * `3.4.4-fork.28+提交号`。若把后缀带进比较，`4-fork` 会被当成大于 `4`，
     * 导致每次检查都误报有新版本。发版序号不属于上游版本语义，统一剥离。
     */
    private fun versionCore(version: String): List<String> =
        version.removePrefix("v")
            .substringBefore('+')
            .substringBefore('-')
            .split('.')
            .map { part -> part.takeWhile(Char::isDigit).ifBlank { part } }

    fun isNewer(latest: String, current: String): Boolean {
        val latestParts = versionCore(latest)
        val currentParts = versionCore(current)
        for (index in 0 until maxOf(latestParts.size, currentParts.size)) {
            val latestPart = latestParts.getOrNull(index) ?: "0"
            val currentPart = currentParts.getOrNull(index) ?: "0"
            val latestNumber = latestPart.toIntOrNull()
            val currentNumber = currentPart.toIntOrNull()
            if (latestNumber != null && currentNumber != null) {
                if (latestNumber != currentNumber) return latestNumber > currentNumber
            } else {
                val compared = latestPart.compareTo(currentPart)
                if (compared != 0) return compared > 0
            }
        }
        // 上游版本相同：本 fork 的 `-fork.N` 序号参与比较，否则自己的修复版永远提示不了更新。
        return forkSequence(latest) > forkSequence(current)
    }

    /** 取 `-fork.N` 里的 N；没有 fork 后缀返回 0（纯上游版视为 fork.0）。 */
    private fun forkSequence(version: String): Int =
        Regex("fork[.-](\\d+)")
            .find(version.substringBefore('+'))
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: 0

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private const val LATEST_RELEASE_URL = "https://api.github.com/repos/mariusarva/Eta/releases/latest"
    private const val USER_AGENT = "Eta-Update-Checker"
}
