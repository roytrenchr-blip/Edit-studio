package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.editstudio.app.domain.LayerType
import com.editstudio.app.ui.theme.NavyAccentTeal
import com.editstudio.app.ui.theme.NavyBorder
import com.editstudio.app.ui.theme.NavyCard
import com.editstudio.app.ui.theme.NavyPrimary
import com.editstudio.app.ui.theme.NavyPrimaryDeep
import com.editstudio.app.ui.theme.NavyTextPrimary
import com.editstudio.app.ui.theme.NavyTextSecondary

data class SampleMediaItem(
    val id: String,
    val title: String,
    val isVideo: Boolean,
    val layerType: LayerType,
    val colors: List<Color>
)

@Composable
fun MediaGrid(
    recentMediaUris: List<String>,
    onPickAllAlbums: () -> Unit,
    onAddMediaUri: (uri: String, isVideo: Boolean, name: String) -> Unit,
    onAddSpecialLayer: (LayerType, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSampleAlbum by remember { mutableStateOf(recentMediaUris.isEmpty()) }

    val samples = remember {
        listOf(
            SampleMediaItem("sample_vid_navy", "Deep Ocean Loop", true, LayerType.VIDEO, listOf(Color(0xFF1E3A8A), Color(0xFF0EA5E9))),
            SampleMediaItem("sample_vid_sunset", "Neon Sunset Reel", true, LayerType.VIDEO, listOf(Color(0xFF7C2D12), Color(0xFFF59E0B))),
            SampleMediaItem("sample_img_teal", "Emerald Mesh BG", false, LayerType.GRADIENT, listOf(Color(0xFF064E3B), Color(0xFF14B8A6))),
            SampleMediaItem("sample_solid", "Dark Navy Solid", false, LayerType.SOLID, listOf(Color(0xFF18233C), Color(0xFF0B1120))),
            SampleMediaItem("sample_adj", "Adjustment Layer", false, LayerType.ADJUSTMENT, listOf(Color(0xFF9D174D), Color(0xFFEC4899))),
            SampleMediaItem("sample_cam", "3D Camera Rig", false, LayerType.CAMERA, listOf(Color(0xFF1E40AF), Color(0xFF38BDF8)))
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        // Album Switch Row: All albums (Photo Picker) & Sample media
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPickAllAlbums,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimaryDeep, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("media_all_albums_btn")
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" All albums", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            FilterChip(
                selected = !showSampleAlbum,
                onClick = { showSampleAlbum = false },
                label = { Text("Recent (${recentMediaUris.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyCard,
                    selectedLabelColor = NavyPrimary
                )
            )

            FilterChip(
                selected = showSampleAlbum,
                onClick = { showSampleAlbum = true },
                label = { Text("Sample media", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyCard,
                    selectedLabelColor = NavyAccentTeal
                )
            )
        }

        if (showSampleAlbum || recentMediaUris.isEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(samples, key = { it.id }) { item ->
                    Box(
                        modifier = Modifier
                            .height(78.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(item.colors))
                            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                if (item.layerType == LayerType.VIDEO || item.layerType == LayerType.IMAGE) {
                                    onAddMediaUri("sample://${item.id}", item.isVideo, item.title)
                                } else {
                                    onAddSpecialLayer(item.layerType, item.title)
                                }
                            }
                            .padding(8.dp)
                            .testTag("sample_${item.id}")
                    ) {
                        Icon(
                            imageVector = when (item.layerType) {
                                LayerType.VIDEO -> Icons.Default.Movie
                                LayerType.GRADIENT -> Icons.Default.Gradient
                                LayerType.ADJUSTMENT -> Icons.Default.Tune
                                LayerType.SOLID -> Icons.Default.Square
                                else -> Icons.Default.Collections
                            },
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp).align(Alignment.TopStart)
                        )
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(recentMediaUris) { uri ->
                    Box(
                        modifier = Modifier
                            .height(84.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavyCard)
                            .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                            .clickable { onAddMediaUri(uri, true, "Gallery Clip") }
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Recent Media",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
