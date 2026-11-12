package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.Transform
import com.editstudio.app.ui.components.SliderRow
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioNeonPurple
import com.editstudio.app.ui.theme.StudioSurface
import com.editstudio.app.ui.theme.TextPrimary
import com.editstudio.app.ui.theme.TextSecondary

@Composable
fun PropertyPanel(
    layer: Layer,
    isSimpleMode: Boolean,
    onUpdateTransform: (Transform) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = layer.transform
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${layer.name} Properties",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
        }

        // Scale & Opacity
        SliderRow(
            label = "Scale (Size)",
            value = t.scaleX,
            onValueChange = { onUpdateTransform(t.copy(scaleX = it, scaleY = it)) },
            valueRange = 0.1f..4.0f,
            formatPattern = "%.2fx"
        )

        SliderRow(
            label = "Opacity (Transparancy)",
            value = t.opacity,
            onValueChange = { onUpdateTransform(t.copy(opacity = it)) },
            valueRange = 0f..1f,
            formatPattern = "%.0f%%",
            unit = ""
        )

        SliderRow(
            label = "Rotation (Ghoomana)",
            value = t.rotationZ,
            onValueChange = { onUpdateTransform(t.copy(rotationZ = it)) },
            valueRange = -180f..180f,
            formatPattern = "%.0f°"
        )

        // Flips
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = t.flipHorizontal,
                onClick = { onUpdateTransform(t.copy(flipHorizontal = !t.flipHorizontal)) },
                label = { Text("Flip Horizontal") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StudioNeonPurple.copy(alpha = 0.3f),
                    selectedLabelColor = StudioCyberCyan
                )
            )
            FilterChip(
                selected = t.flipVertical,
                onClick = { onUpdateTransform(t.copy(flipVertical = !t.flipVertical)) },
                label = { Text("Flip Vertical") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StudioNeonPurple.copy(alpha = 0.3f),
                    selectedLabelColor = StudioCyberCyan
                )
            )
        }

        // Pro Mode extra controls: 3D Tilts and Position
        if (!isSimpleMode) {
            Text(
                text = "Pro Mode: 3D & Position",
                style = MaterialTheme.typography.titleSmall,
                color = StudioCyberCyan,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )

            SliderRow(
                label = "X Position",
                value = t.x,
                onValueChange = { onUpdateTransform(t.copy(x = it)) },
                valueRange = -500f..500f,
                formatPattern = "%.0f px"
            )

            SliderRow(
                label = "Y Position",
                value = t.y,
                onValueChange = { onUpdateTransform(t.copy(y = it)) },
                valueRange = -500f..500f,
                formatPattern = "%.0f px"
            )

            SliderRow(
                label = "3D Tilt X (Perspective)",
                value = t.rotationX,
                onValueChange = { onUpdateTransform(t.copy(rotationX = it)) },
                valueRange = -60f..60f,
                formatPattern = "%.0f°"
            )

            SliderRow(
                label = "3D Pan Y",
                value = t.rotationY,
                onValueChange = { onUpdateTransform(t.copy(rotationY = it)) },
                valueRange = -60f..60f,
                formatPattern = "%.0f°"
            )
        }
    }
}
