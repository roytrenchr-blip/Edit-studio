package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editstudio.app.domain.Project
import com.editstudio.app.ui.theme.NavyAccentTeal
import com.editstudio.app.ui.theme.NavyBorder
import com.editstudio.app.ui.theme.NavyCard
import com.editstudio.app.ui.theme.NavyPrimary
import com.editstudio.app.ui.theme.NavyPrimaryDeep
import com.editstudio.app.ui.theme.NavySurface
import com.editstudio.app.ui.theme.NavyTextPrimary
import com.editstudio.app.ui.theme.NavyTextSecondary

@Composable
fun TopBar(
    project: Project,
    isSimpleMode: Boolean,
    onNavigateBack: () -> Unit,
    onRenameClick: () -> Unit,
    onToggleMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NavySurface)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("top_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Peeche Jayein",
                    tint = NavyTextPrimary
                )
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onRenameClick() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("project_name_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = NavyTextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Naam Badlein",
                        tint = NavyTextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = "${project.aspectRatio.widthRatio}:${project.aspectRatio.heightRatio} • ${project.frameRate.fps}fps",
                    style = MaterialTheme.typography.labelSmall,
                    color = NavyPrimary
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Simple / Pro Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavyCard)
                    .border(
                        1.dp,
                        if (isSimpleMode) NavyAccentTeal.copy(alpha = 0.6f) else NavyPrimary,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onToggleMode() }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
                    .testTag("mode_switch_pill")
            ) {
                Text(
                    text = if (isSimpleMode) "Simple" else "Pro",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSimpleMode) NavyAccentTeal else NavyPrimary
                )
            }

            // Settings Gear
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("top_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = NavyTextPrimary,
                    modifier = Modifier.size(21.dp)
                )
            }

            // Export Button
            Button(
                onClick = onOpenExport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyPrimaryDeep,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("top_export_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Export",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
