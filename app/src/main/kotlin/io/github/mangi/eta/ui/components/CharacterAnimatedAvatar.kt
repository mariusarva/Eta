package io.github.mangi.eta.ui.components

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.view.Gravity
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import io.github.mangi.eta.agent.roleplay.CharacterAvatarKind
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 动图 / 视频头像的真实播放层。
 *
 * 静态帧统一走 [CharacterAvatar] 的位图路径（含视频首帧），只有明确要求动起来的位置
 * 才用这里：列表滚动时一屏可能十几张卡，每张都起一个 MediaPlayer 会拖垮滚动帧率，
 * 因此播放只在角色详情主视觉这类单实例位置开启。
 */
@Composable
internal fun CharacterAnimatedAvatar(
    path: String?,
    kind: CharacterAvatarKind,
    shape: Shape,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit,
) {
    if (path == null || !kind.isAnimated) {
        fallback()
        return
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (kind.isVideo) {
            VideoAvatar(path = path, shape = shape, modifier = Modifier.fillMaxSize(), fallback = fallback)
        } else {
            AnimatedImageAvatar(path = path, shape = shape, modifier = Modifier.fillMaxSize(), fallback = fallback)
        }
    }
}

/** GIF 与动图 WebP：ImageDecoder 直接给出 AnimatedImageDrawable，交给 ImageView 自行驱动重绘。 */
@Composable
private fun AnimatedImageAvatar(
    path: String,
    shape: Shape,
    modifier: Modifier,
    fallback: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val drawable by produceState<Drawable?>(initialValue = null, path) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    ImageDecoder.decodeDrawable(ImageDecoder.createSource(File(path)))
                } else {
                    null
                }
            }.getOrNull()
        }
    }
    val current = drawable
    if (current == null) {
        fallback()
        return
    }
    // 圆角/方角裁剪沿用调用方形状：AndroidView 不吃 Compose clip，用外层 Box 的 clip 兜住。
    Box(modifier = modifier.clip(shape)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                ImageView(ctx).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            },
            update = { view ->
                view.setImageDrawable(current)
                (current as? AnimatedImageDrawable)?.apply {
                    repeatCount = AnimatedImageDrawable.REPEAT_INFINITE
                    if (!isRunning) start()
                }
            },
        )
    }
}

/**
 * 视频头像：TextureView + MediaPlayer，静音、循环、自动播放。
 *
 * 只用于单实例的大尺寸位置。TextureView 在不可见时会持续解码，因此离开组合即释放播放器；
 * 播放失败（编码不支持、文件损坏）时回落到首帧位图，不留下黑块。
 */
@Composable
private fun VideoAvatar(
    path: String,
    shape: Shape,
    modifier: Modifier,
    fallback: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var failed by remember(path) { mutableStateOf(false) }
    if (failed) {
        fallback()
        return
    }
    val player = remember(path) { MediaPlayer() }
    var textureView by remember(path) { mutableStateOf<TextureView?>(null) }
    DisposableEffect(path) {
        onDispose {
            runCatching { if (player.isPlaying) player.stop() }
            runCatching { player.release() }
        }
    }
    Box(modifier = modifier.clip(shape)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                TextureView(ctx).also { view ->
                    textureView = view
                    view.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
                            runCatching {
                                player.reset()
                                player.setSurface(android.view.Surface(surface))
                                player.setDataSource(path)
                                player.isLooping = true
                                player.setVolume(0f, 0f)
                                player.setOnPreparedListener { it.start() }
                                player.setOnErrorListener { _, _, _ ->
                                    failed = true
                                    true
                                }
                                player.prepareAsync()
                            }.onFailure { failed = true }
                        }

                        override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) = Unit
                        override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean = true
                        override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) = Unit
                    }
                }
            },
        )
    }
}

/**
 * 视频首帧位图：列表与消息头像都用它，避免为缩略图起播放器。
 * 取第 0 帧而不是中间帧，人物正脸通常出现在开头。
 */
internal suspend fun decodeVideoFrame(path: String, maxPx: Int): Bitmap? = withContext(Dispatchers.IO) {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(path)
        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
        if (width <= 0 || height <= 0) return@withContext null
        // 按目标像素等比取整，避免为 40dp 头像解出 4K 帧。
        val sample = maxOf(1, minOf(width, height) / maxPx.coerceAtLeast(1))
        val frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC) ?: return@withContext null
        if (sample <= 1) frame else Bitmap.createScaledBitmap(
            frame,
            (frame.width / sample).coerceAtLeast(1),
            (frame.height / sample).coerceAtLeast(1),
            true,
        )
    } catch (_: Throwable) {
        null
    } finally {
        runCatching { retriever.release() }
    }
}

/** 供缓存层使用：把首帧包成 ImageBitmap；失败返回 null 由调用方回落字标。 */
internal suspend fun decodeVideoFrameBitmap(path: String, maxPx: Int) =
    decodeVideoFrame(path, maxPx)?.asImageBitmap()
