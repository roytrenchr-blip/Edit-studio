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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editstudio.app.domain.EffectCategory
import com.editstudio.app.domain.EffectType
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.LayerEffect
import com.editstudio.app.ui.components.AnimatedThumbnailCard
import com.editstudio.app.ui.components.GridThumbnailItem
import com.editstudio.app.ui.components.SliderRow
import com.editstudio.app.ui.theme.StudioBorder
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioElectricCoral
import com.editstudio.app.ui.theme.StudioNeonPurple
import com.editstudio.app.ui.theme.StudioSurface
import com.editstudio.app.ui.theme.TextPrimary
import com.editstudio.app.ui.theme.TextSecondary

@Composable
fun EffectsSheet(
    layer: Layer?,
    onAddEffect: (EffectType) -> Unit,
    onRemoveEffect: (String) -> Unit,
    onUpdateEffect: (LayerEffect) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<EffectCategory?>(null) }
    var favoriteSet by remember { mutableStateOf(setOf<String>()) }

    val allEffects = remember { EffectType.entries }
    val filteredEffects = remember(searchQuery, selectedCategory) {
        allEffects.filter { effect ->
            val matchesQuery = searchQuery.isBlank() ||
                    effect.displayName.contains(searchQuery, ignoreCase = true) ||
                    effect.category.displayName.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || effect.category == selectedCategory
            matchesQuery && matchesCategory
        }
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
                text = "GPU Visual Effects",
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
            placeholder = { Text("Effect search karein...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StudioCyberCyan,
                unfocusedBorderColor = StudioBorder
            )
        )

        // Categories Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Sabhi") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StudioNeonPurple)
                )
            }
            items(EffectCategory.entries) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                    label = { Text(cat.displayName) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StudioNeonPurple)
                )
            }
        }

        // Active Layer Applied Effects List with simple intensity sliders
        if (layer != null && layer.effects.isNotEmpty()) {
            Text(
                text = "Applied Effects on ${layer.name}",
                style = MaterialTheme.typography.titleSmall,
                color = StudioCyberCyan,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
            for (eff in layer.effects) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SliderRow(
                        label = eff.type.displayName,
                        value = eff.intensity,
                        onValueChange = { onUpdateEffect(eff.copy(intensity = it)) },
                        valueRange = 0f..1f,
                        formatPattern = "%.0f%%",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onRemoveEffect(eff.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hatao", tint = StudioElectricCoral)
                    }
                }
            }
        }

        // Grid of Effects Thumbnails
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
        ) {
            items(filteredEffects) { effect ->
                val isSelected = layer?.effects?.any { it.type == effect } == true
                AnimatedThumbnailCard(
                    item = GridThumbnailItem(
                        id = effect.id,
                        title = effect.displayName,
                        subtitle = effect.category.displayName,
                        isFavorite = favoriteSet.contains(effect.id),
                        isSelected = isSelected
                    ),
                    onClick = { onAddEffect(effect) },
                    onToggleFavorite = {
                        favoriteSet = if (favoriteSet.contains(effect.id)) {
                            favoriteSet - effect.id
                        } else {
                            favoriteSet + effect.id
                        }
                    }
                )
            }
        }
    }
}
