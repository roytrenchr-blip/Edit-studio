package com.editstudio.app.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.PathAnimationConfig
import com.editstudio.app.domain.PathPoint
import com.editstudio.app.ui.components.AppPrimaryButton
import com.editstudio.app.ui.theme.NavyAccentTeal
import com.editstudio.app.ui.theme.NavyAmber
import com.editstudio.app.ui.theme.NavyBg
import com.editstudio.app.ui.theme.NavyBorder
import com.editstudio.app.ui.theme.NavyPrimary
import com.editstudio.app.ui.theme.NavySurface
import com.editstudio.app.ui.theme.NavyTextPrimary
import com.editstudio.app.ui.theme.NavyTextSecondary

@Composable
fun PathEditor(
    layer: Layer,
    onUpdatePath: (PathAnimationConfig) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var config by remember { mutableStateOf(layer.pathAnimation) }
    var points by remember { mutableStateOf(config.points.toMutableList()) }

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
                text = "Pen Tool Motion Path",
                style = MaterialTheme.typography.titleMedium,
                color = NavyTextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = NavyTextPrimary)
            }
        }

        Text(
            text = "Canvas par tap karke rasta (path) banayein",
            style = MaterialTheme.typography.bodySmall,
            color = NavyTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NavyBg)
                .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newPt = PathPoint(offset.x, offset.y, offset.x, offset.y)
                        val updated = points.toMutableList().apply { add(newPt) }
                        points = updated
                        config = config.copy(points = updated, isEnabled = true)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (points.size >= 2) {
                    val path = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(path = path, color = NavyPrimary, style = Stroke(width = 3.5f))
                }
                for (pt in points) {
                    drawCircle(color = NavyAmber, radius = 6f, center = Offset(pt.x, pt.y))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Auto-Orient (Raste ke mudav par ghoomein)", color = NavyTextPrimary)
            Switch(
                checked = config.autoOrient,
                onCheckedChange = { config = config.copy(autoOrient = it) },
                colors = SwitchDefaults.colors(checkedThumbColor = NavyAccentTeal)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Loop Motion Path", color = NavyTextPrimary)
            Switch(
                checked = config.isLooping,
                onCheckedChange = { config = config.copy(isLooping = it) },
                colors = SwitchDefaults.colors(checkedThumbColor = NavyPrimary)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppPrimaryButton(
                text = "Apply Motion Path",
                onClick = {
                    onUpdatePath(config)
                    onClose()
                },
                modifier = Modifier.weight(1f)
            )
            AppPrimaryButton(
                text = "Path Reset",
                onClick = {
                    points = mutableListOf()
                    config = config.copy(points = emptyList(), isEnabled = false)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
