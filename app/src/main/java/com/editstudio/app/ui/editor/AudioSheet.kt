package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editstudio.app.domain.AudioReactiveConfig
import com.editstudio.app.domain.Layer
import com.editstudio.app.ui.components.AppPrimaryButton
import com.editstudio.app.ui.components.SliderRow
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioGoldenAmber
import com.editstudio.app.ui.theme.StudioNeonPurple
import com.editstudio.app.ui.theme.StudioSurface
import com.editstudio.app.ui.theme.TextPrimary

@Composable
fun AudioSheet(
    selectedLayer: Layer?,
    onUpdateLayer: (Layer) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audioReactive = selectedLayer?.audioReactive ?: AudioReactiveConfig()

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
                text = "Audio & Beat Reactive Motion",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
        }

        // Beat Sync Marker Button
        AppPrimaryButton(
            text = "Automatic Beat Sync (Markers Lagao)",
            icon = Icons.Default.GraphicEq,
            onClick = {
                if (selectedLayer != null) {
                    onUpdateLayer(selectedLayer.copy(audioReactive = audioReactive.copy(isEnabled = true)))
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
        )

        // Audio Reactive Targets
        Text(
            text = "React to Audio Targets (Music par nachao)",
            style = MaterialTheme.typography.titleSmall,
            color = StudioCyberCyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = audioReactive.targetScale,
                onClick = {
                    if (selectedLayer != null) {
                        onUpdateLayer(selectedLayer.copy(audioReactive = audioReactive.copy(isEnabled = true, targetScale = !audioReactive.targetScale)))
                    }
                },
                label = { Text("Scale Pulse") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StudioNeonPurple)
            )
            FilterChip(
                selected = audioReactive.targetShake,
                onClick = {
                    if (selectedLayer != null) {
                        onUpdateLayer(selectedLayer.copy(audioReactive = audioReactive.copy(isEnabled = true, targetShake = !audioReactive.targetShake)))
                    }
                },
                label = { Text("Bass Shake") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StudioNeonPurple)
            )
            FilterChip(
                selected = audioReactive.targetOpacity,
                onClick = {
                    if (selectedLayer != null) {
                        onUpdateLayer(selectedLayer.copy(audioReactive = audioReactive.copy(isEnabled = true, targetOpacity = !audioReactive.targetOpacity)))
                    }
                },
                label = { Text("Strobe Opacity") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StudioNeonPurple)
            )
        }

        SliderRow(
            label = "Sensitivity (Sensitivity Badhao)",
            value = audioReactive.sensitivity,
            onValueChange = {
                if (selectedLayer != null) {
                    onUpdateLayer(selectedLayer.copy(audioReactive = audioReactive.copy(sensitivity = it)))
                }
            },
            valueRange = 0.5f..3.0f,
            formatPattern = "%.1fx",
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
