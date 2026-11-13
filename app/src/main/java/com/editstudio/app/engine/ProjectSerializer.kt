package com.editstudio.app.engine

import com.editstudio.app.domain.Project
import com.editstudio.app.domain.UserMotionPreset
import kotlinx.serialization.json.Json

object ProjectSerializer {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun serializeProject(project: Project): String {
        return json.encodeToString(Project.serializer(), project)
    }

    fun deserializeProject(jsonString: String): Project {
        return json.decodeFromString(Project.serializer(), jsonString)
    }

    fun serializePreset(preset: UserMotionPreset): String {
        return json.encodeToString(UserMotionPreset.serializer(), preset)
    }

    fun deserializePreset(jsonString: String): UserMotionPreset {
        return json.decodeFromString(UserMotionPreset.serializer(), jsonString)
    }
}
