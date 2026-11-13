package com.editstudio.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.editstudio.app.domain.LayerType

// Original Dark Navy Palette for Edit Studio
val NavyBg = Color(0xFF0B1120)
val NavySurface = Color(0xFF111A2E)
val NavyCard = Color(0xFF18233C)
val NavyElevated = Color(0xFF1F2D4D)
val NavyBorder = Color(0xFF253456)

val NavyPrimary = Color(0xFF38BDF8)      // Electric Sky Blue
val NavyPrimaryDeep = Color(0xFF2563EB)  // Cobalt Action Blue
val NavyAccentTeal = Color(0xFF14B8A6)   // Mint Teal
val NavyAmber = Color(0xFFF59E0B)        // Keyframe Diamond / Bookmark Amber
val NavyCoral = Color(0xFFFB7185)        // Playhead / Danger Coral

val NavyTextPrimary = Color(0xFFF1F5F9)
val NavyTextSecondary = Color(0xFF94A3B8)

// Distinct Layer-Type Colors (Timeline Bars & Left Strip Icons)
val LayerColorGroup = Color(0xFF4F46E5)
val LayerColorShape = Color(0xFF0EA5E9)
val LayerColorText = Color(0xFF14B8A6)
val LayerColorMedia = Color(0xFF2563EB)
val LayerColorAudio = Color(0xFF10B981)
val LayerColorDrawing = Color(0xFFF59E0B)
val LayerColorAdjustment = Color(0xFFE11D48)
val LayerColorCamera = Color(0xFF0284C7)

fun colorForLayerType(type: LayerType): Color {
    return when (type) {
        LayerType.GROUP -> LayerColorGroup
        LayerType.SHAPE -> LayerColorShape
        LayerType.TEXT -> LayerColorText
        LayerType.VIDEO, LayerType.IMAGE -> LayerColorMedia
        LayerType.AUDIO -> LayerColorAudio
        LayerType.DRAWING -> LayerColorDrawing
        LayerType.ADJUSTMENT -> LayerColorAdjustment
        LayerType.SOLID, LayerType.GRADIENT, LayerType.CAMERA -> LayerColorCamera
    }
}
