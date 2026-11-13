package com.editstudio.app.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.SpeedPoint
import com.editstudio.app.domain.SpeedRampConfig
import com.editstudio.app.domain.SpeedRampPreset
import com.editstudio.app.motion.SpeedRamp
import com.editstudio.app.ui.components.AppPrimaryButton
import com.editstudio.app.ui.theme.StudioBorder
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioGoldenAmber
import com.editstudio.app.ui.theme.StudioNeonPurple
import com.editstudio.app.ui.theme.StudioSurface
import com.editstudio.app.ui.theme.TextPrimary
import com.editstudio.app.ui.theme.TextSecondary

@Composable
fun SpeedRampEditor(
    layer: Layer,
    onUpdateSpeedRamp: (SpeedRampConfig) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var config by remember { mutableStateOf(layer.speedRamp) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Speed Ramp & Frame Blending",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
        }

        // Preset Chips
        Text(
            text = "Speed Curve Presets (0.1x se 10x)",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SpeedRampPreset.entries) { preset ->
                FilterChip(
                    selected = config.preset == preset,
                    onClick = {
                        val points = SpeedRamp.getPresetPoints(preset)
                        config = config.copy(preset = preset, points = points, isEnabled = true)
                    },
                    label = { Text(preset.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StudioNeonPurple,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Interactive Speed Curve Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F0E18))
                .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                .pointerInput(config.points) {
                    detectDragGestures { change, _ ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val nx = (change.position.x / w).coerceIn(0f, 1f)
                        val ny = (1f - (change.position.y / h)).coerceIn(0f, 1f)
                        val speedMult = ny * 10f // 0 to 10x
                        val updated = config.points.map { pt ->
                            if (kotlin.math.abs(pt.normalizedTime - nx) < 0.2f) {
                                pt.copy(speedMultiplier = speedMult)
                            } else pt
                        }
                        config = config.copy(points = updated, isEnabled = true)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                val w = size.width
                val h = size.height

                // Center 1.0x baseline
                val baseLineY = h - (1f / 10f * h)
                drawLine(color = StudioBorder, start = Offset(0f, baseLineY), end = Offset(w, baseLineY), strokeWidth = 1.5f)

                val sorted = config.points.sortedBy { it.normalizedTime }
                if (sorted.size >= 2) {
                    val path = Path()
                    for (i in sorted.indices) {
                        val px = sorted[i].normalizedTime * w
                        val py = h - ((sorted[i].speedMultiplier / 10f).coerceIn(0f, 1f) * h)
                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }
                    drawPath(path = path, color = StudioCyberCyan, style = Stroke(width = 3.5f))

                    for (pt in sorted) {
                        val px = pt.normalizedTime * w
                        val py = h - ((pt.speedMultiplier / 10f).coerceIn(0f, 1f) * h)
                        drawCircle(color = StudioGoldenAmber, radius = 7f, center = Offset(px, py))
                    }
                }
            }
        }

        // Toggles: Optical Flow Frame Blending & Reverse Video
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Optical Flow Frame Blending", color = TextPrimary)
            Switch(
                checked = config.opticalFlowBlending,
                onCheckedChange = { config = config.copy(opticalFlowBlending = it) },
                colors = SwitchDefaults.colors(checkedThumbColor = StudioCyberCyan)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Reverse Playback (Ulta Chalao)", color = TextPrimary)
            Switch(
                checked = config.isReversed,
                onCheckedChange = { config = config.copy(isReversed = it) },
                colors = SwitchDefaults.colors(checkedThumbColor = StudioGoldenAmber)
            )
        }

        AppPrimaryButton(
            text = "Apply Speed Ramp",
            onClick = {
                onUpdateSpeedRamp(config)
                onClose()
            },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )
    }
}
