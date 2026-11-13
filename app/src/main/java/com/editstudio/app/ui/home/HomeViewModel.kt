package com.editstudio.app.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.editstudio.app.data.db.AppDatabase
import com.editstudio.app.data.db.ProjectEntity
import com.editstudio.app.data.settings.SettingsStore
import com.editstudio.app.domain.AspectRatio
import com.editstudio.app.domain.FrameRate
import com.editstudio.app.domain.Project
import com.editstudio.app.domain.VideoResolution
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class HomeViewModel(private val context: Context) : ViewModel() {

    private val db = AppDatabase.getInstance(context)
    val settingsStore = SettingsStore(context)

    val projects: StateFlow<List<ProjectEntity>> = db.projectDao().getAllProjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isCreateDialogOpen = MutableStateFlow(false)
    val isCreateDialogOpen: StateFlow<Boolean> = _isCreateDialogOpen.asStateFlow()

    private val _isSettingsDialogOpen = MutableStateFlow(false)
    val isSettingsDialogOpen: StateFlow<Boolean> = _isSettingsDialogOpen.asStateFlow()

    fun openCreateDialog() {
        _isCreateDialogOpen.value = true
    }

    fun closeCreateDialog() {
        _isCreateDialogOpen.value = false
    }

    fun openSettingsDialog() {
        _isSettingsDialogOpen.value = true
    }

    fun closeSettingsDialog() {
        _isSettingsDialogOpen.value = false
    }

    fun createNewProject(
        name: String,
        aspectRatio: AspectRatio,
        resolution: VideoResolution,
        frameRate: FrameRate
    ): String {
        val newId = UUID.randomUUID().toString()
        val project = Project(
            id = newId,
            name = if (name.isNotBlank()) name else "Mera Naya Project",
            aspectRatio = aspectRatio,
            resolution = resolution,
            frameRate = frameRate,
            durationMs = 10000L
        )
        viewModelScope.launch {
            db.projectDao().insertProject(ProjectEntity.fromDomain(project))
        }
        closeCreateDialog()
        return newId
    }

    fun duplicateProject(entity: ProjectEntity) {
        viewModelScope.launch {
            val domain = entity.toDomain()
            val dup = domain.copy(
                id = UUID.randomUUID().toString(),
                name = "${domain.name} (Copy)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            db.projectDao().insertProject(ProjectEntity.fromDomain(dup))
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            db.projectDao().deleteProjectById(id)
        }
    }

    fun renameProject(id: String, newName: String) {
        viewModelScope.launch {
            val existing = db.projectDao().getProjectById(id) ?: return@launch
            val domain = existing.toDomain().copy(name = newName, updatedAt = System.currentTimeMillis())
            db.projectDao().insertProject(ProjectEntity.fromDomain(domain))
        }
    }

    fun toggleFavorite(id: String, isFavorite: Boolean) {
        viewModelScope.launch {
            db.projectDao().setFavorite(id, !isFavorite)
        }
    }
}
