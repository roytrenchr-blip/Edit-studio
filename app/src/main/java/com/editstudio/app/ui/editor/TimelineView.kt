package com.editstudio.app.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.LayerType
import com.editstudio.app.domain.Project
import com.editstudio.app.ui.theme.KeyframeDiamond
import com.editstudio.app.ui.theme.StudioBorder
import com.editstudio.app.ui.theme.StudioCard
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioElectricCoral
import com.editstudio.app.ui.theme.StudioNeonPurple
import com.editstudio.app.ui.theme.StudioSurface
import com.editstudio.app.ui.theme.TextPrimary
import com.editstudio.app.ui.theme.TextSecondary
import com.editstudio.app.ui.theme.TimelinePlayhead
import com.editstudio.app.ui.theme.TimelineTrackBg
import com.editstudio.app.util.TimeFormat

@Composable
fun TimelineView(
    project: Project,
    currentTimeMs: Long,
    isPlaying: Boolean,
    selectedLayerId: String?,
    canUndo: Boolean,
    canRedo: Boolean,
    zoomFactor: Float,
    onSeek: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    onStepFrame: (Boolean) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSelectLayer: (String?) -> Unit,
    onTrimLayer: (layerId: String, newStart: Long, newDuration: Long) -> Unit,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val pxPerSecond = (100f * zoomFactor).coerceIn(40f, 400f)
    val totalWidthDp = ((project.durationMs / 1000f) * pxPerSecond).dp.coerceAtLeast(300.dp)
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TimelineTrackBg)
            .border(BorderStroke(1.dp, StudioBorder))
    ) {
        // Controls Row: Undo, Redo, Step Back, Play/Pause, Step Forward, Timecode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSurface)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Undo & Redo
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onUndo, enabled = canUndo) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo Karein",
                        tint = if (canUndo) TextPrimary else TextSecondary.copy(alpha = 0.4f)
                    )
                }
                IconButton(onClick = onRedo, enabled = canRedo) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = "Redo Karein",
                        tint = if (canRedo) TextPrimary else TextSecondary.copy(alpha = 0.4f)
                    )
                }
            }

            // Playback Transport
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onStepFrame(false) }) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Ek Frame Peeche",
                        tint = TextPrimary
                    )
                }
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(StudioNeonPurple)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Ruko" else "Chalao",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { onStepFrame(true) }) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Ek Frame Aage",
                        tint = TextPrimary
                    )
                }
            }

            // Current Timecode
            Text(
                text = "${TimeFormat.formatTimecode(currentTimeMs)} / ${TimeFormat.formatTimecode(project.durationMs)}",
                style = MaterialTheme.typography.labelSmall,
                color = StudioCyberCyan,
                fontWeight = FontWeight.Bold
            )
        }

        // Multi-Track Scrollable Timeline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .horizontalScroll(scrollState)
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        if (zoom != 1f) {
                            onZoomChange(zoomFactor * zoom)
                        }
                    }
                }
        ) {
            // Tracks Column
            Column(
                modifier = Modifier
                    .width(totalWidthDp)
                    .fillMaxSize()
            ) {
                // Time Ruler
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(StudioSurface)
                        .pointerInput(project.durationMs) {
                            detectDragGestures { change, _ ->
                                val timeMs = ((change.position.x / pxPerSecond) * 1000f).toLong()
                                onSeek(timeMs)
                            }
                        }
                ) {
                    val durationSec = project.durationMs / 1000f
                    for (sec in 0..durationSec.toInt()) {
                        val x = sec * pxPerSecond
                        drawLine(
                            color = StudioBorder,
                            start = Offset(x, 12f),
                            end = Offset(x, 24f),
                            strokeWidth = 1.5f
                        )
                    }
                }

                // Layer Tracks
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (layer in project.layers) {
                        val isSelected = layer.id == selectedLayerId
                        val startXDp = ((layer.startTimeMs / 1000f) * pxPerSecond).dp
                        val widthDp = ((layer.durationMs / 1000f) * pxPerSecond).dp.coerceAtLeast(30.dp)

                        val layerColor = when (layer.type) {
                            LayerType.TEXT -> StudioNeonPurple
                            LayerType.SHAPE -> StudioCyberCyan
                            LayerType.VIDEO -> Color(0xFF4361EE)
                            LayerType.IMAGE -> Color(0xFFFF5470)
                            LayerType.AUDIO -> Color(0xFFFFD166)
                            else -> StudioCard
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            // Layer Block
                            Box(
                                modifier = Modifier
                                    .offset(x = startXDp)
                                    .width(widthDp)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(layerColor.copy(alpha = if (isSelected) 0.95f else 0.7f))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color.White else StudioBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectLayer(layer.id) }
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = layer.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (layer.type == LayerType.SHAPE || layer.type == LayerType.AUDIO) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )

                                    // Diamond keyframe indicator if layer has keyframes
                                    if (layer.keyframeTracks.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(KeyframeDiamond)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Draggable Red Playhead
            val playheadXDp = ((currentTimeMs / 1000f) * pxPerSecond).dp
            Box(
                modifier = Modifier
                    .offset(x = playheadXDp - 1.dp)
                    .width(3.dp)
                    .fillMaxSize()
                    .background(TimelinePlayhead)
                    .pointerInput(project.durationMs) {
                        detectDragGestures { change, _ ->
                            val timeMs = ((change.position.x / pxPerSecond) * 1000f).toLong()
                            onSeek(timeMs)
                        }
                    }
            )
        }
    }
}

private fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
