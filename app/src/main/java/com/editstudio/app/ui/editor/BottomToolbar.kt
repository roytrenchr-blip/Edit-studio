package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editstudio.app.ui.theme.StudioBorder
import com.editstudio.app.ui.theme.StudioCard
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioDarkBg
import com.editstudio.app.ui.theme.StudioNeonPurple
import com.editstudio.app.ui.theme.TextPrimary
import com.editstudio.app.ui.theme.TextSecondary

data class BottomBarItem(
    val sheet: ActiveSheet,
    val title: String,
    val icon: ImageVector
)

@Composable
fun BottomToolbar(
    activeSheet: ActiveSheet,
    onOpenSheet: (ActiveSheet) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomBarItem(ActiveSheet.MEDIA, "Media", Icons.Default.PermMedia),
        BottomBarItem(ActiveSheet.TEXT, "Text", Icons.Default.TextFields),
        BottomBarItem(ActiveSheet.SHAPES, "Shapes", Icons.Default.Category),
        BottomBarItem(ActiveSheet.MOTION, "Motion", Icons.Default.Animation),
        BottomBarItem(ActiveSheet.EFFECTS, "Effects", Icons.Default.AutoAwesome),
        BottomBarItem(ActiveSheet.AUDIO, "Audio", Icons.Default.MusicNote)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioDarkBg)
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (item in items) {
            val isSelected = activeSheet == item.sheet
            BigToolbarIcon(
                item = item,
                isSelected = isSelected,
                onClick = {
                    if (isSelected) onOpenSheet(ActiveSheet.NONE) else onOpenSheet(item.sheet)
                }
            )
        }
    }
}

@Composable
private fun BigToolbarIcon(
    item: BottomBarItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("toolbar_${item.title.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) StudioNeonPurple.copy(alpha = 0.25f) else StudioCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = if (isSelected) StudioCyberCyan else TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) StudioCyberCyan else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
