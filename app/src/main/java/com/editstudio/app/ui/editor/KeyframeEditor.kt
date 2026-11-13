package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editstudio.app.domain.AnimProperty
import com.editstudio.app.domain.Keyframe
import com.editstudio.app.domain.KeyframeTrack
import com.editstudio.app.domain.Layer
import com.editstudio.app.engine.Keyframes
import com.editstudio.app.ui.theme.NavyAccentTeal
import com.editstudio.app.ui.theme.NavyAmber
import com.editstudio.app.ui.theme.NavyCoral
import com.editstudio.app.ui.theme.NavyPrimary
import com.editstudio.app.ui.theme.NavyPrimaryDeep
import com.editstudio.app.ui.theme.NavySurface
import com.editstudio.app.ui.theme.NavyTextPrimary

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
    val hasKeyframeAtPlayhead = Keyframes.hasKeyframeNear(layer, selectedProperty, layerTimeMs)

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
                text = "Pro Mode: Keyframes & Curves",
                style = MaterialTheme.typography.titleMedium,
                color = NavyTextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = NavyTextPrimary)
            }
        }

        // Property selector chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                AnimProperty.POSITION_X,
                AnimProperty.POSITION_Y,
                AnimProperty.SCALE_X,
                AnimProperty.ROTATION,
                AnimProperty.OPACITY
            ).forEach { prop ->
                FilterChip(
                    selected = selectedProperty == prop,
                    onClick = { selectedProperty = prop },
                    label = { Text(prop.label, fontSize = 11.sp) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(45f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(NavyAmber)
                )
                Text(
                    text = "${selectedProperty.label} @ ${layerTimeMs}ms",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavyPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    val curVal = Keyframes.evaluateProperty(layer, selectedProperty, layerTimeMs, 1.0f)
                    onUpdateLayer(Keyframes.toggleKeyframe(layer, selectedProperty, layerTimeMs, curVal))
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasKeyframeAtPlayhead) NavyCoral else NavyPrimaryDeep
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
                Text("Smooth Keyframes", color = NavyPrimary)
            }

            Button(
                onClick = onOpenCurveEditor,
                colors = ButtonDefaults.buttonColors(containerColor = NavyAccentTeal, contentColor = Color.Black),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Curve Editor", fontWeight = FontWeight.Bold)
            }
        }
    }
}
