package com.editstudio.app.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editstudio.app.domain.ShapeContent
import com.editstudio.app.domain.ShapeType
import com.editstudio.app.render.ShapeRenderer
import com.editstudio.app.ui.theme.NavyBorder
import com.editstudio.app.ui.theme.NavyCard
import com.editstudio.app.ui.theme.NavyTextSecondary

@Composable
fun ShapeGrid(
    onAddShape: (ShapeType) -> Unit,
    modifier: Modifier = Modifier
) {
    val shapes = listOf(
        ShapeType.CIRCLE,
        ShapeType.SQUARE,
        ShapeType.ROUNDED_SQUARE,
        ShapeType.PLUS,
        ShapeType.RING,
        ShapeType.TRIANGLE,
        ShapeType.PIE,
        ShapeType.HEXAGON,
        ShapeType.STAR,
        ShapeType.ARROW,
        ShapeType.LINE,
        ShapeType.HEART,
        ShapeType.POLYGON,
        ShapeType.SPEECH_BUBBLE,
        ShapeType.LOWER_THIRD,
        ShapeType.PROGRESS_BAR,
        ShapeType.COUNTER,
        ShapeType.CHART_BAR
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 72.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(shapes, key = { it.name }) { shapeType ->
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyCard)
                    .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                    .clickable { onAddShape(shapeType) }
                    .padding(horizontal = 6.dp, vertical = 10.dp)
                    .testTag("shape_item_${shapeType.name.lowercase()}"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvas = drawContext.canvas.nativeCanvas
                        canvas.save()
                        canvas.translate(size.width / 2f, size.height / 2f)
                        ShapeRenderer.drawShapeOnCanvas(
                            canvas = canvas,
                            content = ShapeContent(
                                shapeType = shapeType,
                                fillColor = 0xFF38BDF8,
                                strokeColor = 0xFF14B8A6,
                                strokeWidth = 2f,
                                cornerRadius = 8f
                            ),
                            width = size.width * 0.86f,
                            height = size.height * 0.86f,
                            alpha = 255
                        )
                        canvas.restore()
                    }
                }
                Text(
                    text = shapeType.label,
                    color = NavyTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
