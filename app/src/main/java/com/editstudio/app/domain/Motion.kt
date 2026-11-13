package com.editstudio.app.domain

import kotlinx.serialization.Serializable

@Serializable
enum class MotionGroup(val displayName: String) {
    BASIC("Basic Motion"),
    CINEMATIC("Cinematic & Camera"),
    REVEAL("Mask & Reveal"),
    GLITCH("Glitch & VHS"),
    SOCIAL_REELS("Viral Reels & Shorts")
}

@Serializable
enum class MotionPhase(val label: String) {
    IN("Intro (In)"),
    OUT("Outro (Out)"),
    LOOP("Continuous Loop")
}

@Serializable
enum class MotionPresetType(
    val id: String,
    val title: String,
    val group: MotionGroup,
    val phase: MotionPhase,
    val defaultDurationMs: Long = 800L
) {
    FADE_IN("fade_in", "Fade In", MotionGroup.BASIC, MotionPhase.IN),
    SLIDE_UP("slide_up", "Slide Up", MotionGroup.BASIC, MotionPhase.IN),
    SLIDE_DOWN("slide_down", "Slide Down", MotionGroup.BASIC, MotionPhase.IN),
    SLIDE_LEFT("slide_left", "Slide Left", MotionGroup.BASIC, MotionPhase.IN),
    SLIDE_RIGHT("slide_right", "Slide Right", MotionGroup.BASIC, MotionPhase.IN),
    ZOOM_IN("zoom_in", "Zoom In", MotionGroup.BASIC, MotionPhase.IN),
    ZOOM_OUT("zoom_out", "Zoom Out", MotionGroup.BASIC, MotionPhase.IN),
    SPIN_IN("spin_in", "Spin In", MotionGroup.BASIC, MotionPhase.IN),
    FLIP_IN("flip_in", "3D Card Flip", MotionGroup.BASIC, MotionPhase.IN),
    BOUNCE_IN("bounce_in", "Bounce Drop", MotionGroup.BASIC, MotionPhase.IN),
    POP_IN("pop_in", "Elastic Pop", MotionGroup.BASIC, MotionPhase.IN),
    RUBBER_BAND("rubber_band", "Rubber Band", MotionGroup.BASIC, MotionPhase.IN),

    PULSE_LOOP("pulse_loop", "Pulse / Heartbeat", MotionGroup.BASIC, MotionPhase.LOOP, 1200L),
    FLOAT_LOOP("float_loop", "Floating Air Drift", MotionGroup.BASIC, MotionPhase.LOOP, 2000L),
    SWING_LOOP("swing_loop", "Pendulum Swing", MotionGroup.BASIC, MotionPhase.LOOP, 1500L),
    WIGGLE_LOOP("wiggle_loop", "Dynamic Jello Wiggle", MotionGroup.BASIC, MotionPhase.LOOP, 1000L),
    TADA_POP("tada_pop", "Tada Celebration", MotionGroup.BASIC, MotionPhase.LOOP, 1000L),
    FLASH_BLINK("flash_blink", "Strobe Flash", MotionGroup.BASIC, MotionPhase.LOOP, 500L),

    FADE_OUT("fade_out", "Fade Out", MotionGroup.BASIC, MotionPhase.OUT),
    SLIDE_OUT_DOWN("slide_out_down", "Slide Down Out", MotionGroup.BASIC, MotionPhase.OUT),
    ZOOM_OUT_DISAPPEAR("zoom_out_dis", "Zoom Out", MotionGroup.BASIC, MotionPhase.OUT),

    WHIP_PAN("whip_pan", "Action Whip Pan", MotionGroup.CINEMATIC, MotionPhase.IN, 450L),
    PARALLAX_DRIFT("parallax_drift", "Parallax Depth Drift", MotionGroup.CINEMATIC, MotionPhase.LOOP, 3000L),
    DOLLY_ZOOM("dolly_zoom", "Hitchcock Dolly Zoom", MotionGroup.CINEMATIC, MotionPhase.IN, 1200L),
    RACK_FOCUS("rack_focus", "Cinematic Rack Focus", MotionGroup.CINEMATIC, MotionPhase.IN, 900L),
    HANDHELD_SHAKE("handheld_shake", "Organic Handheld Cam", MotionGroup.CINEMATIC, MotionPhase.LOOP, 2000L),
    TILT_PAN("tilt_pan", "Epic Tilt & Pan", MotionGroup.CINEMATIC, MotionPhase.IN, 1100L),
    ORBIT_3D("orbit_3d", "360 Orbit Rotate", MotionGroup.CINEMATIC, MotionPhase.LOOP, 3000L),

    MASK_WIPE("mask_wipe", "Linear Mask Wipe", MotionGroup.REVEAL, MotionPhase.IN, 700L),
    CIRCLE_REVEAL("circle_reveal", "Iris Circle Expand", MotionGroup.REVEAL, MotionPhase.IN, 650L),
    LINE_REVEAL("line_reveal", "Slice Line Reveal", MotionGroup.REVEAL, MotionPhase.IN, 600L),
    CURTAIN_REVEAL("curtain_reveal", "Stage Curtain Split", MotionGroup.REVEAL, MotionPhase.IN, 800L),
    CLOCK_WIPE("clock_wipe", "Clockwork Radial Wipe", MotionGroup.REVEAL, MotionPhase.IN, 750L),
    INK_DROP("ink_drop", "Artistic Ink Drop", MotionGroup.REVEAL, MotionPhase.IN, 850L),
    STROKE_DRAW("stroke_draw", "Draw-on Stroke", MotionGroup.REVEAL, MotionPhase.IN, 1000L),

    GLITCH_IN("glitch_in", "RGB Glitch Slice", MotionGroup.GLITCH, MotionPhase.IN, 500L),
    VHS_JUMP("vhs_jump", "VCR Tape Jump", MotionGroup.GLITCH, MotionPhase.LOOP, 800L),
    DATA_MOSH("data_mosh", "Data-mosh Block Shift", MotionGroup.GLITCH, MotionPhase.IN, 600L),
    SCAN_LINES_SWEEP("scan_sweep", "CRT Scanline Sweep", MotionGroup.GLITCH, MotionPhase.LOOP, 1000L),

    ZOOM_PUNCH_BEAT("zoom_punch", "Beat Zoom Punch", MotionGroup.SOCIAL_REELS, MotionPhase.LOOP, 400L),
    FLASH_CUT("flash_cut", "High Energy White Flash", MotionGroup.SOCIAL_REELS, MotionPhase.IN, 250L),
    SWIPE_UP_STORY("swipe_up", "Viral Swipe Up", MotionGroup.SOCIAL_REELS, MotionPhase.IN, 550L),
    HEART_BURST("heart_burst", "Like Heart Burst Pop", MotionGroup.SOCIAL_REELS, MotionPhase.IN, 700L),
    COUNTDOWN_POP("countdown_pop", "3-2-1 Pop Countdown", MotionGroup.SOCIAL_REELS, MotionPhase.IN, 600L)
}

@Serializable
data class MotionPresetConfig(
    val presetType: MotionPresetType,
    val durationMs: Long = presetType.defaultDurationMs,
    val easing: EasingType = EasingType.EASE_OUT,
    val intensity: Float = 1.0f,
    val isEnabled: Boolean = true
)

@Serializable
enum class SpeedRampPreset(val displayName: String) {
    CUSTOM("Custom Curve"),
    HERO_TIME("Hero Time (Fast -> Slow -> Normal)"),
    BULLET_TIME("Bullet Time (Ultra Slow Mo)"),
    MONTAGE("Montage (Fast Snappy Beats)"),
    FLASH_IN("Flash In (10x Fast -> 1x)"),
    FLASH_OUT("Flash Out (1x -> 10x Fast)"),
    PULSE("Pulse Beat (Accelerate-Decelerate)"),
    JUMP_CUT("Jump Cut Snappy"),
    SLOW_FAST_REVERSE("Slow to Fast then Reverse")
}

@Serializable
data class SpeedPoint(val normalizedTime: Float, val speedMultiplier: Float)

@Serializable
data class SpeedRampConfig(
    val isEnabled: Boolean = false,
    val preset: SpeedRampPreset = SpeedRampPreset.CUSTOM,
    val points: List<SpeedPoint> = listOf(
        SpeedPoint(0.0f, 1.0f),
        SpeedPoint(0.4f, 0.25f),
        SpeedPoint(0.7f, 2.5f),
        SpeedPoint(1.0f, 1.0f)
    ),
    val isReversed: Boolean = false,
    val opticalFlowBlending: Boolean = true
)

@Serializable
data class MotionBlurConfig(
    val isEnabled: Boolean = false,
    val shutterAngle: Float = 180f,
    val samples: Int = 8
)

@Serializable
data class PathPoint(val x: Float, val y: Float, val controlX: Float = x, val controlY: Float = y)

@Serializable
data class PathAnimationConfig(
    val isEnabled: Boolean = false,
    val points: List<PathPoint> = emptyList(),
    val autoOrient: Boolean = true,
    val isLooping: Boolean = true,
    val easing: EasingType = EasingType.EASE_IN_OUT
)

@Serializable
data class WiggleConfig(
    val isEnabled: Boolean = false,
    val frequencyHz: Float = 2.0f,
    val amplitudePx: Float = 25f,
    val rotateAmplitudeDeg: Float = 5f
)

@Serializable
data class StaggerConfig(
    val isEnabled: Boolean = false,
    val mode: StaggerMode = StaggerMode.PER_LETTER,
    val delayMsPerUnit: Long = 40L,
    val typewriterCursor: Boolean = true
)

@Serializable
enum class StaggerMode(val label: String) {
    PER_LETTER("Per Letter (Typewriter/Wave)"),
    PER_WORD("Per Word"),
    PER_LINE("Per Line")
}

@Serializable
data class Camera3DConfig(
    val isEnabled: Boolean = false,
    val posX: Float = 0f,
    val posY: Float = 0f,
    val zoom: Float = 1.0f,
    val tiltX: Float = 0f,
    val panY: Float = 0f,
    val rollZ: Float = 0f,
    val depthOfField: Float = 0f,
    val parallaxFactor: Float = 1.0f
)

@Serializable
enum class ParticleType(val label: String) {
    SNOW("Winter Snow"),
    RAIN("Cinematic Rain"),
    CONFETTI("Party Confetti"),
    SPARKS("Electric Sparks"),
    FIREWORKS("Festival Fireworks"),
    BUBBLES("Floating Soap Bubbles"),
    SMOKE("Mystic Smoke"),
    FIRE("Flames of Fire"),
    STARS("Twinkling Galaxy Stars"),
    DUST("Sunbeam Floating Dust"),
    HEARTS("Floating Hearts"),
    LIGHT_STREAKS("Cyberpunk Streaks")
}

@Serializable
data class ParticleConfig(
    val isEnabled: Boolean = false,
    val type: ParticleType = ParticleType.SPARKS,
    val amount: Int = 50,
    val speed: Float = 1.0f,
    val size: Float = 1.0f,
    val primaryColor: Long = 0xFFF59E0B,
    val directionAngleDeg: Float = 90f
)

@Serializable
data class AudioReactiveConfig(
    val isEnabled: Boolean = false,
    val targetScale: Boolean = true,
    val targetGlow: Boolean = true,
    val targetShake: Boolean = false,
    val targetOpacity: Boolean = false,
    val sensitivity: Float = 1.2f,
    val decayRate: Float = 0.85f
)
