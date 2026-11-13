package com.editstudio.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.editstudio.app.data.db.ProjectEntity
import com.editstudio.app.data.settings.SettingsStore
import com.editstudio.app.domain.AspectRatio
import com.editstudio.app.domain.FrameRate
import com.editstudio.app.domain.VideoResolution
import com.editstudio.app.ui.components.AppPrimaryButton
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
import com.editstudio.app.util.TimeFormat
import kotlinx.coroutines.launch

@Composable
fun CreateHubScreen(
    viewModel: HomeViewModel,
    onOpenProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("Mera Naya Video") }
    var selectedRatio by remember { mutableStateOf(AspectRatio.PORTRAIT_9_16) }
    var selectedRes by remember { mutableStateOf(VideoResolution.FHD_1080P) }
    var selectedFps by remember { mutableStateOf(FrameRate.FPS_30) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Brand Header
        Column {
            Text(
                text = "Edit Studio",
                style = MaterialTheme.typography.titleLarge,
                color = NavyTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Naya Motion Graphics ya Video Project Shuru Karein",
                style = MaterialTheme.typography.bodySmall,
                color = NavyPrimary
            )
        }

        // Hero Create Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(NavyPrimaryDeep.copy(alpha = 0.7f), NavyAccentTeal.copy(alpha = 0.35f))
                    )
                )
                .border(1.dp, NavyPrimary.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Ka Naam") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyPrimary,
                        unfocusedBorderColor = NavyBorder,
                        focusedContainerColor = NavySurface.copy(alpha = 0.75f),
                        unfocusedContainerColor = NavySurface.copy(alpha = 0.6f)
                    )
                )

                Text("Aspect Ratio Chuno", color = NavyTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AspectRatio.entries.forEach { ratio ->
                        val sel = selectedRatio == ratio
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (sel) NavyPrimaryDeep else NavySurface)
                                .border(1.dp, if (sel) NavyPrimary else NavyBorder, RoundedCornerShape(10.dp))
                                .clickable { selectedRatio = ratio }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${ratio.widthRatio}:${ratio.heightRatio}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Text("Resolution & FPS", color = NavyTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VideoResolution.entries.forEach { res ->
                        FilterChip(
                            selected = selectedRes == res,
                            onClick = { selectedRes = res },
                            label = { Text(res.label.take(5), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyPrimaryDeep)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FrameRate.entries.forEach { fps ->
                        FilterChip(
                            selected = selectedFps == fps,
                            onClick = { selectedFps = fps },
                            label = { Text("${fps.fps} fps", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyAccentTeal, selectedLabelColor = Color.Black)
                        )
                    }
                }

                AppPrimaryButton(
                    text = "Naya Project Banayein",
                    icon = Icons.Default.Add,
                    onClick = {
                        val id = viewModel.createNewProject(name, selectedRatio, selectedRes, selectedFps)
                        onOpenProject(id)
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
            }
        }

        // Quick Start Presets
        Text(
            text = "Jhatpat Shuru Karein",
            style = MaterialTheme.typography.titleSmall,
            color = NavyTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickStartCard(
                title = "9:16 Reels / Shorts",
                subtitle = "1080p • 60 fps",
                onClick = {
                    val id = viewModel.createNewProject("Reels Edit", AspectRatio.PORTRAIT_9_16, VideoResolution.FHD_1080P, FrameRate.FPS_60)
                    onOpenProject(id)
                },
                modifier = Modifier.weight(1f)
            )
            QuickStartCard(
                title = "16:9 YouTube Video",
                subtitle = "4K Ultra HD • 30 fps",
                onClick = {
                    val id = viewModel.createNewProject("YouTube Video", AspectRatio.LANDSCAPE_16_9, VideoResolution.UHD_4K, FrameRate.FPS_30)
                    onOpenProject(id)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickStartCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Default.AspectRatio, contentDescription = null, tint = NavyPrimary)
        Text(title, color = NavyTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = NavyAccentTeal, fontSize = 11.sp)
    }
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val isCreateOpen by viewModel.isCreateDialogOpen.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsDialogOpen.collectAsStateWithLifecycle()

    var projectToRename by remember { mutableStateOf<ProjectEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NavyBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateDialog() },
                containerColor = NavyPrimaryDeep,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.padding(12.dp).testTag("projects_new_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Naya Project")
                    Text("Naya Project", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavySurface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mere Projects",
                        style = MaterialTheme.typography.titleLarge,
                        color = NavyTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${projects.size} Saved Projects",
                        style = MaterialTheme.typography.bodySmall,
                        color = NavyPrimary
                    )
                }

                IconButton(onClick = { viewModel.openSettingsDialog() }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = NavyTextPrimary
                    )
                }
            }

            if (projects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(NavyPrimaryDeep.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = "Koi Project Nahi Hai",
                            style = MaterialTheme.typography.titleMedium,
                            color = NavyTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Naya project banayein ya online store se templates use karein",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NavyTextSecondary
                        )
                        AppPrimaryButton(
                            text = "Naya Project Banayein",
                            icon = Icons.Default.Add,
                            onClick = { viewModel.openCreateDialog() },
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(projects, key = { it.id }) { project ->
                        ProjectGridCard(
                            project = project,
                            onClick = { onOpenProject(project.id) },
                            onDuplicate = { viewModel.duplicateProject(project) },
                            onRename = {
                                projectToRename = project
                                renameInput = project.name
                            },
                            onDelete = { viewModel.deleteProject(project.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(project.id, project.isFavorite) }
                        )
                    }
                }
            }
        }

        if (isCreateOpen) {
            CreateProjectDialog(
                onDismiss = { viewModel.closeCreateDialog() },
                onCreate = { name, ratio, res, fps ->
                    val id = viewModel.createNewProject(name, ratio, res, fps)
                    onOpenProject(id)
                }
            )
        }

        if (isSettingsOpen) {
            SettingsDialog(
                settingsStore = viewModel.settingsStore,
                onDismiss = { viewModel.closeSettingsDialog() }
            )
        }

        if (projectToRename != null) {
            AlertDialog(
                onDismissRequest = { projectToRename = null },
                title = { Text("Project Ka Naam Badlein") },
                text = {
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        label = { Text("Project Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            projectToRename?.let { viewModel.renameProject(it.id, renameInput) }
                            projectToRename = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimaryDeep)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { projectToRename = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ProjectGridCard(
    project: ProjectEntity,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(106.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(NavyPrimaryDeep.copy(alpha = 0.65f), NavyAccentTeal.copy(alpha = 0.45f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(36.dp)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NavyBg.copy(alpha = 0.85f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = project.aspectRatio.replace("_", ":").take(4),
                        style = TextStyle(fontSize = 10.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.align(Alignment.TopEnd).size(36.dp)
                ) {
                    Icon(
                        imageVector = if (project.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (project.isFavorite) NavyAmber else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NavyTextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = NavyTextSecondary, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Rename Karein") },
                                onClick = { onRename(); menuExpanded = false },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate Karein") },
                                onClick = { onDuplicate(); menuExpanded = false },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = NavyCoral) },
                                onClick = { onDelete(); menuExpanded = false },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = NavyCoral) }
                            )
                        }
                    }
                }

                Text(
                    text = "${TimeFormat.formatShortTime(project.durationMs)} • ${project.layerCount} Layers",
                    style = MaterialTheme.typography.labelSmall,
                    color = NavyTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    settingsStore: SettingsStore,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val contentUrl by settingsStore.contentSourceUrl.collectAsStateWithLifecycle(initialValue = SettingsStore.DEFAULT_CONTENT_URL)
    val isSimple by settingsStore.isSimpleMode.collectAsStateWithLifecycle(initialValue = true)
    val wifiOnly by settingsStore.isWifiOnly.collectAsStateWithLifecycle(initialValue = false)
    val lowQuality by settingsStore.isLowQualityPreview.collectAsStateWithLifecycle(initialValue = false)
    var urlInput by remember(contentUrl) { mutableStateOf(contentUrl) }
    var savedMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            color = NavyTextPrimary,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(NavyCard)
                .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Simple Mode (Aasan Controls)", color = NavyTextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Off karne par Pro Keyframes & Curves dikhenge", color = NavyTextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isSimple,
                        onCheckedChange = { coroutineScope.launch { settingsStore.setSimpleMode(it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = NavyAccentTeal)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wi-Fi Only Downloads", color = NavyTextPrimary)
                    Switch(
                        checked = wifiOnly,
                        onCheckedChange = { coroutineScope.launch { settingsStore.setWifiOnly(it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = NavyPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Low Quality Preview (Fast Scrub)", color = NavyTextPrimary)
                    Switch(
                        checked = lowQuality,
                        onCheckedChange = { coroutineScope.launch { settingsStore.setLowQualityPreview(it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = NavyPrimary)
                    )
                }

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Content Source URL (GitHub raw)") },
                    modifier = Modifier.fillMaxWidth()
                )

                AppPrimaryButton(
                    text = "URL Save Karein",
                    onClick = {
                        coroutineScope.launch {
                            settingsStore.setContentSourceUrl(urlInput)
                            savedMsg = "Content Source URL save ho gaya!"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (savedMsg != null) {
                    Text(savedMsg!!, color = NavyAccentTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, ratio: AspectRatio, res: VideoResolution, fps: FrameRate) -> Unit
) {
    var name by remember { mutableStateOf("Mera Naya Video") }
    var selectedRatio by remember { mutableStateOf(AspectRatio.PORTRAIT_9_16) }
    var selectedRes by remember { mutableStateOf(VideoResolution.FHD_1080P) }
    var selectedFps by remember { mutableStateOf(FrameRate.FPS_30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Naya Project Banayein", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Ka Naam") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Aspect Ratio:", style = MaterialTheme.typography.bodySmall, color = NavyTextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AspectRatio.entries.forEach { ratio ->
                        FilterChip(
                            selected = selectedRatio == ratio,
                            onClick = { selectedRatio = ratio },
                            label = { Text(ratio.displayName.take(4)) }
                        )
                    }
                }

                Text("Resolution:", style = MaterialTheme.typography.bodySmall, color = NavyTextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    VideoResolution.entries.forEach { res ->
                        FilterChip(
                            selected = selectedRes == res,
                            onClick = { selectedRes = res },
                            label = { Text(res.label.take(5)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, selectedRatio, selectedRes, selectedFps) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimaryDeep)
            ) {
                Text("Start Editing")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SettingsDialog(
    settingsStore: SettingsStore,
    onReplayTour: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val contentUrl by settingsStore.contentSourceUrl.collectAsStateWithLifecycle(initialValue = SettingsStore.DEFAULT_CONTENT_URL)
    val wifiOnly by settingsStore.isWifiOnly.collectAsStateWithLifecycle(initialValue = false)
    val lowQuality by settingsStore.isLowQualityPreview.collectAsStateWithLifecycle(initialValue = false)

    var urlInput by remember(contentUrl) { mutableStateOf(contentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings & Content Source", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Content Source URL (GitHub raw)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wi-Fi Only Downloads")
                    Switch(
                        checked = wifiOnly,
                        onCheckedChange = {
                            coroutineScope.launch { settingsStore.setWifiOnly(it) }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Low Quality Preview")
                    Switch(
                        checked = lowQuality,
                        onCheckedChange = {
                            coroutineScope.launch { settingsStore.setLowQualityPreview(it) }
                        }
                    )
                }

                if (onReplayTour != null) {
                    TextButton(onClick = onReplayTour) {
                        Text("4-Step Guide Dobara Dekhein", color = NavyPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        settingsStore.setContentSourceUrl(urlInput)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimaryDeep)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Band Karein")
            }
        }
    )
}
