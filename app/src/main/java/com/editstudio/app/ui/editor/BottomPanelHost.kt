package com.editstudio.app.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.editstudio.app.domain.AnimProperty
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.LayerType
import com.editstudio.app.domain.ShapeType
import com.editstudio.app.store.StoreItem
import com.editstudio.app.ui.theme.NavyBorder
import com.editstudio.app.ui.theme.NavySurface

@Composable
fun BottomPanelHost(
    selectedLayer: Layer?,
    isAddPanelVisible: Boolean,
    activeAddTab: AddTab,
    activePropertySubSheet: PropertySubSheet,
    currentTimeMs: Long,
    isSimpleMode: Boolean,
    activeEffectId: String?,
    activeMotionPresetIndex: Int?,
    recentMediaUris: List<String>,
    storeItems: List<StoreItem>,
    onSelectAddTab: (AddTab) -> Unit,
    onAddShape: (ShapeType) -> Unit,
    onPickAllAlbums: () -> Unit,
    onPickAudioFile: () -> Unit,
    onAddMediaUri: (uri: String, isVideo: Boolean, name: String) -> Unit,
    onAddSpecialLayer: (LayerType, String) -> Unit,
    onAddAudioTrack: (title: String, uri: String?) -> Unit,
    onApplyTemplate: (name: String) -> Unit,
    onAddFreehandDrawing: () -> Unit,
    onAddVectorDrawing: () -> Unit,
    onAddText: () -> Unit,
    onDownloadStoreItem: (StoreItem) -> Unit,
    onCloseAddPanel: () -> Unit,
    onOpenPropertySubSheet: (PropertySubSheet) -> Unit,
    onClosePropertySubSheet: () -> Unit,
    onOpenEffects: () -> Unit,
    onOpenMotion: () -> Unit,
    onOpenKeyframesSheet: () -> Unit,
    onOpenSpeedRampSheet: () -> Unit,
    onOpenLayersSheet: () -> Unit,
    onOpenAudioSheet: () -> Unit,
    onResumeDrawing: () -> Unit,
    onSplitSelected: () -> Unit,
    onDuplicateSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onUpdateLayer: (Layer) -> Unit,
    onToggleKeyframe: (AnimProperty, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var panelHeightDp by remember { mutableStateOf(240.dp) }
    val showPanel = selectedLayer != null || isAddPanelVisible

    AnimatedVisibility(
        visible = showPanel,
        enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(panelHeightDp)
                .background(NavySurface)
                .border(0.5.dp, NavyBorder)
        ) {
            // Draggable Resize Handle Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .background(NavySurface)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            val deltaDp = with(density) { dragAmount.toDp() }
                            panelHeightDp = (panelHeightDp - deltaDp).coerceIn(170.dp, 390.dp)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NavyBorder)
                )
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (selectedLayer != null) {
                    if (activePropertySubSheet != PropertySubSheet.NONE) {
                        PropertySubSheetContent(
                            subSheet = activePropertySubSheet,
                            layer = selectedLayer,
                            currentTimeMs = currentTimeMs,
                            isSimpleMode = isSimpleMode,
                            activeEffectId = activeEffectId,
                            activeMotionPresetIndex = activeMotionPresetIndex,
                            onUpdateLayer = onUpdateLayer,
                            onToggleKeyframe = onToggleKeyframe,
                            onOpenSpeedRampFull = onOpenSpeedRampSheet,
                            onBack = onClosePropertySubSheet
                        )
                    } else {
                        PropertiesGrid(
                            layer = selectedLayer,
                            isSimpleMode = isSimpleMode,
                            onSplit = onSplitSelected,
                            onDuplicate = onDuplicateSelected,
                            onDelete = onDeleteSelected,
                            onOpenSubSheet = onOpenPropertySubSheet,
                            onOpenEffects = onOpenEffects,
                            onOpenMotion = onOpenMotion,
                            onOpenKeyframesSheet = onOpenKeyframesSheet,
                            onOpenSpeedRampSheet = onOpenSpeedRampSheet,
                            onOpenLayersSheet = onOpenLayersSheet,
                            onOpenAudioSheet = onOpenAudioSheet,
                            onResumeDrawing = onResumeDrawing,
                            onUpdateLayer = onUpdateLayer
                        )
                    }
                } else {
                    AddPanel(
                        activeTab = activeAddTab,
                        recentMediaUris = recentMediaUris,
                        storeItems = storeItems,
                        onSelectTab = onSelectAddTab,
                        onAddShape = onAddShape,
                        onPickAllAlbums = onPickAllAlbums,
                        onPickAudioFile = onPickAudioFile,
                        onAddMediaUri = onAddMediaUri,
                        onAddSpecialLayer = onAddSpecialLayer,
                        onAddAudioTrack = onAddAudioTrack,
                        onApplyTemplate = onApplyTemplate,
                        onAddFreehandDrawing = onAddFreehandDrawing,
                        onAddVectorDrawing = onAddVectorDrawing,
                        onAddText = onAddText,
                        onDownloadStoreItem = onDownloadStoreItem,
                        onClose = onCloseAddPanel
                    )
                }
            }
        }
    }
}
