package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editstudio.app.domain.FrameRate
import com.editstudio.app.domain.Project
import com.editstudio.app.domain.VideoResolution
import com.editstudio.app.export.ExportConfig
import com.editstudio.app.export.ExportFormat
import com.editstudio.app.ui.components.AppPrimaryButton
import com.editstudio.app.ui.components.SliderRow
import com.editstudio.app.ui.theme.NavyPrimaryDeep
import com.editstudio.app.ui.theme.NavySurface
import com.editstudio.app.ui.theme.NavyTextPrimary
import com.editstudio.app.ui.theme.NavyTextSecondary

@Composable
fun ExportSheet(
    project: Project,
    onStartExport: (ExportConfig) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var format by remember { mutableStateOf(ExportFormat.MP4_H264) }
    var resolution by remember { mutableStateOf(project.resolution) }
    var frameRate by remember { mutableStateOf(project.frameRate) }
    var bitrateMbps by remember { mutableIntStateOf(12) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(NavySurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Video Export Karein",
                style = MaterialTheme.typography.titleMedium,
                color = NavyTextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Band Karein", tint = NavyTextPrimary)
            }
        }

        Text(text = "Format Chuno", style = MaterialTheme.typography.bodySmall, color = NavyTextSecondary, modifier = Modifier.padding(top = 8.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ExportFormat.entries) { fmt ->
                FilterChip(
                    selected = format == fmt,
                    onClick = { format = fmt },
                    label = { Text(fmt.label) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyPrimaryDeep)
                )
            }
        }

        Text(text = "Resolution (Quality)", style = MaterialTheme.typography.bodySmall, color = NavyTextSecondary, modifier = Modifier.padding(top = 10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VideoResolution.entries.forEach { res ->
                FilterChip(
                    selected = resolution == res,
                    onClick = { resolution = res },
                    label = { Text(res.label) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyPrimaryDeep)
                )
            }
        }

        Text(text = "Frame Rate (FPS)", style = MaterialTheme.typography.bodySmall, color = NavyTextSecondary, modifier = Modifier.padding(top = 10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FrameRate.entries.forEach { fps ->
                FilterChip(
                    selected = frameRate == fps,
                    onClick = { frameRate = fps },
                    label = { Text(fps.label) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyPrimaryDeep)
                )
            }
        }

        SliderRow(
            label = "Bitrate (Clarity)",
            value = bitrateMbps.toFloat(),
            onValueChange = { bitrateMbps = it.toInt() },
            valueRange = 4f..50f,
            formatPattern = "%.0f Mbps",
            modifier = Modifier.padding(top = 6.dp)
        )

        AppPrimaryButton(
            text = "Export Shuru Karein (Gallery Mein Save Hoga)",
            icon = Icons.Default.FileDownload,
            onClick = {
                onStartExport(
                    ExportConfig(
                        format = format,
                        resolution = resolution,
                        frameRate = frameRate,
                        bitrateMbps = bitrateMbps
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp)
        )
    }
}
