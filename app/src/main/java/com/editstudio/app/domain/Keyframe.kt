package com.editstudio.app.domain

import kotlinx.serialization.Serializable

@Serializable
enum class AnimProperty(val label: String) {
    POSITION_X("X Position"),
    POSITION_Y("Y Position"),
    SCALE_X("Scale X"),
    SCALE_Y("Scale Y"),
    ROTATION("Rotation"),
    ROTATION_X("3D Tilt X"),
    ROTATION_Y("3D Pan Y"),
    OPACITY("Opacity"),
    BLUR("Blur Amount"),
    GLOW("Glow Intensity"),
    COLOR_HUE("Hue Shift"),
    BORDER_WIDTH("Border Width"),
    SHADOW_RADIUS("Shadow Size")
}

@Serializable
enum class EasingType(val label: String) {
    LINEAR("Linear"),
    EASE_IN("Ease In"),
    EASE_OUT("Ease Out"),
    EASE_IN_OUT("Ease In-Out"),
    BOUNCE("Bounce"),
    ELASTIC("Elastic"),
    BACK("Overshoot Back"),
    BEZIER_CUSTOM("Custom Bezier")
}

@Serializable
data class BezierControlPoints(
    val p1x: Float = 0.25f,
    val p1y: Float = 0.1f,
    val p2x: Float = 0.25f,
    val p2y: Float = 1.0f
)

@Serializable
data class Keyframe(
    val timeMs: Long,
    val value: Float,
    val easing: EasingType = EasingType.EASE_IN_OUT,
    val bezier: BezierControlPoints = BezierControlPoints()
)

@Serializable
data class KeyframeTrack(
    val property: AnimProperty,
    val keyframes: List<Keyframe> = emptyList()
)
