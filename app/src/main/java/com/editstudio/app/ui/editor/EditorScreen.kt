package com.editstudio.app.ui.editor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.editstudio.app.domain.LayerType
import com.editstudio.app.ui.theme.StudioBorder
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioDarkBg
import com.editstudio.app.ui.theme.StudioNeonPurple
import com.editstudio.app.ui.theme.StudioSurface
import com.editstudio.app.ui.theme.TextPrimary
import com.editstudio.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val selectedLayer = state.project.layers.firstOrNull { it.id == state.selectedLayerId }

    // Visual media picker (Zero permission Android Photo & Video Picker)
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.addMediaLayer(uri.toString(), isVideo = true)
        }
    }

    LaunchedEffect(state.feedbackMessage) {
        state.feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    BackHandler {
        if (state.activeSheet != ActiveSheet.NONE) {
            viewModel.closeSheet()
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Editor Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioSurface)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Peeche Jayein",
                            tint = TextPrimary
                        )
                    }
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = state.project.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = state.project.aspectRatio.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = StudioCyberCyan
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Simple / Pro Mode Switch
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioDarkBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state.isSimpleMode) "Simple" else "Pro",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.isSimpleMode) StudioCyberCyan else StudioNeonPurple,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Switch(
                            checked = !state.isSimpleMode,
                            onCheckedChange = { viewModel.toggleSimpleMode() },
                            colors = SwitchDefaults.colors(checkedThumbColor = StudioNeonPurple)
                        )
                    }

                    // Layers Panel Toggle
                    IconButton(onClick = { viewModel.openSheet(ActiveSheet.LAYERS) }) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Layers Panel",
                            tint = StudioCyberCyan
                        )
                    }

                    // Export Button
                    IconButton(
                        onClick = { viewModel.openSheet(ActiveSheet.EXPORT) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioNeonPurple)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export Karein",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            BottomToolbar(
                activeSheet = state.activeSheet,
                onOpenSheet = { sheet ->
                    if (sheet == ActiveSheet.MEDIA) {
                        mediaPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    } else {
                        viewModel.openSheet(sheet)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Half: Preview View
                PreviewView(
                    project = state.project,
                    currentTimeMs = state.currentTimeMs,
                    selectedLayer = selectedLayer,
                    onTransformChange = { layerId, dx, dy, scaleFactor, rotDelta ->
                        val layer = state.project.layers.firstOrNull { it.id == layerId }
                        if (layer != null) {
                            val cur = layer.transform
                            viewModel.updateLayerTransform(
                                layerId,
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
                    modifier = Modifier.weight(1f)
                )

                // Floating Layer Toolbar if a layer is selected
                if (selectedLayer != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingLayerBar(
                            onEdit = { viewModel.openSheet(ActiveSheet.PROPERTIES) },
                            onMotion = { viewModel.openSheet(ActiveSheet.MOTION) },
                            onEffects = { viewModel.openSheet(ActiveSheet.EFFECTS) },
                            onSplit = { viewModel.splitSelectedLayer() },
                            onDuplicate = { viewModel.duplicateSelectedLayer() },
                            onDelete = { viewModel.deleteSelectedLayer() },
                            onMore = {
                                if (state.isSimpleMode) {
                                    viewModel.openSheet(ActiveSheet.SPEED_RAMP)
                                } else {
                                    viewModel.openSheet(ActiveSheet.KEYFRAMES)
                                }
                            }
                        )
                    }
                }

                // Compact Multi-Track Timeline Below Preview
                TimelineView(
                    project = state.project,
                    currentTimeMs = state.currentTimeMs,
                    isPlaying = state.isPlaying,
                    selectedLayerId = state.selectedLayerId,
                    canUndo = state.canUndo,
                    canRedo = state.canRedo,
                    zoomFactor = state.timelineZoom,
                    onSeek = { viewModel.seekTo(it) },
                    onTogglePlay = { viewModel.togglePlayPause() },
                    onStepFrame = { viewModel.stepFrame(it) },
                    onUndo = { viewModel.undo() },
                    onRedo = { viewModel.redo() },
                    onSelectLayer = { viewModel.selectLayer(it) },
                    onTrimLayer = { id, start, dur ->
                        val layer = state.project.layers.firstOrNull { it.id == id }
                        if (layer != null) {
                            viewModel.updateLayer(layer.copy(startTimeMs = start, durationMs = dur))
                        }
                    },
                    onZoomChange = { viewModel.setTimelineZoom(it) }
                )
            }

            // Bottom Sheets
            if (state.activeSheet != ActiveSheet.NONE) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.closeSheet() },
                    sheetState = bottomSheetState,
                    containerColor = StudioSurface
                ) {
                    when (state.activeSheet) {
                        ActiveSheet.MOTION -> {
                            MotionSheet(
                                layer = selectedLayer,
                                onApplyMotion = { viewModel.addMotionPresetToSelected(it) },
                                onClose = { viewModel.closeSheet() }
                            )
                        }
                        ActiveSheet.EFFECTS -> {
                            EffectsSheet(
                                layer = selectedLayer,
                                onAddEffect = { viewModel.addEffectToSelected(it) },
                                onRemoveEffect = { viewModel.removeEffectFromSelected(it) },
                                onUpdateEffect = { eff ->
                                    if (selectedLayer != null) {
                                        val updatedEffects = selectedLayer.effects.map { if (it.id == eff.id) eff else it }
                                        viewModel.updateLayer(selectedLayer.copy(effects = updatedEffects))
                                    }
                                },
                                onClose = { viewModel.closeSheet() }
                            )
                        }
                        ActiveSheet.TEXT -> {
                            TextSheet(
                                layer = if (selectedLayer?.type == LayerType.TEXT) selectedLayer else null,
                                onAddText = {
                                    viewModel.addTextLayer(it)
                                    viewModel.closeSheet()
                                },
                                onUpdateLayer = { viewModel.updateLayer(it) },
                                onClose = { viewModel.closeSheet() }
                            )
                        }
                        ActiveSheet.SHAPES -> {
                            ShapesSheet(
                                onAddShape = {
                                    viewModel.addShapeLayer(it)
                                    viewModel.closeSheet()
                                },
                                onClose = { viewModel.closeSheet() }
                            )
                        }
                        ActiveSheet.AUDIO -> {
                            AudioSheet(
                                selectedLayer = selectedLayer,
                                onUpdateLayer = { viewModel.updateLayer(it) },
                                onClose = { viewModel.closeSheet() }
                            )
                        }
                        ActiveSheet.LAYERS -> {
                            LayerPanel(
                                project = state.project,
                                selectedLayerId = state.selectedLayerId,
                                onSelectLayer = { viewModel.selectLayer(it) },
                                onUpdateLayer = { viewModel.updateLayer(it) },
                                onClose = { viewModel.closeSheet() }
                            )
                        }
                        ActiveSheet.PROPERTIES -> {
                            if (selectedLayer != null) {
                                PropertyPanel(
                                    layer = selectedLayer,
                                    isSimpleMode = state.isSimpleMode,
                                    onUpdateTransform = { viewModel.updateLayerTransform(selectedLayer.id, it) },
                                    onClose = { viewModel.closeSheet() }
                                )
                            }
                        }
                        ActiveSheet.KEYFRAMES -> {
                            if (selectedLayer != null) {
                                KeyframeEditor(
                                    layer = selectedLayer,
                                    currentTimeMs = state.currentTimeMs,
                                    onUpdateLayer = { viewModel.updateLayer(it) },
                                    onOpenCurveEditor = { viewModel.openSheet(ActiveSheet.CURVE_EDITOR) },
                                    onClose = { viewModel.closeSheet() }
                                )
                            }
                        }
                        ActiveSheet.CURVE_EDITOR -> {
                            if (selectedLayer != null) {
                                CurveEditor(
                                    layer = selectedLayer,
                                    onApplyCurve = { /* bezier curve saved to keyframe */ },
                                    onClose = { viewModel.closeSheet() }
                                )
                            }
                        }
                        ActiveSheet.SPEED_RAMP -> {
                            if (selectedLayer != null) {
                                SpeedRampEditor(
                                    layer = selectedLayer,
                                    onUpdateSpeedRamp = { viewModel.updateLayer(selectedLayer.copy(speedRamp = it)) },
                                    onClose = { viewModel.closeSheet() }
                                )
                            }
                        }
                        ActiveSheet.PATH_EDITOR -> {
                            if (selectedLayer != null) {
                                PathEditor(
                                    layer = selectedLayer,
                                    onUpdatePath = { viewModel.updateLayer(selectedLayer.copy(pathAnimation = it)) },
                                    onClose = { viewModel.closeSheet() }
                                )
                            }
                        }
                        ActiveSheet.EXPORT -> {
                            ExportSheet(
                                project = state.project,
                                onStartExport = { config ->
                                    viewModel.startExport(config)
                                },
                                onClose = { viewModel.closeSheet() }
                            )
                        }
                        else -> {}
                    }
                }
            }

            // Guided Tour Overlay for first-time use
            if (state.isTourVisible) {
                GuidedTour(onDismiss = { viewModel.completeGuidedTour() })
            }
        }
    }
}
