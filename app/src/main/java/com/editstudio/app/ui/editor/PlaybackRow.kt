package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.editstudio.app.ui.theme.NavyAccentTeal
import com.editstudio.app.ui.theme.NavyAmber
import com.editstudio.app.ui.theme.NavyBorder
import com.editstudio.app.ui.theme.NavyCard
import com.editstudio.app.ui.theme.NavyPrimary
import com.editstudio.app.ui.theme.NavyPrimaryDeep
import com.editstudio.app.ui.theme.NavySurface
import com.editstudio.app.ui.theme.NavyTextPrimary
import com.editstudio.app.ui.theme.NavyTextSecondary

@Composable
fun PlaybackRow(
    isPlaying: Boolean,
    isLooping: Boolean,
    isFullscreen: Boolean,
    hasBookmarkNearPlayhead: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    hasSelectedLayer: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onPrevKeyframe: () -> Unit,
    onTogglePlay: () -> Unit,
    onNextKeyframe: () -> Unit,
    onCopyLayer: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleLoop: () -> Unit,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NavySurface)
            .border(width = 0.5.dp, color = NavyBorder)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactRowIcon(
            icon = Icons.AutoMirrored.Filled.Undo,
            label = "Undo (Wapas)",
            enabled = canUndo,
            onClick = onUndo,
            tag = "playback_undo"
        )
        CompactRowIcon(
            icon = Icons.AutoMirrored.Filled.Redo,
            label = "Redo (Aage)",
            enabled = canRedo,
            onClick = onRedo,
            tag = "playback_redo"
        )
        CompactRowIcon(
            icon = Icons.Default.SkipPrevious,
            label = "Pichla Keyframe",
            onClick = onPrevKeyframe,
            tag = "playback_prev_kf"
        )

        // Big Play/Pause Center Button
        IconButton(
            onClick = onTogglePlay,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(NavyPrimaryDeep)
                .testTag("playback_play_pause"),
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause Karein" else "Play Karein",
                modifier = Modifier.size(26.dp)
            )
        }

        CompactRowIcon(
            icon = Icons.Default.SkipNext,
            label = "Agla Keyframe",
            onClick = onNextKeyframe,
            tag = "playback_next_kf"
        )
        CompactRowIcon(
            icon = Icons.Default.ContentCopy,
            label = "Layer Copy Karein",
            enabled = hasSelectedLayer,
            onClick = onCopyLayer,
            tag = "playback_copy_layer"
        )
        CompactRowIcon(
            icon = if (hasBookmarkNearPlayhead) Icons.Default.BookmarkAdded else Icons.Default.BookmarkAdd,
            label = "Bookmark / Marker",
            tint = if (hasBookmarkNearPlayhead) NavyAmber else NavyTextPrimary,
            onClick = onToggleBookmark,
            tag = "playback_bookmark"
        )
        CompactRowIcon(
            icon = Icons.Default.Repeat,
            label = "Loop Play",
            tint = if (isLooping) NavyAccentTeal else NavyTextSecondary,
            onClick = onToggleLoop,
            tag = "playback_loop"
        )
        CompactRowIcon(
            icon = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
            label = if (isFullscreen) "Chhota Preview" else "Fullscreen Preview",
            tint = if (isFullscreen) NavyPrimary else NavyTextPrimary,
            onClick = onToggleFullscreen,
            tag = "playback_fullscreen"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactRowIcon(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tag: String,
    enabled: Boolean = true,
    tint: Color = NavyTextPrimary
) {
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(label) } },
        state = tooltipState
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .testTag(tag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) tint else NavyTextSecondary.copy(alpha = 0.35f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
