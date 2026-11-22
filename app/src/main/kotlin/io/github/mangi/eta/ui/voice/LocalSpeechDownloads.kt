package io.github.mangi.eta.ui.voice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * 模型下载不绑定设置页生命周期。
 *
 * 253 MB 的 GGUF 在这类网络下要几分钟，且会在中途断流；用 `rememberCoroutineScope()` 跑下载
 * 时，只要离开设置页或切到后台就被取消，用户回来看到的是「未安装」。放到进程级 scope 后
 * 页面销毁不影响下载，进程被冻结/回收时由 stage 目录的断点续传接着来。
 *
 * 用户主动点「取消」仍然生效：取消的是页面上那个 Job 引用。
 */
internal object LocalSpeechDownloads {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
}