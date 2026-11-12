package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
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
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.StaggerConfig
import com.editstudio.app.domain.StaggerMode
import com.editstudio.app.domain.TextContent
import com.editstudio.app.ui.components.AppPrimaryButton
import com.editstudio.app.ui.components.ColorPickerRow
import com.editstudio.app.ui.components.SliderRow
import com.editstudio.app.ui.theme.StudioBorder
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioNeonPurple
import com.editstudio.app.ui.theme.StudioSurface
import com.editstudio.app.ui.theme.TextPrimary
import com.editstudio.app.ui.theme.TextSecondary

@Composable
fun TextSheet(
    layer: Layer?,
    onAddText: (String) -> Unit,
    onUpdateLayer: (Layer) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf(layer?.textContent?.text ?: "Naya Text Likhein") }
    val content = layer?.textContent ?: TextContent()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (layer != null) "Text Edit Karein" else "Naya Text Add Karein",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
        }

        // Text Input
        OutlinedTextField(
            value = textInput,
            onValueChange = {
                textInput = it
                if (layer != null) {
                    onUpdateLayer(layer.copy(textContent = content.copy(text = it)))
                }
            },
            label = { Text("Text (Hindi / English / Any)", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StudioCyberCyan,
                unfocusedBorderColor = StudioBorder
            )
        )

        if (layer == null) {
            AppPrimaryButton(
                text = "Canvas Par Add Karein",
                onClick = { onAddText(textInput) },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
            return
        }

        // Font Size & Outline
        SliderRow(
            label = "Font Size (Akaar)",
            value = content.fontSizeSp,
            onValueChange = { onUpdateLayer(layer.copy(textContent = content.copy(fontSizeSp = it))) },
            valueRange = 14f..80f,
            formatPattern = "%.0f sp"
        )

        SliderRow(
            label = "Outline Width (Border)",
            value = content.outlineWidth,
            onValueChange = { onUpdateLayer(layer.copy(textContent = content.copy(outlineWidth = it))) },
            valueRange = 0f..10f,
            formatPattern = "%.1f px"
        )

        // Color Picker
        ColorPickerRow(
            selectedColor = content.textColor,
            onColorSelected = { onUpdateLayer(layer.copy(textContent = content.copy(textColor = it))) },
            label = "Text Color"
        )

        // Stagger Animation Toggle (per-letter / word)
        Text(
            text = "Kinetic Text Animation (Stagger)",
            style = MaterialTheme.typography.titleSmall,
            color = StudioCyberCyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = layer.stagger.isEnabled && layer.stagger.mode == StaggerMode.PER_LETTER,
                onClick = {
                    val enabled = !(layer.stagger.isEnabled && layer.stagger.mode == StaggerMode.PER_LETTER)
                    onUpdateLayer(layer.copy(stagger = StaggerConfig(isEnabled = enabled, mode = StaggerMode.PER_LETTER)))
                },
                label = { Text("Typewriter Letter") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StudioNeonPurple)
            )
            FilterChip(
                selected = layer.stagger.isEnabled && layer.stagger.mode == StaggerMode.PER_WORD,
                onClick = {
                    val enabled = !(layer.stagger.isEnabled && layer.stagger.mode == StaggerMode.PER_WORD)
                    onUpdateLayer(layer.copy(stagger = StaggerConfig(isEnabled = enabled, mode = StaggerMode.PER_WORD)))
                },
                label = { Text("Word Stagger") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StudioNeonPurple)
            )
        }

        // Karaoke Highlight Slider
        SliderRow(
            label = "Karaoke Highlight",
            value = content.karaokeProgress,
            onValueChange = { onUpdateLayer(layer.copy(textContent = content.copy(karaokeProgress = it))) },
            valueRange = 0f..1f,
            formatPattern = "%.0f%%"
        )
    }
}
