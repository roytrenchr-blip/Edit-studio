package com.editstudio.app.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editstudio.app.domain.MotionGroup
import com.editstudio.app.domain.MotionPhase
import com.editstudio.app.domain.MotionPresetType
import com.editstudio.app.store.StoreItem
import com.editstudio.app.ui.theme.NavyAccentTeal
import com.editstudio.app.ui.theme.NavyAmber
import com.editstudio.app.ui.theme.NavyBg
import com.editstudio.app.ui.theme.NavyBorder
import com.editstudio.app.ui.theme.NavyCard
import com.editstudio.app.ui.theme.NavyPrimary
import com.editstudio.app.ui.theme.NavyPrimaryDeep
import com.editstudio.app.ui.theme.NavySurface
import com.editstudio.app.ui.theme.NavyTextPrimary
import com.editstudio.app.ui.theme.NavyTextSecondary
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun MotionBrowser(
    favoritePresets: Set<String>,
    recentPresets: List<String>,
    storeMotionPacks: List<StoreItem>,
    onSelectPreset: (MotionPresetType) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenSpeedRamp: () -> Unit,
    onOpenPathEditor: () -> Unit,
    onOpenBeatSync: () -> Unit,
    onDownloadStorePack: (StoreItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("All") } // All, IN, OUT, LOOP, Favorites, Recent

    val anim = rememberInfiniteTransition(label = "motion_thumb")
    val progress by anim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
        label = "p"
    )

    val featured = remember {
        listOf(
            MotionPresetType.POP_IN,
            MotionPresetType.WHIP_PAN,
            MotionPresetType.ZOOM_PUNCH_BEAT,
            MotionPresetType.GLITCH_IN,
            MotionPresetType.HEART_BURST,
            MotionPresetType.ORBIT_3D
        )
    }

    val filteredPresets = remember(searchQuery, selectedTab, favoritePresets, recentPresets) {
        MotionPresetType.entries.filter { p ->
            val matchesQuery = searchQuery.isBlank() ||
                p.title.contains(searchQuery, ignoreCase = true) ||
                p.group.displayName.contains(searchQuery, ignoreCase = true)
            val matchesTab = when (selectedTab) {
                "IN" -> p.phase == MotionPhase.IN
                "OUT" -> p.phase == MotionPhase.OUT
                "LOOP" -> p.phase == MotionPhase.LOOP
                "Favorites" -> favoritePresets.contains(p.id)
                "Recent" -> recentPresets.contains(p.id)
                else -> true
            }
            matchesQuery && matchesTab
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavySurface)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose, modifier = Modifier.testTag("motion_browser_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Peeche Jayein", tint = NavyTextPrimary)
            }
            Text(
                text = "Motion Browser",
                color = NavyTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Motion preset khojein (Pop, Slide, Whip Pan...)", color = NavyTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavyAccentTeal) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyAccentTeal,
                unfocusedBorderColor = NavyBorder,
                focusedContainerColor = NavySurface,
                unfocusedContainerColor = NavySurface
            )
        )

        // Tabs: All, In, Out, Loop, Favorites, Recent
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "All" to "Sabhi",
                "IN" to "In (Intro)",
                "OUT" to "Out (Outro)",
                "LOOP" to "Loop",
                "Favorites" to "★ Favorites",
                "Recent" to "Recently Used"
            ).forEach { (key, label) ->
                FilterChip(
                    selected = selectedTab == key,
                    onClick = { selectedTab = key },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimaryDeep,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Featured Row
            if (searchQuery.isBlank() && selectedTab == "All") {
                item(span = { GridItemSpan(2) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Featured Motion",
                            color = NavyAccentTeal,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(featured, key = { it.id }) { preset ->
                                MotionPresetCard(
                                    preset = preset,
                                    progress = progress,
                                    isFavorite = favoritePresets.contains(preset.id),
                                    isLarge = true,
                                    onClick = { onSelectPreset(preset) },
                                    onToggleFavorite = { onToggleFavorite(preset.id) },
                                    modifier = Modifier.width(156.dp)
                                )
                            }
                        }
                        Text(
                            text = "Sabhi Motion Presets",
                            color = NavyTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            // Preset Cards
            items(filteredPresets, key = { it.id }) { preset ->
                MotionPresetCard(
                    preset = preset,
                    progress = progress,
                    isFavorite = favoritePresets.contains(preset.id),
                    isLarge = false,
                    onClick = { onSelectPreset(preset) },
                    onToggleFavorite = { onToggleFavorite(preset.id) }
                )
            }

            // Pro Motion Tools & Store Packs at Bottom
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Advanced Motion Tools",
                        color = NavyPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MotionToolTile(Icons.Default.Speed, "Speed Ramp", NavyPrimary, onOpenSpeedRamp, Modifier.weight(1f))
                        MotionToolTile(Icons.Default.Route, "Motion Path", NavyAccentTeal, onOpenPathEditor, Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MotionToolTile(Icons.Default.GraphicEq, "Beat Sync", NavyAmber, onOpenBeatSync, Modifier.weight(1f))
                        MotionToolTile(Icons.Default.CameraAlt, "3D Orbit", NavyPrimary, { onSelectPreset(MotionPresetType.ORBIT_3D) }, Modifier.weight(1f))
                    }

                    if (storeMotionPacks.isNotEmpty()) {
                        Text(
                            text = "Online Store Motion Packs",
                            color = NavyAmber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        storeMotionPacks.forEach { pack ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NavyCard)
                                    .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                                    .clickable { onDownloadStorePack(pack) }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = NavyAccentTeal)
                                    Column {
                                        Text(pack.name, color = NavyTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(pack.description, color = NavyTextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Text(if (pack.isDownloaded) "Installed" else "Get", color = NavyPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MotionPresetCard(
    preset: MotionPresetType,
    progress: Float,
    isFavorite: Boolean,
    isLarge: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tx = when (preset) {
        MotionPresetType.SLIDE_LEFT, MotionPresetType.WHIP_PAN -> (1f - progress) * 26f
        MotionPresetType.SLIDE_RIGHT -> -(1f - progress) * 26f
        else -> 0f
    }
    val ty = when (preset) {
        MotionPresetType.SLIDE_UP, MotionPresetType.BOUNCE_IN -> (1f - progress) * 18f
        MotionPresetType.FLOAT_LOOP -> sin(progress * 2f * PI).toFloat() * 7f
        else -> 0f
    }
    val sc = when (preset) {
        MotionPresetType.ZOOM_IN, MotionPresetType.POP_IN, MotionPresetType.HEART_BURST -> 0.6f + 0.4f * progress
        MotionPresetType.PULSE_LOOP, MotionPresetType.ZOOM_PUNCH_BEAT -> 1f + sin(progress * 2f * PI).toFloat() * 0.16f
        else -> 1f
    }
    val rot = when (preset) {
        MotionPresetType.SPIN_IN -> progress * 360f
        MotionPresetType.SWING_LOOP -> sin(progress * 2f * PI).toFloat() * 14f
        else -> 0f
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(8.dp)
            .testTag("motion_card_${preset.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isLarge) 84.dp else 66.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NavySurface),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer {
                        translationX = tx
                        translationY = ty
                        scaleX = sc
                        scaleY = sc
                        rotationZ = rot
                    }
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (preset.phase == MotionPhase.OUT) NavyAmber else NavyAccentTeal)
            )

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) NavyAmber else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Text(
            text = preset.title,
            color = NavyTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = "${preset.phase.label} • ${preset.group.displayName}",
            color = NavyAccentTeal,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MotionToolTile(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
        Text(label, color = NavyTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
