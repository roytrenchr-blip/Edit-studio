package com.editstudio.app.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editstudio.app.domain.EffectCategory
import com.editstudio.app.domain.EffectType
import com.editstudio.app.store.StoreItem
import com.editstudio.app.ui.theme.NavyAccentTeal
import com.editstudio.app.ui.theme.NavyAmber
import com.editstudio.app.ui.theme.NavyBg
import com.editstudio.app.ui.theme.NavyBorder
import com.editstudio.app.ui.theme.NavyCard
import com.editstudio.app.ui.theme.NavyCoral
import com.editstudio.app.ui.theme.NavyPrimary
import com.editstudio.app.ui.theme.NavyPrimaryDeep
import com.editstudio.app.ui.theme.NavySurface
import com.editstudio.app.ui.theme.NavyTextPrimary
import com.editstudio.app.ui.theme.NavyTextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EffectBrowser(
    favoriteEffects: Set<String>,
    recentEffects: List<String>,
    storeEffectPacks: List<StoreItem>,
    onSelectEffect: (EffectType) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDownloadStorePack: (StoreItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // All, Favorites, Recent, or category displayName
    var selectedCategory by remember { mutableStateOf<EffectCategory?>(null) }

    val anim = rememberInfiniteTransition(label = "effect_thumb")
    val animPhase by anim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    val categories = remember {
        listOf(
            EffectCategory.COLOR,
            EffectCategory.DRAWING_EDGE,
            EffectCategory.BLUR,
            EffectCategory.DISTORTION,
            EffectCategory.PROCEDURAL,
            EffectCategory.THREE_D,
            EffectCategory.MOVE_TRANSFORM,
            EffectCategory.REPEAT,
            EffectCategory.GLITCH,
            EffectCategory.PARTICLES,
            EffectCategory.KEYING,
            EffectCategory.STYLIZE
        )
    }

    val featured = remember { EffectType.entries.filter { it.isFeatured } }
    val filteredEffects = remember(searchQuery, selectedFilter, selectedCategory, favoriteEffects, recentEffects) {
        EffectType.entries.filter { eff ->
            val matchesQuery = searchQuery.isBlank() ||
                eff.displayName.contains(searchQuery, ignoreCase = true) ||
                eff.category.displayName.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "Favorites" -> favoriteEffects.contains(eff.id)
                "Recent" -> recentEffects.contains(eff.id)
                else -> true
            }
            val matchesCat = selectedCategory == null || eff.category.displayName == selectedCategory?.displayName
            matchesQuery && matchesFilter && matchesCat
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBg)
    ) {
        // Header + Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavySurface)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose, modifier = Modifier.testTag("effect_browser_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Peeche Jayein", tint = NavyTextPrimary)
            }
            Text(
                text = "Effect Browser",
                color = NavyTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (selectedCategory != null) {
                FilterChip(
                    selected = true,
                    onClick = { selectedCategory = null },
                    label = { Text("${selectedCategory?.displayName} ×", fontSize = 11.sp) }
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Effect khojein (Blur, Glow, Glitch...)", color = NavyTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("effect_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = NavyBorder,
                focusedContainerColor = NavySurface,
                unfocusedContainerColor = NavySurface
            )
        )

        // Quick Filters: All, Favorites, Recently Used
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All" to "Sabhi Effects", "Favorites" to "★ Favorites", "Recent" to "Recently Used").forEach { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key && selectedCategory == null,
                    onClick = {
                        selectedFilter = key
                        selectedCategory = null
                    },
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
            // Featured Horizontal Row
            if (searchQuery.isBlank() && selectedFilter == "All" && selectedCategory == null) {
                item(span = { GridItemSpan(2) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Featured Effects",
                            color = NavyPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(featured, key = { it.id }) { eff ->
                                EffectThumbnailCard(
                                    effect = eff,
                                    animPhase = animPhase,
                                    isFavorite = favoriteEffects.contains(eff.id),
                                    isLarge = true,
                                    onClick = { onSelectEffect(eff) },
                                    onToggleFavorite = { onToggleFavorite(eff.id) },
                                    modifier = Modifier.width(156.dp)
                                )
                            }
                        }
                        Text(
                            text = "Sabhi Effects",
                            color = NavyTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            // Grid of Effect Thumbnails
            items(filteredEffects, key = { it.id }) { eff ->
                EffectThumbnailCard(
                    effect = eff,
                    animPhase = animPhase,
                    isFavorite = favoriteEffects.contains(eff.id),
                    isLarge = false,
                    onClick = { onSelectEffect(eff) },
                    onToggleFavorite = { onToggleFavorite(eff.id) }
                )
            }

            // 12 Category Tiles at Bottom
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Categories (Chuno)",
                        color = NavyAccentTeal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    categories.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { cat ->
                                val isCatSel = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isCatSel) NavyPrimaryDeep else NavyCard)
                                        .border(1.dp, if (isCatSel) NavyPrimary else NavyBorder, RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedCategory = if (isCatSel) null else cat
                                            selectedFilter = "All"
                                        }
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = cat.displayName,
                                        color = NavyTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Store Packs Section
                    if (storeEffectPacks.isNotEmpty()) {
                        Text(
                            text = "Online Store Effect Packs",
                            color = NavyAmber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        storeEffectPacks.forEach { pack ->
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
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = NavyPrimary)
                                    Column {
                                        Text(pack.name, color = NavyTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(pack.description, color = NavyTextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Text(if (pack.isDownloaded) "Installed" else "Get", color = NavyAccentTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EffectThumbnailCard(
    effect: EffectType,
    animPhase: Float,
    isFavorite: Boolean,
    isLarge: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(8.dp)
            .testTag("effect_card_${effect.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isLarge) 88.dp else 70.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E3A8A).copy(alpha = 0.7f))
                    )
                )
        ) {
            // Live Animated Visual Preview per Effect Category
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val pulse = 0.65f + 0.35f * sin(animPhase * 2f * PI).toFloat()

                when (effect.category) {
                    EffectCategory.GLITCH -> {
                        val shift = sin(animPhase * 8f * PI).toFloat() * 10f
                        drawCircle(NavyCoral.copy(alpha = 0.7f), radius = 16f, center = Offset(cx - shift, cy))
                        drawCircle(NavyPrimary.copy(alpha = 0.7f), radius = 16f, center = Offset(cx + shift, cy))
                    }
                    EffectCategory.BLUR -> {
                        drawCircle(NavyPrimary.copy(alpha = 0.25f), radius = 26f * pulse, center = Offset(cx, cy))
                        drawCircle(NavyPrimary.copy(alpha = 0.45f), radius = 18f * pulse, center = Offset(cx, cy))
                        drawCircle(Color.White.copy(alpha = 0.75f), radius = 10f, center = Offset(cx, cy))
                    }
                    EffectCategory.DISTORTION, EffectCategory.REPEAT -> {
                        for (i in 1..3) {
                            drawCircle(
                                color = NavyAccentTeal.copy(alpha = 0.8f - i * 0.2f),
                                radius = (i * 10f * pulse).coerceAtLeast(4f),
                                center = Offset(cx, cy),
                                style = Stroke(width = 2.5f)
                            )
                        }
                    }
                    EffectCategory.PARTICLES -> {
                        for (i in 0..7) {
                            val a = animPhase * 2f * PI + i * (PI / 4.0)
                            val r = 10f + 18f * ((animPhase + i * 0.12f) % 1f)
                            drawCircle(
                                color = NavyAmber,
                                radius = 3.5f,
                                center = Offset(cx + (cos(a) * r).toFloat(), cy + (sin(a) * r).toFloat())
                            )
                        }
                    }
                    else -> {
                        drawCircle(
                            color = NavyPrimary.copy(alpha = 0.35f),
                            radius = 24f * pulse,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = NavyAccentTeal,
                            radius = 12f,
                            center = Offset(cx, cy)
                        )
                    }
                }
            }

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
            text = effect.displayName,
            color = NavyTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = effect.category.displayName,
            color = NavyPrimary,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
