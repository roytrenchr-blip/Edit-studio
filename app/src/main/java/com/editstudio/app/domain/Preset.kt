package com.editstudio.app.domain

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserMotionPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val motionPresets: List<MotionPresetConfig> = emptyList(),
    val effects: List<LayerEffect> = emptyList(),
    val speedRamp: SpeedRampConfig? = null,
    val wiggle: WiggleConfig? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
