package com.editstudio.app.motion

import com.editstudio.app.domain.MotionGroup
import com.editstudio.app.domain.MotionPhase
import com.editstudio.app.domain.MotionPresetType

object MotionRegistry {
    val allPresets: List<MotionPresetType> = MotionPresetType.entries

    fun getByGroup(group: MotionGroup): List<MotionPresetType> {
        return allPresets.filter { it.group == group }
    }

    fun getByPhase(phase: MotionPhase): List<MotionPresetType> {
        return allPresets.filter { it.phase == phase }
    }

    fun search(query: String): List<MotionPresetType> {
        if (query.isBlank()) return allPresets
        return allPresets.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.group.displayName.contains(query, ignoreCase = true)
        }
    }
}
