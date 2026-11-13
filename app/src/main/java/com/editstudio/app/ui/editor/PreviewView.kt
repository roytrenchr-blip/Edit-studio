package com.editstudio.app.ui.editor

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editstudio.app.domain.AnimProperty
import com.editstudio.app.domain.AspectRatio
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.LayerType
import com.editstudio.app.domain.PathPoint
import com.editstudio.app.domain.Project
import com.editstudio.app.engine.Keyframes
import com.editstudio.app.motion.MotionPresets
import com.editstudio.app.render.SharedFrameRenderer
import com.editstudio.app.ui.theme.NavyAccentTeal
import com.editstudio.app.ui.theme.NavyAmber
import com.editstudio.app.ui.theme.NavyBg
import com.editstudio.app.ui.theme.NavyBorder
import com.editstudio.app.ui.theme.NavyCard
import com.editstudio.app.ui.theme.NavyPrimary
import com.editstudio.app.ui.theme.NavySurface

@Composable
fun PreviewView(
    project: Project,
    currentTimeMs: Long,
    selectedLayer: Layer?,
    isDrawingActive: Boolean,
    isVectorDrawing: Boolean,
    onTransformChange: (layerId: String, dx: Float, dy: Float, scaleFactor: Float, rotationDelta: Float) -> Unit,
    onTransformCommit: () -> Unit,
    onAddDrawingStroke: (List<PathPoint>) -> Unit,
    onFinishDrawing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aspect = when (project.aspectRatio) {
        AspectRatio.PORTRAIT_9_16 -> 9f / 16f
        AspectRatio.LANDSCAPE_16_9 -> 16f / 9f
        AspectRatio.SQUARE_1_1 -> 1f
        AspectRatio.VERTICAL_4_5 -> 4f / 5f
    }

    val activeStrokePoints = remember { mutableStateListOf<PathPoint>() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(NavyBg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(aspect)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                .pointerInput(selectedLayer?.id, isDrawingActive) {
                    if (isDrawingActive && selectedLayer?.type == LayerType.DRAWING) {
                        detectDragGestures(
                            onDragStart = { start ->
                                activeStrokePoints.clear()
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val scale = 1080f / size.width.toFloat().coerceAtLeast(1f)
                                val lx = (start.x - cx) * scale
                                val ly = (start.y - cy) * scale
                                activeStrokePoints.add(PathPoint(lx, ly))
                            },
                            onDrag = { change, _ ->
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val scale = 1080f / size.width.toFloat().coerceAtLeast(1f)
                                val lx = (change.position.x - cx) * scale
                                val ly = (change.position.y - cy) * scale
                                activeStrokePoints.add(PathPoint(lx, ly))
                            },
                            onDragEnd = {
                                onAddDrawingStroke(activeStrokePoints.toList())
                                activeStrokePoints.clear()
                            }
                        )
                    } else if (selectedLayer != null) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            val scale = 1080f / size.width.toFloat().coerceAtLeast(1f)
                            onTransformChange(
                                selectedLayer.id,
                                pan.x * scale,
                                pan.y * scale,
                                zoom,
                                rotation
                            )
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Transparent Checkerboard Background
                val tileSize = 18.dp.toPx()
                val cols = (size.width / tileSize).toInt() + 1
                val rows = (size.height / tileSize).toInt() + 1
                for (r in 0..rows) {
                    for (c in 0..cols) {
                        val isEven = (r + c) % 2 == 0
                        drawRect(
                            color = if (isEven) Color(0xFF10192E) else Color(0xFF18243E),
                            topLeft = Offset(c * tileSize, r * tileSize),
                            size = Size(tileSize, tileSize)
                        )
                    }
                }

                // 2. Render all active layers via SharedFrameRenderer onto nativeCanvas
                SharedFrameRenderer.drawProjectFrame(
                    canvas = drawContext.canvas.nativeCanvas,
                    project = project,
                    timeMs = currentTimeMs,
                    width = size.width,
                    height = size.height,
                    isExport = false
                )

                // 3. In-progress live drawing stroke preview
                if (activeStrokePoints.size >= 2) {
                    val scaleToCanvas = size.width / 1080f
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.parseColor("#38BDF8")
                        style = Paint.Style.STROKE
                        strokeWidth = 8f * scaleToCanvas
                        strokeCap = Paint.Cap.ROUND
                    }
                    val path = android.graphics.Path()
                    path.moveTo(cx + activeStrokePoints[0].x * scaleToCanvas, cy + activeStrokePoints[0].y * scaleToCanvas)
                    for (i in 1 until activeStrokePoints.size) {
                        path.lineTo(cx + activeStrokePoints[i].x * scaleToCanvas, cy + activeStrokePoints[i].y * scaleToCanvas)
                    }
                    drawContext.canvas.nativeCanvas.drawPath(path, paint)
                }

                // 4. Selection bounding box & corner transform handles
                if (selectedLayer != null && selectedLayer.containsTime(currentTimeMs) && !isDrawingActive) {
                    val scaleToCanvas = size.width / 1080f
                    val localTime = (currentTimeMs - selectedLayer.startTimeMs).coerceAtLeast(0L)
                    val motion = MotionPresets.evaluatePresets(selectedLayer.motionPresets, localTime, selectedLayer.durationMs)
                    val lx = size.width / 2f + (Keyframes.evaluateProperty(selectedLayer, AnimProperty.POSITION_X, localTime, selectedLayer.transform.x) + motion.offsetX) * scaleToCanvas
                    val ly = size.height / 2f + (Keyframes.evaluateProperty(selectedLayer, AnimProperty.POSITION_Y, localTime, selectedLayer.transform.y) + motion.offsetY) * scaleToCanvas
                    val sx = Keyframes.evaluateProperty(selectedLayer, AnimProperty.SCALE_X, localTime, selectedLayer.transform.scaleX) * motion.scaleX
                    val sy = Keyframes.evaluateProperty(selectedLayer, AnimProperty.SCALE_Y, localTime, selectedLayer.transform.scaleY) * motion.scaleY

                    val boxW = (300f * sx * scaleToCanvas).coerceAtLeast(56f)
                    val boxH = (220f * sy * scaleToCanvas).coerceAtLeast(56f)

                    drawRect(
                        color = NavyPrimary,
                        topLeft = Offset(lx - boxW / 2f, ly - boxH / 2f),
                        size = Size(boxW, boxH),
                        style = Stroke(width = 2.5f)
                    )

                    val corners = listOf(
                        Offset(lx - boxW / 2f, ly - boxH / 2f),
                        Offset(lx + boxW / 2f, ly - boxH / 2f),
                        Offset(lx - boxW / 2f, ly + boxH / 2f),
                        Offset(lx + boxW / 2f, ly + boxH / 2f)
                    )
                    corners.forEach { corner ->
                        drawCircle(color = NavyPrimary, radius = 8f, center = corner)
                        drawCircle(color = Color.White, radius = 4f, center = corner)
                    }

                    val rotHandle = Offset(lx, ly - boxH / 2f - 24f)
                    drawLine(
                        color = NavyPrimary,
                        start = Offset(lx, ly - boxH / 2f),
                        end = rotHandle,
                        strokeWidth = 2f
                    )
                    drawCircle(color = NavyAccentTeal, radius = 7f, center = rotHandle)
                }
            }

            if (isDrawingActive && selectedLayer?.type == LayerType.DRAWING) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavySurface.copy(alpha = 0.9f))
                        .border(1.dp, NavyAmber, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isVectorDrawing) "Vector Draw ON — Ungli se banayein" else "Freehand Draw ON — Ungli se likhein",
                        color = NavyAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    androidx.compose.material3.TextButton(onClick = onFinishDrawing) {
                        Text("Done", color = NavyPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
