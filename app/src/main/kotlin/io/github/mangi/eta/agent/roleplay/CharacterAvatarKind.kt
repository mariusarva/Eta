package io.github.mangi.eta.agent.roleplay

/**
 * 角色头像的媒体类型。
 *
 * 头像不再限定 PNG：动图（GIF / 动图 WebP）与视频（MP4 / WebM）都按原文件保存，
 * 只有静态位图会规整成 PNG（PNG 角色卡导出需要一张真正的 PNG 底图）。
 * 类型只按文件头魔数判定，不看扩展名——用户从相册选的文件扩展名经常不可靠。
 */
internal enum class CharacterAvatarKind {
    PNG,
    JPEG,
    WEBP,
    GIF,
    MP4,
    WEBM;

    /** 是否需要用动画播放（动图或视频）。 */
    val isAnimated: Boolean get() = this == GIF || this == WEBP || this == MP4 || this == WEBM

    /** 视频类需要走 MediaPlayer / 抽帧，不能交给 ImageDecoder。 */
    val isVideo: Boolean get() = this == MP4 || this == WEBM

    /** 落盘扩展名；PNG 是静态位图的统一容器。 */
    val extension: String
        get() = when (this) {
            PNG -> "png"
            JPEG -> "jpg"
            WEBP -> "webp"
            GIF -> "gif"
            MP4 -> "mp4"
            WEBM -> "webm"
        }

    companion object {
        private val PNG_SIGNATURE = byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10)

        /**
         * 按魔数判定类型；无法识别返回 null。
         *
         * 动图 WebP 与静态 WebP 都是 `RIFF....WEBP`，这里只认 WEBP，
         * 是否真的会动交给 ImageDecoder 判断（静态图解码成普通 Drawable 也不出错）。
         */
        fun detect(bytes: ByteArray): CharacterAvatarKind? {
            if (bytes.size >= 8 && bytes.copyOfRange(0, 8).contentEquals(PNG_SIGNATURE)) return PNG
            if (bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) {
                return JPEG
            }
            if (bytes.size >= 12 &&
                ascii(bytes, 0, 4) == "RIFF" &&
                ascii(bytes, 8, 4) == "WEBP"
            ) {
                return WEBP
            }
            if (bytes.size >= 6 && ascii(bytes, 0, 3) == "GIF") return GIF
            if (bytes.size >= 12 && ascii(bytes, 4, 4) == "ftyp") {
                val brand = ascii(bytes, 8, 4).lowercase()
                return when {
                    brand.startsWith("qt") -> MP4
                    brand.startsWith("isom") || brand.startsWith("mp4") || brand.startsWith("iso2") ||
                        brand.startsWith("avc1") || brand.startsWith("mmp4") || brand.startsWith("M4V") -> MP4
                    else -> MP4
                }
            }
            if (bytes.size >= 4 && bytes[0] == 0x1A.toByte() && bytes[1] == 0x45.toByte() &&
                bytes[2] == 0xDF.toByte() && bytes[3] == 0xA3.toByte()
            ) {
                return WEBM
            }
            return null
        }

        /** 文件路径推断类型；路径可能来自旧版本（固定 .png），因此仍以魔数为主。 */
        fun fromPath(path: String?): CharacterAvatarKind? {
            if (path == null) return null
            val file = java.io.File(path)
            if (!file.isFile) return null
            val header = ByteArray(16)
            val read = runCatching {
                file.inputStream().use { it.read(header) }
            }.getOrDefault(-1)
            if (read <= 0) return null
            return detect(header.copyOf(read))
        }

        private fun ascii(bytes: ByteArray, offset: Int, length: Int): String =
            bytes.copyOfRange(offset, minOf(offset + length, bytes.size)).toString(Charsets.ISO_8859_1)
    }
}
