package com.editstudio.app.domain

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class LayerType(val displayName: String) {
    VIDEO("Video"),
    IMAGE("Image"),
    AUDIO("Audio Track"),
    TEXT("Text"),
    SHAPE("Shape"),
    DRAWING("Drawing"),
    SOLID("Solid Color"),
    GRADIENT("Gradient Fill"),
    GROUP("Group"),
    ADJUSTMENT("Adjustment"),
    CAMERA("3D Camera")
}

@Serializable
enum class BlendMode(val displayName: String) {
    NORMAL("Normal"),
    MULTIPLY("Multiply"),
    SCREEN("Screen"),
    OVERLAY("Overlay"),
    ADD("Add / Linear Dodge"),
    SOFT_LIGHT("Soft Light"),
    DIFFERENCE("Difference"),
    DARKEN("Darken"),
    LIGHTEN("Lighten"),
    COLOR_DODGE("Color Dodge"),
    COLOR_BURN("Color Burn")
}

@Serializable
data class Transform(
    val x: Float = 0f,
    val y: Float = 0f,
    val scaleX: Float = 1.0f,
    val scaleY: Float = 1.0f,
    val rotationZ: Float = 0f,
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val opacity: Float = 1.0f,
    val anchorX: Float = 0.5f,
    val anchorY: Float = 0.5f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 0f,
    val cropBottom: Float = 0f
)

@Serializable
enum class ShapeType(val label: String) {
    CIRCLE("Circle"),
    SQUARE("Square"),
    ROUNDED_SQUARE("Rounded Square"),
    RECTANGLE("Rounded Rect"),
    PLUS("Plus (+)"),
    RING("Ring"),
    TRIANGLE("Triangle"),
    PIE("Pie Slice"),
    HEXAGON("Hexagon"),
    STAR("Star"),
    ARROW("Arrow"),
    LINE("Line"),
    HEART("Heart"),
    POLYGON("Polygon"),
    SPEECH_BUBBLE("Speech Bubble"),
    LOWER_THIRD("Lower Third"),
    PROGRESS_BAR("Progress Bar"),
    COUNTER("Counter"),
    CHART_BAR("Bar Chart"),
    CHART_LINE("Line Chart"),
    CHART_PIE("Pie Chart")
}

@Serializable
data class ShapeContent(
    val shapeType: ShapeType = ShapeType.ROUNDED_SQUARE,
    val fillColor: Long = 0xFF38BDF8,
    val strokeColor: Long = 0xFF14B8A6,
    val strokeWidth: Float = 4f,
    val cornerRadius: Float = 16f,
    val shadowRadius: Float = 0f,
    val shadowColor: Long = 0xAA000000,
    val sides: Int = 6,
    val trimPathStart: Float = 0f,
    val trimPathEnd: Float = 1.0f,
    val counterValue: Int = 100,
    val counterPrefix: String = "",
    val counterSuffix: String = "%",
    val chartData: List<Float> = listOf(30f, 65f, 45f, 90f, 75f),
    val chartLabels: List<String> = listOf("Jan", "Feb", "Mar", "Apr", "May")
)

@Serializable
data class TextContent(
    val text: String = "Apna Text Yahan Likhein",
    val fontName: String = "SansSerif",
    val fontSizeSp: Float = 36f,
    val textColor: Long = 0xFFFFFFFF,
    val backgroundColor: Long = 0x00000000,
    val outlineColor: Long = 0xFF38BDF8,
    val outlineWidth: Float = 0f,
    val shadowColor: Long = 0x88000000,
    val shadowRadius: Float = 4f,
    val isBold: Boolean = true,
    val isItalic: Boolean = false,
    val alignment: TextAlignment = TextAlignment.CENTER,
    val letterSpacing: Float = 0f,
    val lineSpacing: Float = 1.2f,
    val karaokeProgress: Float = 0f
)

@Serializable
enum class TextAlignment {
    LEFT, CENTER, RIGHT
}

@Serializable
data class MaskConfig(
    val isEnabled: Boolean = false,
    val isCircle: Boolean = true,
    val featherPx: Float = 0f,
    val isInverted: Boolean = false,
    val width: Float = 300f,
    val height: Float = 300f,
    val centerX: Float = 0.5f,
    val centerY: Float = 0.5f
)

@Serializable
data class DrawingStroke(
    val points: List<PathPoint> = emptyList(),
    val color: Long = 0xFF38BDF8,
    val strokeWidth: Float = 8f,
    val isVectorMode: Boolean = false
)

@Serializable
data class DrawingContent(
    val strokes: List<DrawingStroke> = emptyList(),
    val activeColor: Long = 0xFF38BDF8,
    val activeWidth: Float = 8f,
    val isVectorMode: Boolean = false
)

@Serializable
data class Layer(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Layer",
    val type: LayerType = LayerType.TEXT,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val startTimeMs: Long = 0L,
    val durationMs: Long = 5000L,
    val mediaUri: String? = null,
    val parentLayerId: String? = null,
    val blendMode: BlendMode = BlendMode.NORMAL,
    val transform: Transform = Transform(),
    val textContent: TextContent? = null,
    val shapeContent: ShapeContent? = null,
    val drawingContent: DrawingContent? = null,
    val solidColor: Long = 0xFF18233C,
    val gradientColors: List<Long> = listOf(0xFF3B82F6, 0xFF14B8A6),
    val borderColor: Long = 0xFF38BDF8,
    val borderWidth: Float = 0f,
    val shadowRadius: Float = 0f,
    val shadowColor: Long = 0x88000000,
    val effects: List<LayerEffect> = emptyList(),
    val motionPresets: List<MotionPresetConfig> = emptyList(),
    val keyframeTracks: List<KeyframeTrack> = emptyList(),
    val speedRamp: SpeedRampConfig = SpeedRampConfig(),
    val motionBlur: MotionBlurConfig = MotionBlurConfig(),
    val pathAnimation: PathAnimationConfig = PathAnimationConfig(),
    val wiggle: WiggleConfig = WiggleConfig(),
    val stagger: StaggerConfig = StaggerConfig(),
    val camera3D: Camera3DConfig = Camera3DConfig(),
    val particles: ParticleConfig = ParticleConfig(),
    val mask: MaskConfig = MaskConfig(),
    val audioReactive: AudioReactiveConfig = AudioReactiveConfig(),
    val volume: Float = 1.0f,
    val zIndex: Int = 0
) {
    val endTimeMs: Long get() = startTimeMs + durationMs

    fun containsTime(timeMs: Long): Boolean {
        return timeMs >= startTimeMs && timeMs < endTimeMs
    }
}
