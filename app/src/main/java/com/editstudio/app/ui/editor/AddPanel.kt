package com.editstudio.app.ui.editor

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editstudio.app.domain.LayerType
import com.editstudio.app.domain.ShapeType
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

data class BuiltInAudioTrack(
    val id: String,
    val title: String,
    val category: String,
    val durationLabel: String,
    val freqHz: Double
)

@Composable
fun AddPanel(
    activeTab: AddTab,
    recentMediaUris: List<String>,
    storeItems: List<StoreItem>,
    onSelectTab: (AddTab) -> Unit,
    onAddShape: (ShapeType) -> Unit,
    onPickAllAlbums: () -> Unit,
    onPickAudioFile: () -> Unit,
    onAddMediaUri: (uri: String, isVideo: Boolean, name: String) -> Unit,
    onAddSpecialLayer: (LayerType, String) -> Unit,
    onAddAudioTrack: (title: String, uri: String?) -> Unit,
    onApplyTemplate: (name: String) -> Unit,
    onAddFreehandDrawing: () -> Unit,
    onAddVectorDrawing: () -> Unit,
    onAddText: () -> Unit,
    onDownloadStoreItem: (StoreItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavySurface)
    ) {
        // Top Tabs: Shape, Media, Audio, Elements, Template, Store
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyBg)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AddTab.entries.forEach { tab ->
                val selected = tab == activeTab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) NavyPrimaryDeep else NavyCard)
                        .border(1.dp, if (selected) NavyPrimary else NavyBorder, RoundedCornerShape(10.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("add_tab_${tab.name.lowercase()}")
                ) {
                    Text(
                        text = tab.label,
                        color = if (selected) Color.White else NavyTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Main Split: Left Content Area + Narrow Right Vertical Column
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Left Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                when (activeTab) {
                    AddTab.SHAPE -> ShapeGrid(onAddShape = onAddShape)
                    AddTab.MEDIA -> MediaGrid(
                        recentMediaUris = recentMediaUris,
                        onPickAllAlbums = onPickAllAlbums,
                        onAddMediaUri = onAddMediaUri,
                        onAddSpecialLayer = onAddSpecialLayer
                    )
                    AddTab.AUDIO -> AudioTabContent(
                        storeAudioItems = storeItems.filter { it.category == "Music" || it.category == "Sound Effects" },
                        onPickAudioFile = onPickAudioFile,
                        onAddAudioTrack = onAddAudioTrack
                    )
                    AddTab.ELEMENTS -> ElementsTabContent(
                        storeElements = storeItems.filter { it.category == "Stickers and Shapes" || it.category == "Overlays" },
                        onAddShape = onAddShape,
                        onAddSpecialLayer = onAddSpecialLayer
                    )
                    AddTab.TEMPLATE -> TemplateTabContent(
                        storeTemplates = storeItems.filter { it.category == "Templates" },
                        onApplyTemplate = onApplyTemplate
                    )
                    AddTab.STORE -> QuickStoreTabContent(
                        items = storeItems,
                        onDownload = onDownloadStoreItem,
                        onApplyTemplate = onApplyTemplate
                    )
                }
            }

            // Narrow Right Column: Freehand Drawing, Vector Drawing, Text, Close (X)
            Column(
                modifier = Modifier
                    .width(74.dp)
                    .fillMaxHeight()
                    .background(NavyBg)
                    .border(0.5.dp, NavyBorder)
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RightColumnButton(
                    icon = Icons.Default.Brush,
                    label = "Freehand",
                    tint = NavyAmber,
                    onClick = onAddFreehandDrawing,
                    tag = "add_freehand_btn"
                )
                RightColumnButton(
                    icon = Icons.Default.Gesture,
                    label = "Vector",
                    tint = NavyPrimary,
                    onClick = onAddVectorDrawing,
                    tag = "add_vector_btn"
                )
                RightColumnButton(
                    icon = Icons.Default.TextFields,
                    label = "Text",
                    tint = NavyAccentTeal,
                    onClick = onAddText,
                    tag = "add_text_btn"
                )
                RightColumnButton(
                    icon = Icons.Default.Close,
                    label = "Band",
                    tint = NavyCoral,
                    onClick = onClose,
                    tag = "add_close_btn"
                )
            }
        }
    }
}

@Composable
private fun RightColumnButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 2.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
        Text(
            text = label,
            color = NavyTextPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AudioTabContent(
    storeAudioItems: List<StoreItem>,
    onPickAudioFile: () -> Unit,
    onAddAudioTrack: (String, String?) -> Unit
) {
    val scope = rememberCoroutineScope()
    var playingId by remember { mutableStateOf<String?>(null) }
    var previewJob by remember { mutableStateOf<Job?>(null) }

    val builtInTracks = remember {
        listOf(
            BuiltInAudioTrack("bgm_lofi", "Chill Lo-Fi Beat", "Music", "0:15", 220.0),
            BuiltInAudioTrack("bgm_trap", "Reels Bass Drop", "Music", "0:12", 140.0),
            BuiltInAudioTrack("sfx_whoosh", "Cinematic Whoosh SFX", "SFX", "0:02", 440.0),
            BuiltInAudioTrack("sfx_pop", "Bubble Pop SFX", "SFX", "0:01", 660.0),
            BuiltInAudioTrack("sfx_camera", "Shutter Click SFX", "SFX", "0:01", 880.0)
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            previewJob?.cancel()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Music & Sound Effects", color = NavyTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = onPickAudioFile,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimaryDeep),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                Text(" File Chuno", fontSize = 11.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(builtInTracks, key = { it.id }) { track ->
                val isPreviewing = playingId == track.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavyCard)
                        .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                previewJob?.cancel()
                                if (isPreviewing) {
                                    playingId = null
                                } else {
                                    playingId = track.id
                                    previewJob = scope.launch(Dispatchers.IO) {
                                        playSynthTone(track.freqHz)
                                        playingId = null
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isPreviewing) NavyAccentTeal else NavySurface)
                        ) {
                            Icon(
                                imageVector = if (isPreviewing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Sunne ke liye Play",
                                tint = if (isPreviewing) Color.Black else NavyPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(track.title, color = NavyTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${track.category} • ${track.durationLabel}", color = NavyTextSecondary, fontSize = 10.sp)
                        }
                    }
                    Button(
                        onClick = { onAddAudioTrack(track.title, "sample://${track.id}") },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimaryDeep),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(storeAudioItems, key = { it.id }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavyCard)
                        .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = NavyAccentTeal, modifier = Modifier.size(20.dp))
                        Column {
                            Text(item.name, color = NavyTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("${item.category} • Store Pack", color = NavyPrimary, fontSize = 10.sp)
                        }
                    }
                    Button(
                        onClick = { onAddAudioTrack(item.name, null) },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyAccentTeal, contentColor = Color.Black),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun playSynthTone(freqHz: Double) {
    try {
        val sampleRate = 22050
        val count = sampleRate // 1 sec preview
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val env = 1.0 - (i.toDouble() / count)
            val angle = 2.0 * PI * i * freqHz / sampleRate
            buffer[i] = (sin(angle) * env * 12000.0).toInt().toShort()
        }
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(buffer, 0, buffer.size)
        track.play()
        Thread.sleep(950L)
        track.stop()
        track.release()
    } catch (_: Exception) {
    }
}

@Composable
private fun ElementsTabContent(
    storeElements: List<StoreItem>,
    onAddShape: (ShapeType) -> Unit,
    onAddSpecialLayer: (LayerType, String) -> Unit
) {
    val builtInElements = remember {
        listOf(
            Triple("Subscribe Lower Third", ShapeType.LOWER_THIRD, LayerType.SHAPE),
            Triple("Chat Bubble", ShapeType.SPEECH_BUBBLE, LayerType.SHAPE),
            Triple("Progress Meter", ShapeType.PROGRESS_BAR, LayerType.SHAPE),
            Triple("Subscriber Counter", ShapeType.COUNTER, LayerType.SHAPE),
            Triple("Analytics Bar Chart", ShapeType.CHART_BAR, LayerType.SHAPE),
            Triple("Glow Adjustment Overlay", ShapeType.ROUNDED_SQUARE, LayerType.ADJUSTMENT)
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(builtInElements) { (title, shapeType, layerType) ->
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyCard)
                    .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                    .clickable {
                        if (layerType == LayerType.SHAPE) onAddShape(shapeType)
                        else onAddSpecialLayer(layerType, title)
                    }
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                Text(title, color = NavyTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Motion Element", color = NavyTextSecondary, fontSize = 10.sp)
            }
        }

        items(storeElements, key = { it.id }) { item ->
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyCard)
                    .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                    .clickable { onAddShape(ShapeType.STAR) }
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = NavyAmber, modifier = Modifier.size(18.dp))
                Text(item.name, color = NavyTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Store Element", color = NavyAccentTeal, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun TemplateTabContent(
    storeTemplates: List<StoreItem>,
    onApplyTemplate: (String) -> Unit
) {
    val templates = remember(storeTemplates) {
        val local = listOf("Reels Hook Intro", "Lyric Glow Beat", "Product Promo Card", "Vlog Title Opener")
        (local + storeTemplates.map { it.name }).distinct()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(templates) { tpl ->
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyCard)
                    .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                    .clickable { onApplyTemplate(tpl) }
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Style, contentDescription = null, tint = NavyAccentTeal, modifier = Modifier.size(20.dp))
                Text(tpl, color = NavyTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Tap karke apply karein", color = NavyPrimary, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun QuickStoreTabContent(
    items: List<StoreItem>,
    onDownload: (StoreItem) -> Unit,
    onApplyTemplate: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(items, key = { it.id }) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavyCard)
                    .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(item.name, color = NavyTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${item.category} • ${item.size}", color = NavyPrimary, fontSize = 10.sp)
                }
                Button(
                    onClick = {
                        if (item.category == "Templates") onApplyTemplate(item.name) else onDownload(item)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimaryDeep),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(if (item.category == "Templates") "Use" else if (item.isDownloaded) "Ready" else "Get", fontSize = 11.sp)
                }
            }
        }
    }
}
