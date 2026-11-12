package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editstudio.app.domain.AnimProperty
import com.editstudio.app.domain.Keyframe
import com.editstudio.app.domain.KeyframeTrack
import com.editstudio.app.domain.Layer
import com.editstudio.app.engine.Keyframes
import com.editstudio.app.ui.components.AppPrimaryButton
import com.editstudio.app.ui.theme.KeyframeDiamond
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioElectricCoral
import com.editstudio.app.ui.theme.StudioNeonPurple
import com.editstudio.app.ui.theme.StudioSurface
import com.editstudio.app.ui.theme.TextPrimary

@Composable
fun KeyframeEditor(
    layer: Layer,
    currentTimeMs: Long,
    onUpdateLayer: (Layer) -> Unit,
    onOpenCurveEditor: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedProperty by remember { mutableStateOf(AnimProperty.SCALE_X) }
    val layerTimeMs = (currentTimeMs - layer.startTimeMs).coerceAtLeast(0L)

    val currentTrack = layer.keyframeTracks.firstOrNull { it.property == selectedProperty }
    val hasKeyframeAtPlayhead = currentTrack?.keyframes?.any { it.timeMs == layerTimeMs } == true

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
                text = "Pro Mode: Keyframes & Curves",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
        }

        // Diamond Keyframe Status
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(KeyframeDiamond)
                )
                Text(
                    text = "${selectedProperty.label} Keyframe @ ${layerTimeMs}ms",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioCyberCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Add or Remove Keyframe diamond button
            Button(
                onClick = {
                    val tracks = layer.keyframeTracks.toMutableList()
                    val existingTrackIndex = tracks.indexOfFirst { it.property == selectedProperty }
                    if (hasKeyframeAtPlayhead) {
                        // Remove
                        if (existingTrackIndex != -1) {
                            val updatedKfs = tracks[existingTrackIndex].keyframes.filter { it.timeMs != layerTimeMs }
                            tracks[existingTrackIndex] = tracks[existingTrackIndex].copy(keyframes = updatedKfs)
                        }
                    } else {
                        // Add
                        val currentVal = Keyframes.evaluateProperty(layer, selectedProperty, layerTimeMs, 1.0f)
                        val newKf = Keyframe(timeMs = layerTimeMs, value = currentVal)
                        if (existingTrackIndex != -1) {
                            val updatedKfs = (tracks[existingTrackIndex].keyframes + newKf).sortedBy { it.timeMs }
                            tracks[existingTrackIndex] = tracks[existingTrackIndex].copy(keyframes = updatedKfs)
                        } else {
                            tracks.add(KeyframeTrack(property = selectedProperty, keyframes = listOf(newKf)))
                        }
                    }
                    onUpdateLayer(layer.copy(keyframeTracks = tracks))
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasKeyframeAtPlayhead) StudioElectricCoral else StudioNeonPurple
                )
            ) {
                Icon(
                    imageVector = if (hasKeyframeAtPlayhead) Icons.Default.Delete else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(if (hasKeyframeAtPlayhead) "Keyframe Hatao" else "Keyframe Lagao")
            }
        }

        // Smooth Keyframes & Curve Editor actions
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (currentTrack != null) {
                        val smoothed = Keyframes.smoothKeyframes(currentTrack)
                        val updatedTracks = layer.keyframeTracks.map { if (it.property == selectedProperty) smoothed else it }
                        onUpdateLayer(layer.copy(keyframeTracks = updatedTracks))
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Smooth Keyframes", color = StudioCyberCyan)
            }

            Button(
                onClick = onOpenCurveEditor,
                colors = ButtonDefaults.buttonColors(containerColor = StudioCyberCyan, contentColor = Color.Black),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Curve Editor", fontWeight = FontWeight.Bold)
            }
        }
    }
}
