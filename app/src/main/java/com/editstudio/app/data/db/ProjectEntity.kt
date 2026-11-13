package com.editstudio.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.editstudio.app.domain.Project
import com.editstudio.app.engine.ProjectSerializer

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val aspectRatio: String,
    val durationMs: Long,
    val layerCount: Int,
    val thumbnailUri: String?,
    val isFavorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val projectJson: String
) {
    fun toDomain(): Project {
        return try {
            ProjectSerializer.deserializeProject(projectJson)
        } catch (e: Exception) {
            Project(
                id = id,
                name = name,
                durationMs = durationMs,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }

    companion object {
        fun fromDomain(project: Project): ProjectEntity {
            return ProjectEntity(
                id = project.id,
                name = project.name,
                aspectRatio = project.aspectRatio.name,
                durationMs = project.durationMs,
                layerCount = project.layers.size,
                thumbnailUri = project.thumbnailUri,
                isFavorite = project.isFavorite,
                createdAt = project.createdAt,
                updatedAt = project.updatedAt,
                projectJson = ProjectSerializer.serializeProject(project)
            )
        }
    }
}
