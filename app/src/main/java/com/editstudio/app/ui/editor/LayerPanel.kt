package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.editstudio.app.domain.BlendMode
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.Project
import com.editstudio.app.ui.theme.StudioBorder
import com.editstudio.app.ui.theme.StudioCard
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioNeonPurple
import com.editstudio.app.ui.theme.StudioSurface
import com.editstudio.app.ui.theme.TextPrimary
import com.editstudio.app.ui.theme.TextSecondary

@Composable
fun LayerPanel(
    project: Project,
    selectedLayerId: String?,
    onSelectLayer: (String?) -> Unit,
    onUpdateLayer: (Layer) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = "Layers & Blend Modes",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Band Karein", tint = TextPrimary)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(project.layers.reversed()) { layer ->
                val isSelected = layer.id == selectedLayerId
                LayerRowItem(
                    layer = layer,
                    allLayers = project.layers,
                    isSelected = isSelected,
                    onSelect = { onSelectLayer(layer.id) },
                    onUpdate = onUpdateLayer
                )
            }
        }
    }
}

@Composable
private fun LayerRowItem(
    layer: Layer,
    allLayers: List<Layer>,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onUpdate: (Layer) -> Unit
) {
    var blendMenuExpanded by remember { mutableStateOf(false) }
    var parentMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) StudioNeonPurple.copy(alpha = 0.2f) else StudioCard)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) StudioCyberCyan else StudioBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = layer.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) StudioCyberCyan else TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Visibility Toggle
                    IconButton(
                        onClick = { onUpdate(layer.copy(isVisible = !layer.isVisible)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Visibility",
                            tint = if (layer.isVisible) StudioCyberCyan else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Lock Toggle
                    IconButton(
                        onClick = { onUpdate(layer.copy(isLocked = !layer.isLocked)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Lock",
                            tint = if (layer.isLocked) StudioNeonPurple else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Blend Mode & Parenting Selectors
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Blend Mode Button
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioSurface)
                            .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                            .clickable { blendMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Blend: ${layer.blendMode.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = StudioNeonPurple,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    DropdownMenu(
                        expanded = blendMenuExpanded,
                        onDismissRequest = { blendMenuExpanded = false }
                    ) {
                        for (mode in BlendMode.entries) {
                            DropdownMenuItem(
                                text = { Text(mode.displayName) },
                                onClick = {
                                    onUpdate(layer.copy(blendMode = mode))
                                    blendMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Parenting Button
                Box {
                    val parentName = allLayers.firstOrNull { it.id == layer.parentLayerId }?.name ?: "Parent: None"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioSurface)
                            .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                            .clickable { parentMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = parentName,
                            style = MaterialTheme.typography.labelSmall,
                            color = StudioCyberCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    DropdownMenu(
                        expanded = parentMenuExpanded,
                        onDismissRequest = { parentMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (No Parent)") },
                            onClick = {
                                onUpdate(layer.copy(parentLayerId = null))
                                parentMenuExpanded = false
                            }
                        )
                        for (other in allLayers) {
                            if (other.id != layer.id) {
                                DropdownMenuItem(
                                    text = { Text(other.name) },
                                    onClick = {
                                        onUpdate(layer.copy(parentLayerId = other.id))
                                        parentMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
