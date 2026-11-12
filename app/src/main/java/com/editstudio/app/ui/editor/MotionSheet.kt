package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editstudio.app.domain.EasingType
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.MotionGroup
import com.editstudio.app.domain.MotionPhase
import com.editstudio.app.domain.MotionPresetType
import com.editstudio.app.motion.MotionRegistry
import com.editstudio.app.ui.components.AnimatedThumbnailCard
import com.editstudio.app.ui.components.GridThumbnailItem
import com.editstudio.app.ui.components.SliderRow
import com.editstudio.app.ui.theme.StudioBorder
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioNeonPurple
import com.editstudio.app.ui.theme.StudioSurface
import com.editstudio.app.ui.theme.TextPrimary
import com.editstudio.app.ui.theme.TextSecondary

@Composable
fun MotionSheet(
    layer: Layer?,
    onApplyMotion: (MotionPresetType) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPhaseIndex by remember { mutableIntStateOf(0) }
    var selectedGroup by remember { mutableStateOf<MotionGroup?>(null) }
    var favoriteSet by remember { mutableStateOf(setOf<String>()) }

    var durationMs by remember { mutableStateOf(800L) }
    var selectedEasing by remember { mutableStateOf(EasingType.EASE_OUT) }

    val phases = listOf(MotionPhase.IN, MotionPhase.OUT, MotionPhase.LOOP)
    val currentPhase = phases[selectedPhaseIndex]

    val items = remember(searchQuery, selectedGroup, currentPhase) {
        MotionRegistry.filter(searchQuery, selectedGroup, currentPhase)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "One-Tap Motion Presets",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Motion search karein...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StudioCyberCyan,
                unfocusedBorderColor = StudioBorder
            )
        )

        // Phase Tabs: In, Out, Loop
        ScrollableTabRow(
            selectedTabIndex = selectedPhaseIndex,
            containerColor = StudioSurface,
            contentColor = StudioCyberCyan,
            edgePadding = 0.dp,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            phases.forEachIndexed { index, phase ->
                Tab(
                    selected = selectedPhaseIndex == index,
                    onClick = { selectedPhaseIndex = index },
                    text = { Text(phase.label, fontWeight = if (selectedPhaseIndex == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedGroup == null,
                    onClick = { selectedGroup = null },
                    label = { Text("Sabhi") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StudioNeonPurple)
                )
            }
            items(MotionGroup.entries) { group ->
                FilterChip(
                    selected = selectedGroup == group,
                    onClick = { selectedGroup = if (selectedGroup == group) null else group },
                    label = { Text(group.displayName) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StudioNeonPurple)
                )
            }
        }

        // Duration Slider & Easing Choice for One-Tap
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SliderRow(
                label = "Duration (Raftaar)",
                value = durationMs / 1000f,
                onValueChange = { durationMs = (it * 1000f).toLong() },
                valueRange = 0.2f..3.0f,
                formatPattern = "%.1fs",
                modifier = Modifier.weight(1f)
            )
        }

        // Grid of Animated Thumbnails
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
        ) {
            items(items) { item ->
                val isSelected = layer?.motionPresets?.any { it.presetType == item.type } == true
                AnimatedThumbnailCard(
                    item = GridThumbnailItem(
                        id = item.type.id,
                        title = item.title,
                        subtitle = item.hinglishDesc,
                        isFavorite = favoriteSet.contains(item.type.id),
                        isSelected = isSelected
                    ),
                    onClick = { onApplyMotion(item.type) },
                    onToggleFavorite = {
                        favoriteSet = if (favoriteSet.contains(item.type.id)) {
                            favoriteSet - item.type.id
                        } else {
                            favoriteSet + item.type.id
                        }
                    }
                )
            }
        }
    }
}
