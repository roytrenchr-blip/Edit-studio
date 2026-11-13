package com.editstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editstudio.app.ui.theme.NavyAccentTeal
import com.editstudio.app.ui.theme.NavyAmber
import com.editstudio.app.ui.theme.NavyBorder
import com.editstudio.app.ui.theme.NavyCard
import com.editstudio.app.ui.theme.NavyPrimary
import com.editstudio.app.ui.theme.NavySurface
import com.editstudio.app.ui.theme.NavyTextPrimary
import com.editstudio.app.ui.theme.NavyTextSecondary
import java.util.Locale

@Composable
fun SliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    formatPattern: String = "%.2f",
    unit: String = "",
    showKeyframeButton: Boolean = false,
    hasKeyframeAtPlayhead: Boolean = false,
    onToggleKeyframe: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val formattedValue = if (formatPattern.contains("%.0f%%")) {
        "${(value * 100f).toInt()}%"
    } else {
        String.format(Locale.US, formatPattern, value) + unit
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = NavyTextPrimary,
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formattedValue,
                    style = MaterialTheme.typography.labelMedium,
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (showKeyframeButton && onToggleKeyframe != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (hasKeyframeAtPlayhead) NavyAmber.copy(alpha = 0.22f) else NavyCard)
                            .border(
                                1.dp,
                                if (hasKeyframeAtPlayhead) NavyAmber else NavyBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleKeyframe()
                            }
                            .testTag("kf_${label.lowercase().replace(" ", "_")}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .rotate(45f)
                                .background(
                                    color = if (hasKeyframeAtPlayhead) NavyAmber else Color.Transparent,
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (hasKeyframeAtPlayhead) NavyAmber else NavyTextSecondary,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = NavyPrimary,
                activeTrackColor = NavyPrimary,
                inactiveTrackColor = NavyBorder
            )
        )
    }
}

@Composable
fun ColorPickerRow(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    label: String = "Rang Chuno",
    modifier: Modifier = Modifier
) {
    val palette = listOf(
        0xFFFFFFFFL,
        0xFF0B1120L,
        0xFF38BDF8L,
        0xFF2563EBL,
        0xFF14B8A6L,
        0xFF10B981L,
        0xFFF59E0BL,
        0xFFFB7185L,
        0xFFEF4444L,
        0xFFE879F9L,
        0xFF94A3B8L,
        0x00000000L
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = NavyTextSecondary,
            fontWeight = FontWeight.Medium
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            palette.forEach { hex ->
                val isSelected = selectedColor == hex
                val isTransparent = hex == 0x00000000L
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isTransparent) NavySurface else Color(hex))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) NavyPrimary else NavyBorder,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected Color",
                            tint = if (hex == 0xFFFFFFFFL || hex == 0xFFF59E0BL) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    } else if (isTransparent) {
                        Text("×", color = NavyTextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
