package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.Project
import com.editstudio.app.engine.Timeline
import com.editstudio.app.ui.theme.NavyAmber
import com.editstudio.app.ui.theme.NavyPrimary
import com.editstudio.app.ui.theme.colorForLayerType
import kotlin.math.roundToInt

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LayerBar(
    layer: Layer,
    project: Project,
    isSelected: Boolean,
    pxPerMs: Float,
    leadingOffsetPx: Float,
    onSelect: () -> Unit,
    onMoveAndTrim: (startMs: Long, durationMs: Long) -> Unit,
    onCommitChange: () -> Unit,
    onReorder: (moveUp: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val barColor = colorForLayerType(layer.type)

    var showReorderMenu by remember { mutableStateOf(false) }
    var lastSnapTriggered by remember { mutableStateOf(false) }

    val startPx = leadingOffsetPx + layer.startTimeMs * pxPerMs
    val widthPx = (layer.durationMs * pxPerMs).coerceAtLeast(with(density) { 46.dp.toPx() })
    val widthDp = with(density) { widthPx.toDp() }

    Box(
        modifier = modifier
            .offset { IntOffset(startPx.roundToInt(), 0) }
            .width(widthDp)
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(barColor.copy(alpha = if (isSelected) 0.92f else 0.72f))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White else barColor.copy(alpha = 0.95f),
                shape = RoundedCornerShape(8.dp)
            )
            .combinedClickable(
                onClick = onSelect,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showReorderMenu = true
                }
            )
            .pointerInput(layer.id, pxPerMs) {
                var accumDx = 0f
                var origStart = layer.startTimeMs
                detectHorizontalDragGestures(
                    onDragStart = {
                        origStart = layer.startTimeMs
                        accumDx = 0f
                        lastSnapTriggered = false
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (layer.isLocked) return@detectHorizontalDragGestures
                        accumDx += dragAmount
                        val deltaMs = (accumDx / pxPerMs).toLong()
                        val rawStart = (origStart + deltaMs).coerceAtLeast(0L)
                        val (snappedStart, snapped) = Timeline.snapTimeWithResult(rawStart, project, layer.id)
                        if (snapped && !lastSnapTriggered) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        lastSnapTriggered = snapped
                        onMoveAndTrim(snappedStart, layer.durationMs)
                    },
                    onDragEnd = { onCommitChange() }
                )
            }
            .testTag("layer_bar_${layer.id}")
    ) {
        // Left Trim Edge Handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(12.dp)
                .fillMaxHeight()
                .background(if (isSelected) NavyPrimary.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.25f))
                .pointerInput(layer.id, pxPerMs) {
                    var accumDx = 0f
                    var origStart = layer.startTimeMs
                    var origDur = layer.durationMs
                    detectHorizontalDragGestures(
                        onDragStart = {
                            origStart = layer.startTimeMs
                            origDur = layer.durationMs
                            accumDx = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (layer.isLocked) return@detectHorizontalDragGestures
                            accumDx += dragAmount
                            val deltaMs = (accumDx / pxPerMs).toLong()
                            val newStart = (origStart + deltaMs).coerceIn(0L, origStart + origDur - 200L)
                            val newDur = (origDur - (newStart - origStart)).coerceAtLeast(200L)
                            onMoveAndTrim(newStart, newDur)
                        },
                        onDragEnd = { onCommitChange() }
                    )
                }
        )

        // Layer Name & Keyframe Diamonds
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp, end = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = layer.name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (layer.keyframeTracks.any { it.keyframes.isNotEmpty() }) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .rotate(45f)
                        .background(NavyAmber)
                )
            }
        }

        // Individual Keyframe Diamonds positioned accurately along the bar
        val allKfTimes = layer.keyframeTracks.flatMap { it.keyframes }.map { it.timeMs }.distinct()
        allKfTimes.forEach { kfLocalMs ->
            val kfOffsetDp = with(density) { (kfLocalMs * pxPerMs).toDp() }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = kfOffsetDp - 3.dp, y = (-3).dp)
                    .size(6.dp)
                    .rotate(45f)
                    .background(NavyAmber)
            )
        }

        // Right Drag / Trim Handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(18.dp)
                .fillMaxHeight()
                .background(if (isSelected) NavyPrimary.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.28f))
                .pointerInput(layer.id, pxPerMs) {
                    var accumDx = 0f
                    var origDur = layer.durationMs
                    detectHorizontalDragGestures(
                        onDragStart = {
                            origDur = layer.durationMs
                            accumDx = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (layer.isLocked) return@detectHorizontalDragGestures
                            accumDx += dragAmount
                            val deltaMs = (accumDx / pxPerMs).toLong()
                            val rawDur = (origDur + deltaMs).coerceAtLeast(250L)
                            val (snappedEnd, _) = Timeline.snapTimeWithResult(layer.startTimeMs + rawDur, project, layer.id)
                            val finalDur = (snappedEnd - layer.startTimeMs).coerceAtLeast(250L)
                            onMoveAndTrim(layer.startTimeMs, finalDur)
                        },
                        onDragEnd = { onCommitChange() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Trim Karein",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }

        DropdownMenu(
            expanded = showReorderMenu,
            onDismissRequest = { showReorderMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Upar Bhejein (Layer Up)") },
                leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null) },
                onClick = { onReorder(true); showReorderMenu = false }
            )
            DropdownMenuItem(
                text = { Text("Neeche Bhejein (Layer Down)") },
                leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null) },
                onClick = { onReorder(false); showReorderMenu = false }
            )
        }
    }
}
