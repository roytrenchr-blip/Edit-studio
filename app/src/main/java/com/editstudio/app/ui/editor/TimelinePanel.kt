package com.editstudio.app.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editstudio.app.domain.LayerType
import com.editstudio.app.domain.Project
import com.editstudio.app.ui.theme.NavyAmber
import com.editstudio.app.ui.theme.NavyBg
import com.editstudio.app.ui.theme.NavyBorder
import com.editstudio.app.ui.theme.NavyCard
import com.editstudio.app.ui.theme.NavyCoral
import com.editstudio.app.ui.theme.NavyPrimary
import com.editstudio.app.ui.theme.NavyPrimaryDeep
import com.editstudio.app.ui.theme.NavySurface
import com.editstudio.app.ui.theme.NavyTextPrimary
import com.editstudio.app.ui.theme.NavyTextSecondary
import com.editstudio.app.ui.theme.colorForLayerType
import com.editstudio.app.util.TimeFormat
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.roundToInt

@Composable
fun TimelinePanel(
    project: Project,
    currentTimeMs: Long,
    isPlaying: Boolean,
    selectedLayerId: String?,
    zoomFactor: Float,
    onSeek: (Long) -> Unit,
    onSelectLayer: (String?) -> Unit,
    onMoveAndTrimLayer: (layerId: String, startMs: Long, durationMs: Long) -> Unit,
    onCommitLayerChange: () -> Unit,
    onReorderLayer: (layerId: String, moveUp: Boolean) -> Unit,
    onZoomChange: (Float) -> Unit,
    onOpenAddPanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    var isSyncingProgrammatically by remember { mutableStateOf(false) }

    // px per millisecond scales with zoomFactor
    val pxPerSecDp = 64f * zoomFactor
    val pxPerSecPx = with(density) { pxPerSecDp.dp.toPx() }
    val pxPerMs = pxPerSecPx / 1000f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(NavyBg)
            .border(0.5.dp, NavyBorder)
    ) {
        // Timecode above Center-Fixed Playhead
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavySurface)
                .padding(vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NavyCard)
                    .border(1.dp, NavyBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = formatTimecodeHHMMSS(currentTimeMs),
                    color = NavyTextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(zoomFactor) {
                    detectTransformGestures { _, _, zoom, _ ->
                        if (kotlin.math.abs(zoom - 1f) > 0.01f) {
                            onZoomChange(zoomFactor * zoom)
                        }
                    }
                }
        ) {
            val leftStripWidthDp = 30.dp
            val scrollAreaWidthDp = (maxWidth - leftStripWidthDp).coerceAtLeast(100.dp)
            val scrollAreaWidthPx = with(density) { scrollAreaWidthDp.toPx() }
            val halfViewportPx = scrollAreaWidthPx / 2f
            val totalContentWidthPx = halfViewportPx * 2f + project.durationMs * pxPerMs
            val totalContentWidthDp = with(density) { totalContentWidthPx.toDp() }

            // Sync scrollState when user scrolls horizontally -> updates currentTimeMs
            LaunchedEffect(horizontalScrollState, pxPerMs) {
                snapshotFlow { horizontalScrollState.value to horizontalScrollState.isScrollInProgress }
                    .distinctUntilChanged()
                    .collect { (scrollX, inProgress) ->
                        if (inProgress && !isSyncingProgrammatically) {
                            val tMs = (scrollX / pxPerMs).toLong().coerceIn(0L, project.durationMs)
                            onSeek(tMs)
                        }
                    }
            }

            // Sync scrollState when currentTimeMs changes externally (e.g. playback / prev-next keyframe)
            LaunchedEffect(currentTimeMs, pxPerMs) {
                if (!horizontalScrollState.isScrollInProgress) {
                    val targetX = (currentTimeMs * pxPerMs).roundToInt().coerceAtLeast(0)
                    if (kotlin.math.abs(horizontalScrollState.value - targetX) > 2) {
                        isSyncingProgrammatically = true
                        horizontalScrollState.scrollTo(targetX)
                        isSyncingProgrammatically = false
                    }
                }
            }

            Row(modifier = Modifier.fillMaxSize()) {
                // Slim Vertical Strip on Left showing colored icon per layer type
                Column(
                    modifier = Modifier
                        .width(leftStripWidthDp)
                        .fillMaxHeight()
                        .background(NavySurface)
                        .border(0.5.dp, NavyBorder)
                        .padding(top = 20.dp)
                        .verticalScroll(verticalScrollState),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val ordered = project.layers.reversed()
                    ordered.forEach { layer ->
                        val tint = colorForLayerType(layer.type)
                        val isSel = layer.id == selectedLayerId
                        Box(
                            modifier = Modifier
                                .size(24.dp, 34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) tint.copy(alpha = 0.28f) else Color.Transparent)
                                .clickable { onSelectLayer(if (isSel) null else layer.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconForLayerType(layer.type),
                                contentDescription = layer.type.displayName,
                                tint = tint,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Horizontally Scrollable Ruler & Layers under Fixed Center Playhead
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        Column(
                            modifier = Modifier
                                .width(totalContentWidthDp)
                                .fillMaxHeight()
                        ) {
                            // Time Ruler + Bookmarks
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                                    .background(NavySurface)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val totalSecs = (project.durationMs / 1000L).toInt() + 1
                                    for (s in 0..totalSecs) {
                                        val x = halfViewportPx + s * pxPerSecPx
                                        drawLine(
                                            color = NavyBorder,
                                            start = Offset(x, size.height * 0.35f),
                                            end = Offset(x, size.height),
                                            strokeWidth = 1.5f
                                        )
                                        val halfX = x + pxPerSecPx / 2f
                                        drawLine(
                                            color = NavyBorder.copy(alpha = 0.5f),
                                            start = Offset(halfX, size.height * 0.65f),
                                            end = Offset(halfX, size.height),
                                            strokeWidth = 1f
                                        )
                                    }

                                    // Bookmark Markers
                                    project.bookmarksMs.forEach { bm ->
                                        val bx = halfViewportPx + bm * pxPerMs
                                        drawCircle(
                                            color = NavyAmber,
                                            radius = 4.5f,
                                            center = Offset(bx, size.height * 0.45f)
                                        )
                                    }
                                }
                            }

                            // Layers Stacked Vertically
                            if (project.layers.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { onSelectLayer(null) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Koi layer nahi — '+' dabakar Shape, Text ya Video add karein",
                                        color = NavyTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .verticalScroll(verticalScrollState)
                                        .padding(vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    project.layers.reversed().forEach { layer ->
                                        LayerBar(
                                            layer = layer,
                                            project = project,
                                            isSelected = layer.id == selectedLayerId,
                                            pxPerMs = pxPerMs,
                                            leadingOffsetPx = halfViewportPx,
                                            onSelect = { onSelectLayer(if (selectedLayerId == layer.id) null else layer.id) },
                                            onMoveAndTrim = { s, d -> onMoveAndTrimLayer(layer.id, s, d) },
                                            onCommitChange = onCommitLayerChange,
                                            onReorder = { moveUp -> onReorderLayer(layer.id, moveUp) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Fixed Center Vertical Playhead Line
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(NavyCoral)
                    )
                    // Playhead Cap Triangle/Pill at Top
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(width = 10.dp, height = 8.dp)
                            .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(NavyCoral)
                    )
                }
            }

            // Floating Round "+" Button at Bottom-Right
            FloatingActionButton(
                onClick = onOpenAddPanel,
                shape = CircleShape,
                containerColor = NavyPrimaryDeep,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(4.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(48.dp)
                    .testTag("timeline_add_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Naya Layer Add Karein",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun formatTimecodeHHMMSS(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val mins = (totalSeconds / 60L) % 60L
    val secs = totalSeconds % 60L
    val frames = ((ms % 1000L) / 33L).coerceIn(0L, 29L)
    return "%02d:%02d:%02d".format(mins, secs, frames)
}

private fun iconForLayerType(type: LayerType): ImageVector {
    return when (type) {
        LayerType.GROUP -> Icons.Default.Folder
        LayerType.SHAPE -> Icons.Default.Category
        LayerType.TEXT -> Icons.Default.TextFields
        LayerType.VIDEO, LayerType.IMAGE -> Icons.Default.Image
        LayerType.AUDIO -> Icons.Default.MusicNote
        LayerType.DRAWING -> Icons.Default.Brush
        LayerType.ADJUSTMENT -> Icons.Default.Tune
        LayerType.SOLID, LayerType.GRADIENT, LayerType.CAMERA -> Icons.Default.Videocam
    }
}
