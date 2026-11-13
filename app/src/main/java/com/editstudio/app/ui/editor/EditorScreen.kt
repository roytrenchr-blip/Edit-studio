package com.editstudio.app.ui.editor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.editstudio.app.ui.home.SettingsDialog
import com.editstudio.app.ui.store.StoreViewModel
import com.editstudio.app.ui.theme.NavyBg
import com.editstudio.app.ui.theme.NavyPrimaryDeep
import com.editstudio.app.ui.theme.NavySurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val exportProgress by viewModel.exportProgress.collectAsStateWithLifecycle()
    val storeViewModel = remember { StoreViewModel(context) }
    val storeState by storeViewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember(state.project.name) { mutableStateOf(state.project.name) }

    val selectedLayer = state.project.layers.firstOrNull { it.id == state.selectedLayerId }
    val hasBookmarkNearPlayhead = state.project.bookmarksMs.any { kotlin.math.abs(it - state.currentTimeMs) < 120L }

    // Android Zero-Permission Photo & Video Picker ("All albums")
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri) ?: ""
            val isVideo = mime.startsWith("video")
            viewModel.addMediaLayer(uri.toString(), isVideo = isVideo)
        }
    }

    // Audio File Picker
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.addAudioLayer("Custom Audio", uri.toString())
        }
    }

    LaunchedEffect(state.feedbackMessage) {
        state.feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    LaunchedEffect(exportProgress.outputPath, exportProgress.error) {
        exportProgress.outputPath?.let {
            snackbarHostState.showSnackbar("Export complete! Gallery mein save ho gaya.")
        }
        exportProgress.error?.let {
            snackbarHostState.showSnackbar("Export error: $it")
        }
    }

    BackHandler {
        when {
            state.showEffectBrowser -> viewModel.closeEffectBrowser()
            state.showMotionBrowser -> viewModel.closeMotionBrowser()
            state.activeSheet != ActiveSheet.NONE -> viewModel.closeSheet()
            state.isFullscreenPreview -> viewModel.toggleFullscreenPreview()
            state.activePropertySubSheet != PropertySubSheet.NONE -> viewModel.closePropertySubSheet()
            state.selectedLayerId != null -> viewModel.selectLayer(null)
            else -> onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NavyBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!state.isFullscreenPreview) {
                TopBar(
                    project = state.project,
                    isSimpleMode = state.isSimpleMode,
                    onNavigateBack = onNavigateBack,
                    onRenameClick = {
                        renameInput = state.project.name
                        showRenameDialog = true
                    },
                    onToggleMode = { viewModel.toggleSimpleMode() },
                    onOpenSettings = { viewModel.openSheet(ActiveSheet.SETTINGS) },
                    onOpenExport = { viewModel.openSheet(ActiveSheet.EXPORT) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 2. PREVIEW
                PreviewView(
                    project = state.project,
                    currentTimeMs = state.currentTimeMs,
                    selectedLayer = selectedLayer,
                    isDrawingActive = state.isDrawingActive,
                    isVectorDrawing = state.isVectorDrawing,
                    onTransformChange = { id, dx, dy, scaleFactor, rotDelta ->
                        val layer = state.project.layers.firstOrNull { it.id == id }
                        if (layer != null) {
                            val cur = layer.transform
                            viewModel.updateLayerTransform(
                                id,
                                cur.copy(
                                    x = cur.x + dx,
                                    y = cur.y + dy,
                                    scaleX = (cur.scaleX * scaleFactor).coerceIn(0.1f, 5.0f),
                                    scaleY = (cur.scaleY * scaleFactor).coerceIn(0.1f, 5.0f),
                                    rotationZ = cur.rotationZ + rotDelta
                                )
                            )
                        }
                    },
                    onTransformCommit = { viewModel.commitTransformSnapshot() },
                    onAddDrawingStroke = { pts -> viewModel.addStrokeToActiveDrawing(pts) },
                    onFinishDrawing = { viewModel.toggleDrawingMode(false) },
                    modifier = Modifier.weight(if (state.isFullscreenPreview) 1f else 1.1f)
                )

                // 3. PLAYBACK ROW directly below preview
                PlaybackRow(
                    isPlaying = state.isPlaying,
                    isLooping = state.isLooping,
                    isFullscreen = state.isFullscreenPreview,
                    hasBookmarkNearPlayhead = hasBookmarkNearPlayhead,
                    canUndo = state.canUndo,
                    canRedo = state.canRedo,
                    hasSelectedLayer = selectedLayer != null,
                    onUndo = { viewModel.undo() },
                    onRedo = { viewModel.redo() },
                    onPrevKeyframe = { viewModel.jumpToAdjacentKeyframe(forward = false) },
                    onTogglePlay = { viewModel.togglePlayPause() },
                    onNextKeyframe = { viewModel.jumpToAdjacentKeyframe(forward = true) },
                    onCopyLayer = { viewModel.duplicateSelectedLayer() },
                    onToggleBookmark = { viewModel.toggleBookmarkAtPlayhead() },
                    onToggleLoop = { viewModel.toggleLoop() },
                    onToggleFullscreen = { viewModel.toggleFullscreenPreview() }
                )

                if (!state.isFullscreenPreview) {
                    // 4. TIMELINE
                    TimelinePanel(
                        project = state.project,
                        currentTimeMs = state.currentTimeMs,
                        isPlaying = state.isPlaying,
                        selectedLayerId = state.selectedLayerId,
                        zoomFactor = state.timelineZoom,
                        onSeek = { viewModel.seekTo(it) },
                        onSelectLayer = { viewModel.selectLayer(it) },
                        onMoveAndTrimLayer = { id, start, dur -> viewModel.moveAndTrimLayer(id, start, dur) },
                        onCommitLayerChange = { viewModel.commitTransformSnapshot() },
                        onReorderLayer = { id, up -> viewModel.reorderLayer(id, up) },
                        onZoomChange = { viewModel.setTimelineZoom(it) },
                        onOpenAddPanel = { viewModel.openAddPanel() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.75f)
                    )

                    // 5. BOTTOM PANEL (changes cleanly between AddPanel & PropertiesGrid/SubSheet)
                    BottomPanelHost(
                        selectedLayer = selectedLayer,
                        isAddPanelVisible = state.isAddPanelVisible,
                        activeAddTab = state.activeAddTab,
                        activePropertySubSheet = state.activePropertySubSheet,
                        currentTimeMs = state.currentTimeMs,
                        isSimpleMode = state.isSimpleMode,
                        activeEffectId = state.activeEffectId,
                        activeMotionPresetIndex = state.activeMotionPresetIndex,
                        recentMediaUris = state.recentMediaUris,
                        storeItems = storeState.items,
                        onSelectAddTab = { viewModel.selectAddTab(it) },
                        onAddShape = { viewModel.addShapeLayer(it) },
                        onPickAllAlbums = {
                            mediaPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        onPickAudioFile = {
                            audioPickerLauncher.launch(arrayOf("audio/*"))
                        },
                        onAddMediaUri = { uri, isVideo, name ->
                            viewModel.addMediaLayer(uri, isVideo, name)
                        },
                        onAddSpecialLayer = { type, name ->
                            viewModel.addSpecialLayer(type, name)
                        },
                        onAddAudioTrack = { title, uri ->
                            viewModel.addAudioLayer(title, uri)
                        },
                        onApplyTemplate = { name ->
                            viewModel.applyStarterTemplate(name)
                        },
                        onAddFreehandDrawing = { viewModel.addDrawingLayer(isVector = false) },
                        onAddVectorDrawing = { viewModel.addDrawingLayer(isVector = true) },
                        onAddText = { viewModel.addTextLayer("Naya Text") },
                        onDownloadStoreItem = { storeViewModel.downloadItem(it) },
                        onCloseAddPanel = { viewModel.closeAddPanel() },
                        onOpenPropertySubSheet = { viewModel.openPropertySubSheet(it) },
                        onClosePropertySubSheet = { viewModel.closePropertySubSheet() },
                        onOpenEffects = { viewModel.openEffectBrowser() },
                        onOpenMotion = { viewModel.openMotionBrowser() },
                        onOpenKeyframesSheet = { viewModel.openSheet(ActiveSheet.KEYFRAMES) },
                        onOpenSpeedRampSheet = { viewModel.openSheet(ActiveSheet.SPEED_RAMP) },
                        onOpenLayersSheet = { viewModel.openSheet(ActiveSheet.LAYERS) },
                        onOpenAudioSheet = { viewModel.openSheet(ActiveSheet.AUDIO_EDIT) },
                        onResumeDrawing = { viewModel.toggleDrawingMode(true) },
                        onSplitSelected = { viewModel.splitSelectedLayer() },
                        onDuplicateSelected = { viewModel.duplicateSelectedLayer() },
                        onDeleteSelected = { viewModel.deleteSelectedLayer() },
                        onUpdateLayer = { viewModel.updateLayer(it) },
                        onToggleKeyframe = { prop, v -> viewModel.toggleKeyframeForSelected(prop, v) }
                    )
                }
            }

            // 6. Full-screen EFFECT BROWSER
            if (state.showEffectBrowser) {
                EffectBrowser(
                    favoriteEffects = state.favoriteEffects,
                    recentEffects = state.recentEffects,
                    storeEffectPacks = storeState.items.filter { it.category == "Effect Packs" || it.category == "LUT Looks" },
                    onSelectEffect = { viewModel.addEffectToSelected(it) },
                    onToggleFavorite = { viewModel.toggleFavoriteEffect(it) },
                    onDownloadStorePack = { storeViewModel.downloadItem(it) },
                    onClose = { viewModel.closeEffectBrowser() }
                )
            }

            // 7. Full-screen MOTION BROWSER
            if (state.showMotionBrowser) {
                MotionBrowser(
                    favoritePresets = state.favoritePresets,
                    recentPresets = state.recentPresets,
                    storeMotionPacks = storeState.items.filter { it.category == "Motion Packs" || it.category == "Transition Packs" },
                    onSelectPreset = { viewModel.addMotionPresetToSelected(it) },
                    onToggleFavorite = { viewModel.toggleFavoritePreset(it) },
                    onOpenSpeedRamp = {
                        viewModel.closeMotionBrowser()
                        viewModel.openSheet(ActiveSheet.SPEED_RAMP)
                    },
                    onOpenPathEditor = {
                        viewModel.closeMotionBrowser()
                        viewModel.openSheet(ActiveSheet.PATH_EDITOR)
                    },
                    onOpenBeatSync = {
                        viewModel.closeMotionBrowser()
                        viewModel.openSheet(ActiveSheet.AUDIO_EDIT)
                    },
                    onDownloadStorePack = { storeViewModel.downloadItem(it) },
                    onClose = { viewModel.closeMotionBrowser() }
                )
            }

            // Modal Bottom Sheets for Advanced Editors & Export
            if (state.activeSheet != ActiveSheet.NONE && state.activeSheet != ActiveSheet.SETTINGS) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.closeSheet() },
                    sheetState = bottomSheetState,
                    containerColor = NavySurface
                ) {
                    when (state.activeSheet) {
                        ActiveSheet.LAYERS -> LayerPanel(
                            project = state.project,
                            selectedLayerId = state.selectedLayerId,
                            onSelectLayer = { viewModel.selectLayer(it) },
                            onUpdateLayer = { viewModel.updateLayer(it) },
                            onClose = { viewModel.closeSheet() }
                        )
                        ActiveSheet.KEYFRAMES -> if (selectedLayer != null) {
                            KeyframeEditor(
                                layer = selectedLayer,
                                currentTimeMs = state.currentTimeMs,
                                onUpdateLayer = { viewModel.updateLayer(it) },
                                onOpenCurveEditor = { viewModel.openSheet(ActiveSheet.CURVE_EDITOR) },
                                onClose = { viewModel.closeSheet() }
                            )
                        }
                        ActiveSheet.CURVE_EDITOR -> if (selectedLayer != null) {
                            CurveEditor(
                                layer = selectedLayer,
                                onApplyCurve = { bezier ->
                                    val updatedTracks = selectedLayer.keyframeTracks.map { tr ->
                                        tr.copy(keyframes = tr.keyframes.map { kf ->
                                            kf.copy(easing = com.editstudio.app.domain.EasingType.BEZIER_CUSTOM, bezier = bezier)
                                        })
                                    }
                                    viewModel.updateLayer(selectedLayer.copy(keyframeTracks = updatedTracks))
                                },
                                onClose = { viewModel.closeSheet() }
                            )
                        }
                        ActiveSheet.SPEED_RAMP -> if (selectedLayer != null) {
                            SpeedRampEditor(
                                layer = selectedLayer,
                                onUpdateSpeedRamp = { viewModel.updateLayer(selectedLayer.copy(speedRamp = it)) },
                                onClose = { viewModel.closeSheet() }
                            )
                        }
                        ActiveSheet.PATH_EDITOR -> if (selectedLayer != null) {
                            PathEditor(
                                layer = selectedLayer,
                                onUpdatePath = { viewModel.updateLayer(selectedLayer.copy(pathAnimation = it)) },
                                onClose = { viewModel.closeSheet() }
                            )
                        }
                        ActiveSheet.AUDIO_EDIT -> AudioSheet(
                            selectedLayer = selectedLayer,
                            onUpdateLayer = { viewModel.updateLayer(it) },
                            onClose = { viewModel.closeSheet() }
                        )
                        ActiveSheet.EXPORT -> ExportSheet(
                            project = state.project,
                            onStartExport = { config ->
                                viewModel.startExport(config)
                                viewModel.closeSheet()
                            },
                            onClose = { viewModel.closeSheet() }
                        )
                        else -> {}
                    }
                }
            }

            if (state.activeSheet == ActiveSheet.SETTINGS) {
                SettingsDialog(
                    settingsStore = viewModel.settingsStore,
                    onReplayTour = {
                        viewModel.closeSheet()
                        viewModel.replayGuidedTour()
                    },
                    onDismiss = { viewModel.closeSheet() }
                )
            }

            if (showRenameDialog) {
                AlertDialog(
                    onDismissRequest = { showRenameDialog = false },
                    title = { Text("Project Ka Naam Badlein") },
                    text = {
                        OutlinedTextField(
                            value = renameInput,
                            onValueChange = { renameInput = it },
                            label = { Text("Project Name") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.renameProject(renameInput)
                                showRenameDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimaryDeep)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRenameDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (state.isTourVisible) {
                GuidedTour(onDismiss = { viewModel.completeGuidedTour() })
            }
        }
    }
}
