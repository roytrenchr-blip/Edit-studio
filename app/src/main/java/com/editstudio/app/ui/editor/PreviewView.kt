package com.editstudio.app.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.editstudio.app.domain.AspectRatio
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.Project
import com.editstudio.app.ui.theme.StudioBorder
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioDarkBg
import com.editstudio.app.ui.theme.StudioNeonPurple

@Composable
fun PreviewView(
    project: Project,
    currentTimeMs: Long,
    selectedLayer: Layer?,
    onTransformChange: (layerId: String, dx: Float, dy: Float, scaleFactor: Float, rotationDelta: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val aspect = when (project.aspectRatio) {
        AspectRatio.PORTRAIT_9_16 -> 9f / 16f
        AspectRatio.LANDSCAPE_16_9 -> 16f / 9f
        AspectRatio.SQUARE_1_1 -> 1f
        AspectRatio.VERTICAL_4_5 -> 4f / 5f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(aspect)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                .pointerInput(selectedLayer?.id) {
                    if (selectedLayer != null) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            onTransformChange(
                                selectedLayer.id,
                                pan.x,
                                pan.y,
                                zoom,
                                rotation
                            )
                        }
                    }
                }
        ) {
            // Live Preview Canvas rendering layers & bounding box
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)

                // Draw visible layers for current time
                for (layer in project.layers) {
                    if (!layer.isVisible || !layer.containsTime(currentTimeMs)) continue

                    val lx = center.x + layer.transform.x
                    val ly = center.y + layer.transform.y
                    val w = 220f * layer.transform.scaleX
                    val h = 120f * layer.transform.scaleY

                    // Draw placeholder visual block on canvas
                    val color = when (layer.type) {
                        com.editstudio.app.domain.LayerType.TEXT -> StudioNeonPurple
                        com.editstudio.app.domain.LayerType.SHAPE -> StudioCyberCyan
                        com.editstudio.app.domain.LayerType.VIDEO -> Color(0xFF4361EE)
                        com.editstudio.app.domain.LayerType.IMAGE -> Color(0xFFFF5470)
                        else -> Color(0xFF6C7086)
                    }

                    drawRoundRect(
                        color = color.copy(alpha = layer.transform.opacity * 0.7f),
                        topLeft = Offset(lx - w / 2f, ly - h / 2f),
                        size = Size(w, h),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                    )
                }

                // If a layer is selected, draw bounding box & corner handles
                if (selectedLayer != null && selectedLayer.containsTime(currentTimeMs)) {
                    val lx = center.x + selectedLayer.transform.x
                    val ly = center.y + selectedLayer.transform.y
                    val w = 230f * selectedLayer.transform.scaleX
                    val h = 130f * selectedLayer.transform.scaleY

                    // Bounding Box
                    drawRect(
                        color = StudioCyberCyan,
                        topLeft = Offset(lx - w / 2f, ly - h / 2f),
                        size = Size(w, h),
                        style = Stroke(width = 2.5f)
                    )

                    // 4 Corner Handles
                    val handleRadius = 8f
                    val corners = listOf(
                        Offset(lx - w / 2f, ly - h / 2f),
                        Offset(lx + w / 2f, ly - h / 2f),
                        Offset(lx - w / 2f, ly + h / 2f),
                        Offset(lx + w / 2f, ly + h / 2f)
                    )
                    for (c in corners) {
                        drawCircle(color = StudioCyberCyan, radius = handleRadius, center = c)
                        drawCircle(color = Color.White, radius = handleRadius / 2f, center = c)
                    }

                    // Rotation Top Handle
                    val topHandle = Offset(lx, ly - h / 2f - 24f)
                    drawLine(
                        color = StudioCyberCyan,
                        start = Offset(lx, ly - h / 2f),
                        end = topHandle,
                        strokeWidth = 2f
                    )
                    drawCircle(color = StudioNeonPurple, radius = 7f, center = topHandle)
                }
            }
        }
    }
}
