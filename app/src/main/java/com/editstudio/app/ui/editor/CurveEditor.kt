package com.editstudio.app.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editstudio.app.domain.BezierControlPoints
import com.editstudio.app.domain.Layer
import com.editstudio.app.ui.components.AppPrimaryButton
import com.editstudio.app.ui.theme.NavyAmber
import com.editstudio.app.ui.theme.NavyBg
import com.editstudio.app.ui.theme.NavyBorder
import com.editstudio.app.ui.theme.NavyPrimary
import com.editstudio.app.ui.theme.NavyPrimaryDeep
import com.editstudio.app.ui.theme.NavySurface
import com.editstudio.app.ui.theme.NavyTextPrimary
import com.editstudio.app.ui.theme.NavyTextSecondary

@Composable
fun CurveEditor(
    layer: Layer,
    onApplyCurve: (BezierControlPoints) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var p1x by remember { mutableFloatStateOf(0.25f) }
    var p1y by remember { mutableFloatStateOf(0.1f) }
    var p2x by remember { mutableFloatStateOf(0.25f) }
    var p2y by remember { mutableFloatStateOf(1.0f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(NavySurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Interactive Bezier Curve Editor",
                style = MaterialTheme.typography.titleMedium,
                color = NavyTextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = NavyTextPrimary)
            }
        }

        Text(
            text = "Handles ko drag karke custom easing curve banayein",
            style = MaterialTheme.typography.bodySmall,
            color = NavyTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = false,
                onClick = { p1x = 0.42f; p1y = 0f; p2x = 1f; p2y = 1f },
                label = { Text("Ease In") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyPrimaryDeep)
            )
            FilterChip(
                selected = false,
                onClick = { p1x = 0f; p1y = 0f; p2x = 0.58f; p2y = 1f },
                label = { Text("Ease Out") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyPrimaryDeep)
            )
            FilterChip(
                selected = false,
                onClick = { p1x = 0.42f; p1y = 0f; p2x = 0.58f; p2y = 1f },
                label = { Text("Ease In-Out") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyPrimaryDeep)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NavyBg)
                .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val nx = (change.position.x / w).coerceIn(0f, 1f)
                        val ny = (1f - (change.position.y / h)).coerceIn(0f, 1f)
                        if (nx < 0.5f) {
                            p1x = nx
                            p1y = ny
                        } else {
                            p2x = nx
                            p2y = ny
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val w = size.width
                val h = size.height
                val start = Offset(0f, h)
                val end = Offset(w, 0f)
                val c1 = Offset(p1x * w, h - (p1y * h))
                val c2 = Offset(p2x * w, h - (p2y * h))

                drawLine(color = NavyBorder, start = start, end = c1, strokeWidth = 2f)
                drawLine(color = NavyBorder, start = end, end = c2, strokeWidth = 2f)

                val curvePath = Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(c1.x, c1.y, c2.x, c2.y, end.x, end.y)
                }
                drawPath(path = curvePath, color = NavyPrimary, style = Stroke(width = 4f))

                drawCircle(color = NavyPrimaryDeep, radius = 10f, center = c1)
                drawCircle(color = NavyAmber, radius = 10f, center = c2)
            }
        }

        AppPrimaryButton(
            text = "Apply Custom Curve",
            onClick = {
                onApplyCurve(BezierControlPoints(p1x, p1y, p2x, p2y))
                onClose()
            },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )
    }
}
