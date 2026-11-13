package com.editstudio.app.ui.editor

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.editstudio.app.data.db.AppDatabase
import com.editstudio.app.data.db.ProjectEntity
import com.editstudio.app.data.settings.SettingsStore
import com.editstudio.app.domain.AnimProperty
import com.editstudio.app.domain.DrawingContent
import com.editstudio.app.domain.DrawingStroke
import com.editstudio.app.domain.EasingType
import com.editstudio.app.domain.EffectType
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.LayerEffect
import com.editstudio.app.domain.LayerType
import com.editstudio.app.domain.MotionPresetConfig
import com.editstudio.app.domain.MotionPresetType
import com.editstudio.app.domain.PathPoint
import com.editstudio.app.domain.Project
import com.editstudio.app.domain.ShapeContent
import com.editstudio.app.domain.ShapeType
import com.editstudio.app.domain.TextContent
import com.editstudio.app.domain.Transform
import com.editstudio.app.engine.Keyframes
import com.editstudio.app.engine.ProjectSerializer
import com.editstudio.app.engine.Timeline
import com.editstudio.app.engine.UndoManager
import com.editstudio.app.export.ExportConfig
import com.editstudio.app.export.ExportProgress
import com.editstudio.app.export.ExportService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

enum class ActiveSheet {
    NONE,
    LAYERS,
    KEYFRAMES,
    CURVE_EDITOR,
    SPEED_RAMP,
    PATH_EDITOR,
    EXPORT,
    SETTINGS,
    TEXT_EDIT,
    AUDIO_EDIT
}

enum class AddTab(val label: String) {
    SHAPE("Shape"),
    MEDIA("Media"),
    AUDIO("Audio"),
    ELEMENTS("Elements"),
    TEMPLATE("Template"),
    STORE("Store")
}

enum class PropertySubSheet(val title: String) {
    NONE(""),
    COLOR_FILL("Color & Fill"),
    BORDER_SHADOW("Border & Shadow"),
    BLENDING_OPACITY("Blending & Opacity"),
    MOVE_TRANSFORM("Move & Transform"),
    EDIT_SHAPE("Edit Shape"),
    EDIT_TEXT("Edit Text"),
    EFFECT_SLIDERS("Effect Sliders"),
    MOTION_SLIDERS("Motion Settings"),
    SPEED("Speed"),
    AUDIO("Audio")
}

data class EditorUiState(
    val project: Project = Project(),
    val currentTimeMs: Long = 0L,
    val isPlaying: Boolean = false,
    val isLooping: Boolean = true,
    val isFullscreenPreview: Boolean = false,
    val selectedLayerId: String? = null,
    val activeSheet: ActiveSheet = ActiveSheet.NONE,
    val isAddPanelVisible: Boolean = true,
    val activeAddTab: AddTab = AddTab.SHAPE,
    val activePropertySubSheet: PropertySubSheet = PropertySubSheet.NONE,
    val showEffectBrowser: Boolean = false,
    val showMotionBrowser: Boolean = false,
    val activeEffectId: String? = null,
    val activeMotionPresetIndex: Int? = null,
    val isDrawingActive: Boolean = false,
    val isVectorDrawing: Boolean = false,
    val isSimpleMode: Boolean = true,
    val isTourVisible: Boolean = false,
    val timelineZoom: Float = 1.0f,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val recentMediaUris: List<String> = emptyList(),
    val favoriteEffects: Set<String> = emptySet(),
    val recentEffects: List<String> = emptyList(),
    val favoritePresets: Set<String> = emptySet(),
    val recentPresets: List<String> = emptyList(),
    val feedbackMessage: String? = null
)

class EditorViewModel(
    private val context: Context,
    private val projectId: String
) : ViewModel() {

    private val db = AppDatabase.getInstance(context)
    val settingsStore = SettingsStore(context)
    private val undoManager = UndoManager(maxHistory = 60)

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    val exportProgress: StateFlow<ExportProgress> = ExportService.progressState

    private var playbackJob: Job? = null
    private var autoSaveJob: Job? = null

    init {
        loadProject()
        observeSettings()
    }

    private fun loadProject() {
        viewModelScope.launch {
            val entity = db.projectDao().getProjectById(projectId)
            val loaded = if (entity != null) {
                entity.toDomain()
            } else {
                Project(id = projectId, name = "Mera Naya Video")
            }
            _uiState.value = _uiState.value.copy(project = loaded)
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsStore.isSimpleMode.collect { simple ->
                _uiState.value = _uiState.value.copy(isSimpleMode = simple)
            }
        }
        viewModelScope.launch {
            val tourDone = settingsStore.isGuidedTourDone.first()
            if (!tourDone) {
                _uiState.value = _uiState.value.copy(isTourVisible = true)
            }
        }
        viewModelScope.launch {
            settingsStore.favoriteEffects.collect { favs ->
                _uiState.value = _uiState.value.copy(favoriteEffects = favs)
            }
        }
        viewModelScope.launch {
            settingsStore.recentEffects.collect { recs ->
                _uiState.value = _uiState.value.copy(recentEffects = recs)
            }
        }
        viewModelScope.launch {
            settingsStore.favoritePresets.collect { favs ->
                _uiState.value = _uiState.value.copy(favoritePresets = favs)
            }
        }
        viewModelScope.launch {
            settingsStore.recentPresets.collect { recs ->
                _uiState.value = _uiState.value.copy(recentPresets = recs)
            }
        }
    }

    private fun commitProjectChange(newProject: Project, recordHistory: Boolean = true) {
        if (recordHistory) {
            undoManager.recordSnapshot(_uiState.value.project)
        }
        val maxLayerEnd = newProject.layers.maxOfOrNull { it.endTimeMs } ?: 10000L
        val adjustedDuration = maxOf(newProject.durationMs, maxLayerEnd, 5000L)
        val updated = newProject.copy(
            durationMs = adjustedDuration,
            updatedAt = System.currentTimeMillis()
        )
        _uiState.value = _uiState.value.copy(
            project = updated,
            canUndo = undoManager.canUndo,
            canRedo = undoManager.canRedo
        )
        scheduleAutoSave(updated)
    }

    private fun scheduleAutoSave(project: Project) {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(350L)
            db.projectDao().insertProject(ProjectEntity.fromDomain(project))
        }
    }

    fun renameProject(newName: String) {
        val clean = newName.trim()
        if (clean.isEmpty()) return
        commitProjectChange(_uiState.value.project.copy(name = clean))
        showFeedback("Project naam update ho gaya!")
    }

    fun undo() {
        val prev = undoManager.undo(_uiState.value.project) ?: return
        _uiState.value = _uiState.value.copy(
            project = prev,
            canUndo = undoManager.canUndo,
            canRedo = undoManager.canRedo
        )
        scheduleAutoSave(prev)
    }

    fun redo() {
        val next = undoManager.redo(_uiState.value.project) ?: return
        _uiState.value = _uiState.value.copy(
            project = next,
            canUndo = undoManager.canUndo,
            canRedo = undoManager.canRedo
        )
        scheduleAutoSave(next)
    }

    fun seekTo(timeMs: Long) {
        val dur = _uiState.value.project.durationMs
        _uiState.value = _uiState.value.copy(currentTimeMs = timeMs.coerceIn(0L, dur))
    }

    fun togglePlayPause() {
        val currentlyPlaying = _uiState.value.isPlaying
        if (currentlyPlaying) {
            playbackJob?.cancel()
            _uiState.value = _uiState.value.copy(isPlaying = false)
        } else {
            _uiState.value = _uiState.value.copy(isPlaying = true)
            playbackJob = viewModelScope.launch {
                val fps = _uiState.value.project.frameRate.fps
                val frameMs = (1000L / fps).coerceAtLeast(16L)
                while (_uiState.value.isPlaying) {
                    delay(frameMs)
                    val cur = _uiState.value.currentTimeMs
                    val dur = _uiState.value.project.durationMs
                    val next = cur + frameMs
                    if (next >= dur) {
                        if (_uiState.value.isLooping) {
                            _uiState.value = _uiState.value.copy(currentTimeMs = 0L)
                        } else {
                            _uiState.value = _uiState.value.copy(currentTimeMs = dur, isPlaying = false)
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(currentTimeMs = next)
                    }
                }
            }
        }
    }

    fun toggleLoop() {
        val next = !_uiState.value.isLooping
        _uiState.value = _uiState.value.copy(isLooping = next)
        showFeedback(if (next) "Loop mode ON" else "Loop mode OFF")
    }

    fun toggleFullscreenPreview() {
        _uiState.value = _uiState.value.copy(isFullscreenPreview = !_uiState.value.isFullscreenPreview)
    }

    fun toggleBookmarkAtPlayhead() {
        val t = (_uiState.value.currentTimeMs / 50L) * 50L
        val marks = _uiState.value.project.bookmarksMs.toMutableList()
        val existing = marks.firstOrNull { kotlin.math.abs(it - t) < 120L }
        if (existing != null) {
            marks.remove(existing)
            showFeedback("Bookmark hata diya")
        } else {
            marks.add(t)
            marks.sort()
            showFeedback("Bookmark add ho gaya!")
        }
        commitProjectChange(_uiState.value.project.copy(bookmarksMs = marks))
    }

    fun stepFrame(forward: Boolean) {
        val proj = _uiState.value.project
        val next = Timeline.stepPlayhead(_uiState.value.currentTimeMs, forward, proj.frameRate.fps, proj.durationMs)
        _uiState.value = _uiState.value.copy(currentTimeMs = next)
    }

    fun jumpToAdjacentKeyframe(forward: Boolean) {
        val layer = _uiState.value.project.layers.firstOrNull { it.id == _uiState.value.selectedLayerId }
        val target = Timeline.findAdjacentKeyframeTime(layer, _uiState.value.currentTimeMs, forward)
        if (target != null) {
            seekTo(target)
        } else {
            stepFrame(forward)
        }
    }

    fun selectLayer(layerId: String?) {
        _uiState.value = _uiState.value.copy(
            selectedLayerId = layerId,
            activePropertySubSheet = PropertySubSheet.NONE,
            isAddPanelVisible = if (layerId == null) true else _uiState.value.isAddPanelVisible,
            isDrawingActive = false
        )
    }

    fun openAddPanel(tab: AddTab = _uiState.value.activeAddTab) {
        _uiState.value = _uiState.value.copy(
            selectedLayerId = null,
            isAddPanelVisible = true,
            activeAddTab = tab,
            activePropertySubSheet = PropertySubSheet.NONE,
            isDrawingActive = false
        )
    }

    fun closeAddPanel() {
        _uiState.value = _uiState.value.copy(isAddPanelVisible = false)
    }

    fun selectAddTab(tab: AddTab) {
        _uiState.value = _uiState.value.copy(activeAddTab = tab)
    }

    fun openPropertySubSheet(subSheet: PropertySubSheet) {
        _uiState.value = _uiState.value.copy(activePropertySubSheet = subSheet)
    }

    fun closePropertySubSheet() {
        _uiState.value = _uiState.value.copy(activePropertySubSheet = PropertySubSheet.NONE)
    }

    fun openEffectBrowser() {
        _uiState.value = _uiState.value.copy(showEffectBrowser = true)
    }

    fun closeEffectBrowser() {
        _uiState.value = _uiState.value.copy(showEffectBrowser = false)
    }

    fun openMotionBrowser() {
        _uiState.value = _uiState.value.copy(showMotionBrowser = true)
    }

    fun closeMotionBrowser() {
        _uiState.value = _uiState.value.copy(showMotionBrowser = false)
    }

    fun openSheet(sheet: ActiveSheet) {
        _uiState.value = _uiState.value.copy(activeSheet = sheet)
    }

    fun closeSheet() {
        _uiState.value = _uiState.value.copy(activeSheet = ActiveSheet.NONE)
    }

    fun toggleSimpleMode() {
        val next = !_uiState.value.isSimpleMode
        viewModelScope.launch {
            settingsStore.setSimpleMode(next)
        }
        showFeedback(if (next) "Simple Mode ON" else "Pro Mode ON (Keyframes & Curves)")
    }

    fun completeGuidedTour() {
        _uiState.value = _uiState.value.copy(isTourVisible = false)
        viewModelScope.launch {
            settingsStore.setGuidedTourDone(true)
        }
    }

    fun replayGuidedTour() {
        _uiState.value = _uiState.value.copy(isTourVisible = true)
    }

    fun setTimelineZoom(zoom: Float) {
        _uiState.value = _uiState.value.copy(timelineZoom = zoom.coerceIn(0.4f, 4.0f))
    }

    fun addTextLayer(text: String = "Naya Text") {
        val proj = _uiState.value.project
        val newLayer = Layer(
            name = text.take(18).ifBlank { "Text Layer" },
            type = LayerType.TEXT,
            startTimeMs = _uiState.value.currentTimeMs,
            durationMs = 4000L,
            textContent = TextContent(text = text),
            zIndex = proj.layers.size
        )
        commitProjectChange(proj.copy(layers = proj.layers + newLayer))
        _uiState.value = _uiState.value.copy(
            selectedLayerId = newLayer.id,
            activePropertySubSheet = PropertySubSheet.EDIT_TEXT
        )
    }

    fun addShapeLayer(shapeType: ShapeType) {
        val proj = _uiState.value.project
        val newLayer = Layer(
            name = shapeType.label,
            type = LayerType.SHAPE,
            startTimeMs = _uiState.value.currentTimeMs,
            durationMs = 4000L,
            shapeContent = ShapeContent(shapeType = shapeType),
            zIndex = proj.layers.size
        )
        commitProjectChange(proj.copy(layers = proj.layers + newLayer))
        _uiState.value = _uiState.value.copy(selectedLayerId = newLayer.id)
        showFeedback("${shapeType.label} add ho gaya!")
    }

    fun addDrawingLayer(isVector: Boolean) {
        val proj = _uiState.value.project
        val newLayer = Layer(
            name = if (isVector) "Vector Drawing" else "Freehand Drawing",
            type = LayerType.DRAWING,
            startTimeMs = _uiState.value.currentTimeMs,
            durationMs = 5000L,
            drawingContent = DrawingContent(isVectorMode = isVector),
            zIndex = proj.layers.size
        )
        commitProjectChange(proj.copy(layers = proj.layers + newLayer))
        _uiState.value = _uiState.value.copy(
            selectedLayerId = newLayer.id,
            isDrawingActive = true,
            isVectorDrawing = isVector,
            isAddPanelVisible = false
        )
        showFeedback("Preview par ungli se draw karein!")
    }

    fun addStrokeToActiveDrawing(points: List<PathPoint>) {
        if (points.size < 2) return
        val id = _uiState.value.selectedLayerId ?: return
        val proj = _uiState.value.project
        val layer = proj.layers.firstOrNull { it.id == id && it.type == LayerType.DRAWING } ?: return
        val dc = layer.drawingContent ?: DrawingContent(isVectorMode = _uiState.value.isVectorDrawing)
        val stroke = DrawingStroke(
            points = points,
            color = dc.activeColor,
            strokeWidth = dc.activeWidth,
            isVectorMode = _uiState.value.isVectorDrawing
        )
        val updated = layer.copy(drawingContent = dc.copy(strokes = dc.strokes + stroke))
        updateLayer(updated, recordHistory = true)
    }

    fun toggleDrawingMode(active: Boolean) {
        _uiState.value = _uiState.value.copy(isDrawingActive = active)
    }

    fun addMediaLayer(uriString: String, isVideo: Boolean, customName: String? = null) {
        val proj = _uiState.value.project
        val type = if (isVideo) LayerType.VIDEO else LayerType.IMAGE
        val newLayer = Layer(
            name = customName ?: if (isVideo) "Video Clip" else "Photo Layer",
            type = type,
            startTimeMs = _uiState.value.currentTimeMs,
            durationMs = 5000L,
            mediaUri = uriString,
            zIndex = proj.layers.size
        )
        val updatedRecents = (listOf(uriString) + _uiState.value.recentMediaUris).distinct().take(24)
        commitProjectChange(proj.copy(layers = proj.layers + newLayer))
        _uiState.value = _uiState.value.copy(
            selectedLayerId = newLayer.id,
            recentMediaUris = updatedRecents
        )
    }

    fun addAudioLayer(title: String, uriString: String? = null) {
        val proj = _uiState.value.project
        val newLayer = Layer(
            name = title,
            type = LayerType.AUDIO,
            startTimeMs = _uiState.value.currentTimeMs,
            durationMs = 8000L,
            mediaUri = uriString,
            zIndex = proj.layers.size
        )
        commitProjectChange(proj.copy(layers = proj.layers + newLayer))
        _uiState.value = _uiState.value.copy(selectedLayerId = newLayer.id)
        showFeedback("Audio track add ho gaya!")
    }

    fun addSpecialLayer(type: LayerType, name: String) {
        val proj = _uiState.value.project
        val newLayer = Layer(
            name = name,
            type = type,
            startTimeMs = _uiState.value.currentTimeMs,
            durationMs = 5000L,
            zIndex = proj.layers.size
        )
        commitProjectChange(proj.copy(layers = proj.layers + newLayer))
        _uiState.value = _uiState.value.copy(selectedLayerId = newLayer.id)
    }

    fun applyStarterTemplate(templateName: String) {
        val proj = _uiState.value.project
        val bg = Layer(
            name = "$templateName BG",
            type = LayerType.GRADIENT,
            startTimeMs = 0L,
            durationMs = 6000L,
            gradientColors = listOf(0xFF0F172A, 0xFF1D4ED8),
            zIndex = proj.layers.size
        )
        val shape = Layer(
            name = "Template Badge",
            type = LayerType.SHAPE,
            startTimeMs = 200L,
            durationMs = 5600L,
            shapeContent = ShapeContent(ShapeType.ROUNDED_SQUARE, fillColor = 0xFF14B8A6),
            motionPresets = listOf(MotionPresetConfig(MotionPresetType.POP_IN)),
            zIndex = proj.layers.size + 1
        )
        val text = Layer(
            name = templateName,
            type = LayerType.TEXT,
            startTimeMs = 400L,
            durationMs = 5200L,
            textContent = TextContent(text = templateName, fontSizeSp = 34f),
            motionPresets = listOf(MotionPresetConfig(MotionPresetType.SLIDE_UP)),
            zIndex = proj.layers.size + 2
        )
        commitProjectChange(proj.copy(layers = proj.layers + listOf(bg, shape, text)))
        _uiState.value = _uiState.value.copy(selectedLayerId = text.id)
        showFeedback("Template '$templateName' apply ho gaya!")
    }

    fun updateLayer(updatedLayer: Layer, recordHistory: Boolean = true) {
        val proj = _uiState.value.project
        val newLayers = proj.layers.map { if (it.id == updatedLayer.id) updatedLayer else it }
        commitProjectChange(proj.copy(layers = newLayers), recordHistory)
    }

    fun updateLayerTransform(layerId: String, newTransform: Transform) {
        val proj = _uiState.value.project
        val target = proj.layers.firstOrNull { it.id == layerId } ?: return
        if (target.isLocked) return
        updateLayer(target.copy(transform = newTransform), recordHistory = false)
    }

    fun commitTransformSnapshot() {
        undoManager.recordSnapshot(_uiState.value.project)
        _uiState.value = _uiState.value.copy(canUndo = undoManager.canUndo, canRedo = undoManager.canRedo)
        scheduleAutoSave(_uiState.value.project)
    }

    fun toggleKeyframeForSelected(property: AnimProperty, value: Float) {
        val id = _uiState.value.selectedLayerId ?: return
        val proj = _uiState.value.project
        val layer = proj.layers.firstOrNull { it.id == id } ?: return
        val localTime = (_uiState.value.currentTimeMs - layer.startTimeMs).coerceIn(0L, layer.durationMs)
        val updated = Keyframes.toggleKeyframe(layer, property, localTime, value)
        updateLayer(updated)
    }

    fun reorderLayer(layerId: String, moveUp: Boolean) {
        val proj = _uiState.value.project
        val list = proj.layers.toMutableList()
        val idx = list.indexOfFirst { it.id == layerId }
        if (idx == -1) return
        val targetIdx = if (moveUp) (idx + 1).coerceAtMost(list.lastIndex) else (idx - 1).coerceAtLeast(0)
        if (idx == targetIdx) return
        val item = list.removeAt(idx)
        list.add(targetIdx, item)
        val reindexed = list.mapIndexed { index, l -> l.copy(zIndex = index) }
        commitProjectChange(proj.copy(layers = reindexed))
    }

    fun moveAndTrimLayer(layerId: String, startMs: Long, durationMs: Long) {
        val proj = _uiState.value.project
        val layer = proj.layers.firstOrNull { it.id == layerId } ?: return
        val trimmed = Timeline.trimLayer(layer, startMs, durationMs)
        updateLayer(trimmed, recordHistory = false)
    }

    fun splitSelectedLayer() {
        val id = _uiState.value.selectedLayerId ?: return
        val proj = _uiState.value.project
        val layer = proj.layers.firstOrNull { it.id == id } ?: return
        val split = Timeline.splitLayer(layer, _uiState.value.currentTimeMs)
        if (split == null) {
            showFeedback("Split karne ke liye playhead ko layer ke beech mein rakhein")
            return
        }
        val newLayers = proj.layers.toMutableList()
        val idx = newLayers.indexOfFirst { it.id == id }
        newLayers[idx] = split.first
        newLayers.add(idx + 1, split.second)
        commitProjectChange(proj.copy(layers = newLayers))
        _uiState.value = _uiState.value.copy(selectedLayerId = split.second.id)
        showFeedback("Layer split ho gaya!")
    }

    fun duplicateSelectedLayer() {
        val id = _uiState.value.selectedLayerId ?: return
        val proj = _uiState.value.project
        val layer = proj.layers.firstOrNull { it.id == id } ?: return
        val dup = layer.copy(
            id = UUID.randomUUID().toString(),
            name = "${layer.name} Copy",
            startTimeMs = layer.startTimeMs + 300L,
            zIndex = proj.layers.size
        )
        commitProjectChange(proj.copy(layers = proj.layers + dup))
        _uiState.value = _uiState.value.copy(selectedLayerId = dup.id)
        showFeedback("Layer duplicate ho gaya!")
    }

    fun deleteSelectedLayer() {
        val id = _uiState.value.selectedLayerId ?: return
        val proj = _uiState.value.project
        val newLayers = proj.layers.filter { it.id != id }
        commitProjectChange(proj.copy(layers = newLayers))
        _uiState.value = _uiState.value.copy(
            selectedLayerId = null,
            activePropertySubSheet = PropertySubSheet.NONE
        )
        showFeedback("Layer delete ho gaya")
    }

    fun addMotionPresetToSelected(presetType: MotionPresetType) {
        val id = _uiState.value.selectedLayerId ?: return
        val proj = _uiState.value.project
        val layer = proj.layers.firstOrNull { it.id == id } ?: return
        val config = MotionPresetConfig(presetType = presetType, durationMs = presetType.defaultDurationMs)
        val filtered = layer.motionPresets.filter { it.presetType.phase != presetType.phase }
        val updatedPresets = filtered + config
        updateLayer(layer.copy(motionPresets = updatedPresets))
        viewModelScope.launch { settingsStore.addRecentPreset(presetType.id) }
        _uiState.value = _uiState.value.copy(
            showMotionBrowser = false,
            activeMotionPresetIndex = updatedPresets.lastIndex,
            activePropertySubSheet = PropertySubSheet.MOTION_SLIDERS
        )
        showFeedback("${presetType.title} lag gaya!")
    }

    fun toggleFavoritePreset(presetId: String) {
        viewModelScope.launch { settingsStore.toggleFavoritePreset(presetId) }
    }

    fun addEffectToSelected(effectType: EffectType) {
        val id = _uiState.value.selectedLayerId ?: return
        val proj = _uiState.value.project
        val layer = proj.layers.firstOrNull { it.id == id } ?: return
        val newEff = LayerEffect(type = effectType, intensity = effectType.defaultIntensity)
        updateLayer(layer.copy(effects = layer.effects + newEff))
        viewModelScope.launch { settingsStore.addRecentEffect(effectType.id) }
        _uiState.value = _uiState.value.copy(
            showEffectBrowser = false,
            activeEffectId = newEff.id,
            activePropertySubSheet = PropertySubSheet.EFFECT_SLIDERS
        )
        showFeedback("${effectType.displayName} add ho gaya!")
    }

    fun removeEffectFromSelected(effectId: String) {
        val id = _uiState.value.selectedLayerId ?: return
        val proj = _uiState.value.project
        val layer = proj.layers.firstOrNull { it.id == id } ?: return
        updateLayer(layer.copy(effects = layer.effects.filter { it.id != effectId }))
    }

    fun toggleFavoriteEffect(effectId: String) {
        viewModelScope.launch { settingsStore.toggleFavoriteEffect(effectId) }
    }

    fun startExport(config: ExportConfig) {
        val projJson = ProjectSerializer.serializeProject(_uiState.value.project)
        val intent = Intent(context, ExportService::class.java).apply {
            putExtra(ExportService.EXTRA_PROJECT_JSON, projJson)
            putExtra(ExportService.EXTRA_FORMAT, config.format.name)
            putExtra(ExportService.EXTRA_RES, config.resolution.name)
            putExtra(ExportService.EXTRA_FPS, config.frameRate.name)
            putExtra(ExportService.EXTRA_BITRATE, config.bitrateMbps)
        }
        context.startForegroundService(intent)
        showFeedback("Export shuru ho gaya! Notification bar dekhein.")
    }

    fun showFeedback(msg: String) {
        _uiState.value = _uiState.value.copy(feedbackMessage = msg)
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
    }
}
