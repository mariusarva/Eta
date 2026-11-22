// SPDX-License-Identifier: Apache-2.0
package io.github.mangi.eta.agent.voice.local

import io.github.mangi.eta.agent.voice.SpeechErrorCode
import io.github.mangi.eta.agent.voice.SpeechFailure
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI

/** User-initiated model fetch only. No runtime discovery, unpinned branches, or recognition uploads. */
internal object LocalSpeechDownload {
    /**
     * 镜像仓库：本机网络访问不到 huggingface.co，同一批固定版本文件已由 CI 下载、
     * 按 catalog 的 sha256 校验后发布到本项目 Release。先走镜像，失败再回退官方地址。
     */
    private const val MIRROR_BASE = "https://github.com/mariusarva/Eta/releases/download/local-models-v1"

    /** 连续多少次「一点没下到」才放弃当前源；只要有进展就一直续传（该网络会在中途断流）。 */
    private const val MAX_STALLS = 3

    /** 5 秒读超时会让百 MB 下载在抖动时反复中断，改成 30 秒。 */
    private const val READ_TIMEOUT_MS = 30_000

    internal fun mirrorUrl(file: LocalSpeechModelFile): String? {
        if (!file.name.matches(Regex("[A-Za-z0-9_.-]{1,128}"))) return null
        return "$MIRROR_BASE/${file.name}"
    }

    suspend fun fetch(model: LocalSpeechModelDescriptor, stage: File, progress: (LocalSpeechProgress) -> Unit) {
        var completed = 0L
        progress(LocalSpeechProgress(LocalSpeechStage.DOWNLOADING, 0, model.sizeBytes))
        for (file in model.archive?.let { listOf(it) } ?: model.files) {
            currentCoroutineContext().ensureActive()
            val dest = File(stage, file.name)
            if (dest.isFile && dest.length() == file.sizeBytes) {
                // 上次中断前这一份已经完整（续传）。
                finishFile(model, stage, file, dest, progress)
                completed += file.sizeBytes
                progress(LocalSpeechProgress(LocalSpeechStage.DOWNLOADING, completed, model.sizeBytes))
                continue
            }
            val candidates = buildList {
                // Vosk 走 alphacephei（可直连），其余优先 GitHub 镜像，最后回退 huggingface。
                if (model.engine != LocalSpeechBackend.VOSK) mirrorUrl(file)?.let { add(URI(it)) }
                add(URI(file.pinnedUrl))
            }
            var failure: Exception? = null
            var fetched = false
            for (target in candidates) {
                if (fetched) break
                var stalls = 0
                var highWater = -1L
                while (stalls < MAX_STALLS) {
                    currentCoroutineContext().ensureActive()
                    var start = if (dest.isFile) dest.length() else 0L
                    if (start > file.sizeBytes) { dest.delete(); start = 0L }
                    // 有前进就重置计数：这条链路会在 100～200 MB 处断流，但每次续传都往前推。
                    if (start > highWater) { highWater = start; stalls = 0 } else stalls++
                    if (stalls >= MAX_STALLS) break
                    try {
                        attemptDownload(model, target, dest, file, start) { written ->
                            // copyResume 的 written 已包含续传起点。
                            progress(LocalSpeechProgress(LocalSpeechStage.DOWNLOADING,
                                completed + written, model.sizeBytes))
                        }
                        fetched = true
                        break
                    } catch (e: CancellationException) { throw e
                    } catch (e: IllegalArgumentException) { throw e
                    } catch (e: SpeechFailure) { failure = e
                    } catch (_: java.net.SocketTimeoutException) {
                        failure = SpeechFailure(SpeechErrorCode.TIMEOUT, "模型下载超时，已保留进度，可重新点下载继续；已有模型不受影响")
                    } catch (_: java.io.IOException) {
                        failure = SpeechFailure(SpeechErrorCode.NETWORK, "模型下载中断，已保留进度，可重新点下载继续；已有模型不受影响")
                    }
                    delay(1_000L)
                }
            }
            if (!fetched) throw failure ?: SpeechFailure(SpeechErrorCode.NETWORK, "模型下载失败，请检查网络后重试")
            finishFile(model, stage, file, dest, progress)
            completed += file.sizeBytes
        }
        progress(LocalSpeechProgress(LocalSpeechStage.DOWNLOADING, model.sizeBytes, model.sizeBytes))
    }

    /** Vosk 需要就地解压并删除压缩包；其余格式下载完即完成。 */
    private suspend fun finishFile(model: LocalSpeechModelDescriptor, stage: File, file: LocalSpeechModelFile,
        dest: File, progress: (LocalSpeechProgress) -> Unit) {
        if (model.engine != LocalSpeechBackend.VOSK) return
        currentCoroutineContext().ensureActive()
        LocalSpeechVoskArchive.unpack(model, stage, dest, progress)
        check(dest.delete()) { "无法清理临时模型压缩包" }
    }

    /**
     * 一次 HTTP 尝试。`start` 是已落盘字节数，用 Range 续传；服务器忽略 Range 时从头重写。
     */
    private suspend fun attemptDownload(model: LocalSpeechModelDescriptor, target: URI, dest: File,
        file: LocalSpeechModelFile, start: Long, progress: (Long) -> Unit) {
        var url = target
        var connection: HttpURLConnection? = null
        try {
            for (redirect in 0..8) {
                currentCoroutineContext().ensureActive()
                require(allowedUrl(model, url)) { "模型下载重定向不安全" }
                val c = (url.toURL().openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false; connectTimeout = 20_000; readTimeout = READ_TIMEOUT_MS
                    setRequestProperty("Accept-Encoding", "identity")
                    if (start > 0) setRequestProperty("Range", "bytes=$start-")
                }
                connection = c
                val status = c.responseCode
                if (status in setOf(301, 302, 303, 307, 308)) {
                    require(redirect < 8) { "模型下载重定向过多" }
                    val location = c.getHeaderField("Location") ?: error("模型下载缺少地址")
                    url = url.resolve(location); c.disconnect(); connection = null
                    continue
                }
                if (status == 416) {
                    // 续传位置越界：本地残片不可信，清掉让下一次从头开始。
                    c.disconnect(); connection = null
                    dest.delete()
                    throw SpeechFailure(SpeechErrorCode.NETWORK, "续传位置无效，已重置，请重新点下载")
                }
                if (status != 200 && status != 206) {
                    throw SpeechFailure(SpeechErrorCode.NETWORK, "模型下载失败（HTTP $status），请检查网络后重试")
                }
                val resumeFrom = if (status == 206) start else 0L
                if (status == 200 && start > 0) dest.delete()
                val remaining = file.sizeBytes - resumeFrom
                val length = c.contentLengthLong
                // 长度声明只用于提前发现明显不对的响应；真正判定在复制之后按已落盘字节数做，
                // 因为 CDN 可能不回 Content-Length 或给的是压缩前长度。
                require(length == -1L || length <= remaining) { "下载文件大小与固定版本不匹配" }
                c.inputStream.use { input -> copyResume(input, dest, file.sizeBytes, resumeFrom, progress) }
                val written = dest.length()
                if (written > file.sizeBytes) {
                    dest.delete()
                    throw SpeechFailure(SpeechErrorCode.PROTOCOL, "下载内容超出模型大小，已丢弃，请重新下载")
                }
                // 连接正常关闭但字节不够属于中断，抛 IOException 交给外层续传，不要当成格式错误。
                if (written < file.sizeBytes) throw java.io.IOException("模型下载提前结束")
                return
            }
            throw SpeechFailure(SpeechErrorCode.NETWORK, "模型下载重定向过多")
        } finally { connection?.disconnect() }
    }

    internal fun allowedUrl(model: LocalSpeechModelDescriptor, url: URI): Boolean {
        if (model.engine == LocalSpeechBackend.VOSK) {
            // No arbitrary official-host files, query strings, credentials, alternate ports, or CDN redirects.
            return url.toASCIIString() == LocalSpeechRegistry.voskUrl(model.id)
        }
        val host = url.host.orEmpty()
        val https = url.scheme == "https" && url.userInfo == null && (url.port == -1 || url.port == 443)
        if (!https) return false
        // 镜像与 GitHub Release 资产重定向目标。
        if (host == "github.com" && url.toASCIIString().startsWith(MIRROR_BASE + "/")) return true
        if (host == "objects.githubusercontent.com" || host == "release-assets.githubusercontent.com") return true
        return host == "huggingface.co" || host.endsWith(".huggingface.co") || host.endsWith(".hf.co")
    }

    /** 导入 / 解压路径的一次性写入（不续传），可选流式 SHA-256 校验。 */
    internal suspend fun copyExact(input: InputStream, output: File, expected: Long, expectedSha256: String? = null,
        progress: (Long) -> Unit) {
        require(!output.exists()) { "临时文件重复" }
        val coroutine = currentCoroutineContext(); val buffer = ByteArray(64 * 1024); var bytes = 0L
        val digest = expectedSha256?.let { java.security.MessageDigest.getInstance("SHA-256") }
        val stream = try { output.outputStream() } catch (_: java.io.IOException) {
            throw SpeechFailure(SpeechErrorCode.STORAGE, "无法创建模型临时文件，请检查存储空间和权限")
        }
        stream.use { sink ->
            while (true) {
                coroutine.ensureActive()
                val n = input.read(buffer, 0, minOf(buffer.size.toLong(), expected - bytes + 1L).toInt())
                if (n == -1) break
                require(n > 0 && n.toLong() <= expected - bytes) { "文件超出目录大小，已拒绝导入" }
                try { sink.write(buffer, 0, n) } catch (_: java.io.IOException) {
                    throw SpeechFailure(SpeechErrorCode.STORAGE, "模型写入失败，请释放存储空间后重试；已有文件保留")
                }
                digest?.update(buffer, 0, n)
                bytes += n; progress(bytes)
            }
            require(bytes == expected) { "文件不完整或不属于所选模型" }
            if (digest != null) require(digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) } == expectedSha256) {
                "模型写入流 SHA-256 不匹配"
            }
            try { sink.fd.sync() } catch (_: java.io.IOException) {
                throw SpeechFailure(SpeechErrorCode.STORAGE, "模型文件同步失败，请检查存储空间后重试")
            }
        }
    }

    /**
     * 从 `resumeFrom` 起写满 [expectedTotal] 字节。已存在且长度一致的前缀直接复用（断点续传），
     * 最终完整性由安装阶段的 SHA-256 校验兜底。
     */
    internal suspend fun copyResume(input: InputStream, output: File, expectedTotal: Long, resumeFrom: Long,
        progress: (Long) -> Unit) {
        val coroutine = currentCoroutineContext(); val buffer = ByteArray(64 * 1024)
        var bytes = resumeFrom
        val append = resumeFrom > 0 && output.isFile && output.length() == resumeFrom
        if (!append) { bytes = 0L; output.delete() }
        val stream = try {
            if (append) FileOutputStream(output, true) else output.outputStream()
        } catch (_: java.io.IOException) {
            throw SpeechFailure(SpeechErrorCode.STORAGE, "无法创建模型临时文件，请检查存储空间和权限")
        }
        stream.use { sink ->
            while (true) {
                coroutine.ensureActive()
                val room = expectedTotal - bytes
                if (room <= 0) break
                val n = input.read(buffer, 0, minOf(buffer.size.toLong(), room).toInt())
                if (n == -1) break
                require(n > 0) { "文件超出目录大小，已拒绝导入" }
                try { sink.write(buffer, 0, n) } catch (_: java.io.IOException) {
                    throw SpeechFailure(SpeechErrorCode.STORAGE, "模型写入失败，请释放存储空间后重试；已有文件保留")
                }
                bytes += n; progress(bytes)
            }
            try { sink.fd.sync() } catch (_: java.io.IOException) {
                throw SpeechFailure(SpeechErrorCode.STORAGE, "模型文件同步失败，请检查存储空间后重试")
            }
        }
    }
}