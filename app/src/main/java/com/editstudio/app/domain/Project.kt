package com.editstudio.app.domain

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class AspectRatio(val widthRatio: Int, val heightRatio: Int, val displayName: String) {
    PORTRAIT_9_16(9, 16, "9:16 (Reels/Shorts)"),
    LANDSCAPE_16_9(16, 9, "16:9 (YouTube)"),
    SQUARE_1_1(1, 1, "1:1 (Instagram Feed)"),
    VERTICAL_4_5(4, 5, "4:5 (Post Portrait)")
}

@Serializable
enum class VideoResolution(val width: Int, val height: Int, val label: String) {
    HD_720P(720, 1280, "720p HD"),
    FHD_1080P(1080, 1920, "1080p Full HD"),
    UHD_4K(2160, 3840, "4K Ultra HD")
}

@Serializable
enum class FrameRate(val fps: Int, val label: String) {
    FPS_24(24, "24 fps (Cinema)"),
    FPS_30(30, "30 fps (Standard)"),
    FPS_60(60, "60 fps (Smooth)")
}

@Serializable
data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Mera Naya Project",
    val aspectRatio: AspectRatio = AspectRatio.PORTRAIT_9_16,
    val resolution: VideoResolution = VideoResolution.FHD_1080P,
    val frameRate: FrameRate = FrameRate.FPS_30,
    val durationMs: Long = 10000L,
    val layers: List<Layer> = emptyList(),
    val bookmarksMs: List<Long> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val thumbnailUri: String? = null,
    val isFavorite: Boolean = false
) {
    fun widthFor(res: VideoResolution = resolution): Int {
        return when (aspectRatio) {
            AspectRatio.PORTRAIT_9_16 -> (res.height * 9) / 16
            AspectRatio.LANDSCAPE_16_9 -> res.height
            AspectRatio.SQUARE_1_1 -> res.height
            AspectRatio.VERTICAL_4_5 -> (res.height * 4) / 5
        }
    }

    fun heightFor(res: VideoResolution = resolution): Int {
        return res.height
    }
}
