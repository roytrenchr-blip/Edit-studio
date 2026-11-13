package com.editstudio.app.domain

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class EffectCategory(val displayName: String) {
    COLOR("Color & Light"),
    DRAWING_EDGE("Drawing & Edge"),
    BLUR("Blur"),
    DISTORTION("Distortion/Warp"),
    PROCEDURAL("Procedural"),
    THREE_D("3D"),
    MOVE_TRANSFORM("Move/Transform"),
    REPEAT("Repeat"),
    GLITCH("Glitch"),
    PARTICLES("Particles"),
    KEYING("Keying"),
    STYLIZE("Stylize"),
    GLOW("Color & Light")
}

@Serializable
enum class EffectType(
    val id: String,
    val displayName: String,
    val category: EffectCategory,
    val defaultIntensity: Float = 0.5f,
    val isFeatured: Boolean = false
) {
    // Blur
    BLUR_GAUSSIAN("blur_gaussian", "Gaussian Blur", EffectCategory.BLUR, 0.4f, true),
    BLUR_DIRECTIONAL("blur_dir", "Directional Blur", EffectCategory.BLUR, 0.5f),
    BLUR_RADIAL("blur_radial", "Radial Spin Blur", EffectCategory.BLUR, 0.3f),
    BLUR_ZOOM("blur_zoom", "Zoom Punch Blur", EffectCategory.BLUR, 0.4f),
    BLUR_TILT_SHIFT("blur_tilt", "Tilt Shift", EffectCategory.BLUR, 0.5f),
    BLUR_BOKEH("blur_bokeh", "Lens Bokeh", EffectCategory.BLUR, 0.6f),

    // Color & Light
    GLOW_NEON("glow_neon", "Neon Edge Glow", EffectCategory.COLOR, 0.7f, true),
    GLOW_BLOOM("glow_bloom", "Cinema Bloom", EffectCategory.COLOR, 0.5f),
    LIGHT_RAYS("light_rays", "God Rays", EffectCategory.COLOR, 0.6f),
    LENS_FLARE("lens_flare", "Anamorphic Flare", EffectCategory.COLOR, 0.5f),
    LIGHT_LEAKS("light_leaks", "Golden Light Leaks", EffectCategory.COLOR, 0.5f),
    COLOR_GRADING("color_grading", "Cinema Tone", EffectCategory.COLOR, 0.5f, true),
    VIGNETTE("vignette", "Dark Vignette", EffectCategory.COLOR, 0.4f),
    HUE_SATURATION("hue_sat", "Hue & Vibrance", EffectCategory.COLOR, 0.5f),
    BLACK_WHITE("black_white", "Noir B&W", EffectCategory.COLOR, 1.0f),
    LUT_FILTER("lut_filter", "3D LUT (.CUBE)", EffectCategory.COLOR, 0.8f),

    // Drawing & Edge
    NEON_OUTLINE("neon_outline", "Neon Outline", EffectCategory.DRAWING_EDGE, 0.8f, true),
    EDGE_DETECT("edge_detect", "Glow Edge Detect", EffectCategory.DRAWING_EDGE, 0.6f),
    SKETCH_INK("sketch_ink", "Pencil Sketch", EffectCategory.DRAWING_EDGE, 0.6f),

    // Glitch
    RGB_SPLIT("rgb_split", "RGB Split", EffectCategory.GLITCH, 0.4f, true),
    DIGITAL_GLITCH("digital_glitch", "Cyber Block Glitch", EffectCategory.GLITCH, 0.5f),
    VHS_TAPE("vhs_tape", "90s VHS Camcorder", EffectCategory.GLITCH, 0.6f, true),
    CRT_MONITOR("crt_monitor", "CRT Scanlines", EffectCategory.GLITCH, 0.5f),
    FILM_GRAIN("film_grain", "35mm Film Grain", EffectCategory.GLITCH, 0.35f),
    FILM_BURN("film_burn", "Vintage Film Burn", EffectCategory.GLITCH, 0.5f),
    NOISE("noise", "Sensor Noise", EffectCategory.GLITCH, 0.25f),

    // Distortion/Warp
    WAVE_DISTORT("wave_distort", "Wave Warp", EffectCategory.DISTORTION, 0.4f, true),
    RIPPLE("ripple", "Water Ripple", EffectCategory.DISTORTION, 0.5f),
    SHOCKWAVE("shockwave", "Shockwave Pulse", EffectCategory.DISTORTION, 0.6f),
    BULGE("bulge", "Sphere Bulge", EffectCategory.DISTORTION, 0.5f),
    PINCH("pinch", "Pinch Warp", EffectCategory.DISTORTION, 0.5f),
    SWIRL("swirl", "Vortex Swirl", EffectCategory.DISTORTION, 0.5f),
    FISHEYE("fisheye", "Action Fisheye", EffectCategory.DISTORTION, 0.6f),

    // Repeat
    MIRROR("mirror", "Symmetry Mirror", EffectCategory.REPEAT, 0.5f),
    KALEIDOSCOPE("kaleidoscope", "Kaleidoscope", EffectCategory.REPEAT, 0.5f, true),
    TILE_REPEAT("tile_repeat", "Motion Tiles", EffectCategory.REPEAT, 0.5f),

    // Stylize
    PIXELATE("pixelate", "8-Bit Pixelate", EffectCategory.STYLIZE, 0.4f),
    HALFTONE("halftone", "Comic Halftone", EffectCategory.STYLIZE, 0.5f),
    POSTERIZE("posterize", "Posterize Colors", EffectCategory.STYLIZE, 0.5f),
    CARTOON("cartoon", "Toon Shader", EffectCategory.STYLIZE, 0.5f),
    DUOTONE("duotone", "Navy Duotone", EffectCategory.STYLIZE, 0.7f, true),
    THERMAL("thermal", "Thermal Heat", EffectCategory.STYLIZE, 0.6f),

    // Procedural / Particles / 3D / Move / Keying
    FRACTAL_CLOUD("fractal_cloud", "Procedural Clouds", EffectCategory.PROCEDURAL, 0.5f),
    PARTICLE_SPARK("particle_spark", "Electric Sparks", EffectCategory.PARTICLES, 0.7f),
    PARTICLE_SNOW("particle_snow", "Winter Snow", EffectCategory.PARTICLES, 0.6f),
    CUBE_3D("cube_3d", "3D Flip Perspective", EffectCategory.THREE_D, 0.5f),
    OSCILLATE_MOVE("oscillate_move", "Auto Oscillate", EffectCategory.MOVE_TRANSFORM, 0.5f),
    CHROMA_KEY("chroma_key", "Chroma Key (Green)", EffectCategory.KEYING, 0.85f),
    LUMA_KEY("luma_key", "Luma Matte Key", EffectCategory.KEYING, 0.5f)
}

@Serializable
data class EffectParameter(
    val key: String,
    val name: String,
    val value: Float,
    val minValue: Float = 0f,
    val maxValue: Float = 1f
)

@Serializable
data class LayerEffect(
    val id: String = UUID.randomUUID().toString(),
    val type: EffectType,
    val isEnabled: Boolean = true,
    val intensity: Float = type.defaultIntensity,
    val params: List<EffectParameter> = emptyList(),
    val isFavorite: Boolean = false
)
