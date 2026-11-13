package com.editstudio.app.ui.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.editstudio.app.ui.theme.StudioBorder
import com.editstudio.app.ui.theme.StudioCard
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioDarkBg
import com.editstudio.app.ui.theme.StudioElectricCoral
import com.editstudio.app.ui.theme.StudioGoldenAmber
import com.editstudio.app.ui.theme.StudioNeonPurple
import com.editstudio.app.ui.theme.StudioSurface
import com.editstudio.app.ui.theme.TextPrimary
import com.editstudio.app.ui.theme.TextSecondary
import com.editstudio.app.util.TimeFormat
import kotlinx.coroutines.launch

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
        containerColor = StudioDarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateDialog() },
                containerColor = StudioNeonPurple,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.padding(16.dp)
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
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioSurface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Edit Studio",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Video & Motion Graphics Editor",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioCyberCyan
                    )
                }

                IconButton(onClick = { viewModel.openSettingsDialog() }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextPrimary
                    )
                }
            }

            // Projects List
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
                                .background(StudioNeonPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = StudioCyberCyan,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = "Koi Project Nahi Hai",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Naya project banayein ya online store se templates use karein",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
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

        // Create Project Modal
        if (isCreateOpen) {
            CreateProjectDialog(
                onDismiss = { viewModel.closeCreateDialog() },
                onCreate = { name, ratio, res, fps ->
                    val id = viewModel.createNewProject(name, ratio, res, fps)
                    onOpenProject(id)
                }
            )
        }

        // Settings Dialog
        if (isSettingsOpen) {
            SettingsDialog(
                settingsStore = viewModel.settingsStore,
                onDismiss = { viewModel.closeSettingsDialog() }
            )
        }

        // Rename Dialog
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
                    Button(onClick = {
                        projectToRename?.let { viewModel.renameProject(it.id, renameInput) }
                        projectToRename = null
                    }) {
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
            .background(StudioCard)
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(StudioNeonPurple.copy(alpha = 0.5f), StudioCyberCyan.copy(alpha = 0.5f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(36.dp)
                )

                // Aspect ratio badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(StudioDarkBg.copy(alpha = 0.8f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = project.aspectRatio.replace("_", ":").take(4),
                        style = TextStyle(fontSize = 10.sp, color = StudioCyberCyan, fontWeight = FontWeight.Bold)
                    )
                }

                // Favorite Star
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.align(Alignment.TopEnd).size(36.dp)
                ) {
                    Icon(
                        imageVector = if (project.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (project.isFavorite) StudioGoldenAmber else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Info
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
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
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextSecondary, modifier = Modifier.size(16.dp))
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
                                text = { Text("Delete", color = StudioElectricCoral) },
                                onClick = { onDelete(); menuExpanded = false },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StudioElectricCoral) }
                            )
                        }
                    }
                }

                Text(
                    text = "${TimeFormat.formatShortTime(project.durationMs)} • ${project.layerCount} Layers",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
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

                Text("Aspect Ratio:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AspectRatio.entries.forEach { ratio ->
                        androidx.compose.material3.FilterChip(
                            selected = selectedRatio == ratio,
                            onClick = { selectedRatio = ratio },
                            label = { Text(ratio.displayName.take(4)) }
                        )
                    }
                }

                Text("Resolution:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    VideoResolution.entries.forEach { res ->
                        androidx.compose.material3.FilterChip(
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
                colors = ButtonDefaults.buttonColors(containerColor = StudioNeonPurple)
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
                    label = { Text("Content Source URL (GitHub raw manifest)") },
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
                    Text("Low Quality Preview (Fast scrub)")
                    Switch(
                        checked = lowQuality,
                        onCheckedChange = {
                            coroutineScope.launch { settingsStore.setLowQualityPreview(it) }
                        }
                    )
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
                colors = ButtonDefaults.buttonColors(containerColor = StudioNeonPurple)
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
