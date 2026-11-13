package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.editstudio.app.ui.theme.StudioBorder
import com.editstudio.app.ui.theme.StudioCard
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioElectricCoral
import com.editstudio.app.ui.theme.StudioNeonPurple
import com.editstudio.app.ui.theme.TextPrimary

@Composable
fun FloatingLayerBar(
    onEdit: () -> Unit,
    onMotion: () -> Unit,
    onEffects: () -> Unit,
    onSplit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(StudioCard.copy(alpha = 0.95f))
            .border(1.dp, StudioBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FloatingBarIcon(Icons.Default.Edit, "Edit Karein", "Edit properties", onEdit, StudioCyberCyan)
        FloatingBarIcon(Icons.Default.Animation, "Motion Preset", "Motion animations", onMotion, StudioNeonPurple)
        FloatingBarIcon(Icons.Default.AutoAwesome, "Visual Effects", "GPU shader effects", onEffects, StudioNeonPurple)
        FloatingBarIcon(Icons.Default.CallSplit, "Split", "Split layer at playhead", onSplit, TextPrimary)
        FloatingBarIcon(Icons.Default.ContentCopy, "Duplicate", "Duplicate layer", onDuplicate, TextPrimary)
        FloatingBarIcon(Icons.Default.Delete, "Delete", "Delete layer", onDelete, StudioElectricCoral)
        FloatingBarIcon(Icons.Default.MoreHoriz, "More Options", "Keyframes & speed ramp", onMore, TextPrimary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatingBarIcon(
    icon: ImageVector,
    title: String,
    tooltip: String,
    onClick: () -> Unit,
    tint: Color
) {
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tooltip)
            }
        },
        state = tooltipState
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
