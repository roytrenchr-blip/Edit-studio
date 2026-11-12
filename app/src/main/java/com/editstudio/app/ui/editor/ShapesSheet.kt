package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.editstudio.app.domain.ShapeContent
import com.editstudio.app.domain.ShapeType
import com.editstudio.app.ui.components.AnimatedThumbnailCard
import com.editstudio.app.ui.components.GridThumbnailItem
import com.editstudio.app.ui.theme.StudioSurface
import com.editstudio.app.ui.theme.TextPrimary

@Composable
fun ShapesSheet(
    onAddShape: (ShapeContent) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shapeTypes = listOf(
        ShapeType.RECTANGLE,
        ShapeType.CIRCLE,
        ShapeType.ARROW,
        ShapeType.SPEECH_BUBBLE,
        ShapeType.LOWER_THIRD,
        ShapeType.PROGRESS_BAR,
        ShapeType.COUNTER,
        ShapeType.CHART_BAR,
        ShapeType.CHART_LINE,
        ShapeType.CHART_PIE,
        ShapeType.STAR
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Motion Shapes & Callouts",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(shapeTypes) { type ->
                AnimatedThumbnailCard(
                    item = GridThumbnailItem(
                        id = type.name,
                        title = type.label,
                        subtitle = "One-tap shape"
                    ),
                    onClick = {
                        onAddShape(ShapeContent(shapeType = type))
                        onClose()
                    },
                    onToggleFavorite = {}
                )
            }
        }
    }
}
