package com.editstudio.app.render

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.editstudio.app.domain.DrawingContent
import com.editstudio.app.domain.ShapeContent
import com.editstudio.app.domain.ShapeType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object ShapeRenderer {

    fun buildShapePath(type: ShapeType, width: Float, height: Float, content: ShapeContent): Path {
        val hw = width / 2f
        val hh = height / 2f
        val r = min(hw, hh)
        val path = Path()

        when (type) {
            ShapeType.CIRCLE -> path.addCircle(0f, 0f, r, Path.Direction.CW)
            ShapeType.SQUARE -> {
                val side = min(hw, hh)
                path.addRect(-side, -side, side, side, Path.Direction.CW)
            }
            ShapeType.ROUNDED_SQUARE -> {
                val side = min(hw, hh)
                val cr = content.cornerRadius.coerceIn(0f, side)
                path.addRoundRect(RectF(-side, -side, side, side), cr, cr, Path.Direction.CW)
            }
            ShapeType.RECTANGLE -> {
                val cr = content.cornerRadius.coerceIn(0f, min(hw, hh))
                path.addRoundRect(RectF(-hw, -hh, hw, hh), cr, cr, Path.Direction.CW)
            }
            ShapeType.PLUS -> {
                val arm = r * 0.32f
                path.moveTo(-arm, -r)
                path.lineTo(arm, -r)
                path.lineTo(arm, -arm)
                path.lineTo(r, -arm)
                path.lineTo(r, arm)
                path.lineTo(arm, arm)
                path.lineTo(arm, r)
                path.lineTo(-arm, r)
                path.lineTo(-arm, arm)
                path.lineTo(-r, arm)
                path.lineTo(-r, -arm)
                path.lineTo(-arm, -arm)
                path.close()
            }
            ShapeType.RING -> {
                path.fillType = Path.FillType.EVEN_ODD
                path.addCircle(0f, 0f, r, Path.Direction.CW)
                path.addCircle(0f, 0f, r * 0.56f, Path.Direction.CCW)
            }
            ShapeType.TRIANGLE -> {
                path.moveTo(0f, -r)
                path.lineTo(r * 0.92f, r * 0.8f)
                path.lineTo(-r * 0.92f, r * 0.8f)
                path.close()
            }
            ShapeType.PIE -> {
                path.moveTo(0f, 0f)
                path.arcTo(RectF(-r, -r, r, r), -90f, 270f, false)
                path.close()
            }
            ShapeType.HEXAGON -> addRegularPolygon(path, 6, r)
            ShapeType.POLYGON -> addRegularPolygon(path, content.sides.coerceIn(5, 12), r)
            ShapeType.STAR -> {
                val pts = 5
                val inner = r * 0.44f
                for (i in 0 until pts * 2) {
                    val rad = if (i % 2 == 0) r else inner
                    val angle = -PI / 2.0 + i * (PI / pts)
                    val px = (cos(angle) * rad).toFloat()
                    val py = (sin(angle) * rad).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
            }
            ShapeType.ARROW -> {
                val bodyH = r * 0.32f
                path.moveTo(-r, -bodyH)
                path.lineTo(r * 0.15f, -bodyH)
                path.lineTo(r * 0.15f, -r * 0.78f)
                path.lineTo(r, 0f)
                path.lineTo(r * 0.15f, r * 0.78f)
                path.lineTo(r * 0.15f, bodyH)
                path.lineTo(-r, bodyH)
                path.close()
            }
            ShapeType.LINE -> {
                path.addRoundRect(RectF(-hw, -r * 0.1f, hw, r * 0.1f), r * 0.1f, r * 0.1f, Path.Direction.CW)
            }
            ShapeType.HEART -> {
                path.moveTo(0f, r * 0.85f)
                path.cubicTo(-r * 1.2f, r * 0.15f, -r * 0.95f, -r * 0.95f, 0f, -r * 0.35f)
                path.cubicTo(r * 0.95f, -r * 0.95f, r * 1.2f, r * 0.15f, 0f, r * 0.85f)
                path.close()
            }
            ShapeType.SPEECH_BUBBLE -> {
                val rect = RectF(-hw, -hh, hw, hh * 0.55f)
                path.addRoundRect(rect, 22f, 22f, Path.Direction.CW)
                path.moveTo(-hw * 0.35f, hh * 0.55f)
                path.lineTo(-hw * 0.55f, hh * 0.95f)
                path.lineTo(-hw * 0.05f, hh * 0.55f)
                path.close()
            }
            ShapeType.LOWER_THIRD -> {
                path.addRoundRect(RectF(-hw, -hh * 0.45f, hw, hh * 0.45f), 14f, 14f, Path.Direction.CW)
            }
            ShapeType.PROGRESS_BAR -> {
                path.addRoundRect(RectF(-hw, -hh * 0.25f, hw, hh * 0.25f), 16f, 16f, Path.Direction.CW)
            }
            ShapeType.COUNTER, ShapeType.CHART_BAR, ShapeType.CHART_LINE, ShapeType.CHART_PIE -> {
                path.addRoundRect(RectF(-hw, -hh, hw, hh), 18f, 18f, Path.Direction.CW)
            }
        }
        return path
    }

    private fun addRegularPolygon(path: Path, sides: Int, radius: Float) {
        for (i in 0 until sides) {
            val angle = -PI / 2.0 + i * (2.0 * PI / sides)
            val px = (cos(angle) * radius).toFloat()
            val py = (sin(angle) * radius).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
    }

    fun drawShapeOnCanvas(
        canvas: Canvas,
        content: ShapeContent,
        width: Float,
        height: Float,
        alpha: Int
    ) {
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = content.fillColor.toInt()
            this.alpha = (((content.fillColor ushr 24) and 0xFFL) * alpha / 255L).toInt().coerceIn(0, 255)
            style = Paint.Style.FILL
            if (content.shadowRadius > 0f) {
                setShadowLayer(content.shadowRadius, 0f, 4f, content.shadowColor.toInt())
            }
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = content.strokeColor.toInt()
            this.alpha = alpha
            style = Paint.Style.STROKE
            strokeWidth = content.strokeWidth
        }

        if (content.shapeType == ShapeType.CHART_BAR) {
            val bars = content.chartData.ifEmpty { listOf(40f, 70f, 55f, 90f) }
            val barW = width / (bars.size * 1.5f)
            val maxV = (bars.maxOrNull() ?: 100f).coerceAtLeast(1f)
            bars.forEachIndexed { idx, v ->
                val bh = (v / maxV) * height * 0.8f
                val bx = -width / 2f + idx * (barW * 1.45f) + barW * 0.3f
                canvas.drawRoundRect(bx, height / 2f - bh, bx + barW, height / 2f, 8f, 8f, fillPaint)
            }
            return
        }

        if (content.shapeType == ShapeType.COUNTER) {
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = content.fillColor.toInt()
                this.alpha = alpha
                textSize = min(width, height) * 0.42f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            val label = "${content.counterPrefix}${content.counterValue}${content.counterSuffix}"
            canvas.drawText(label, 0f, textPaint.textSize * 0.35f, textPaint)
            return
        }

        val path = buildShapePath(content.shapeType, width, height, content)
        canvas.drawPath(path, fillPaint)
        if (content.strokeWidth > 0.5f) {
            canvas.drawPath(path, strokePaint)
        }
    }

    fun drawDrawingOnCanvas(
        canvas: Canvas,
        drawing: DrawingContent,
        alpha: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        for (stroke in drawing.strokes) {
            if (stroke.points.size < 2) continue
            paint.color = stroke.color.toInt()
            paint.alpha = alpha
            paint.strokeWidth = stroke.strokeWidth
            val p = Path()
            p.moveTo(stroke.points[0].x, stroke.points[0].y)
            if (stroke.isVectorMode && stroke.points.size >= 3) {
                for (i in 1 until stroke.points.size - 1) {
                    val cur = stroke.points[i]
                    val next = stroke.points[i + 1]
                    val midX = (cur.x + next.x) / 2f
                    val midY = (cur.y + next.y) / 2f
                    p.quadTo(cur.x, cur.y, midX, midY)
                }
                val last = stroke.points.last()
                p.lineTo(last.x, last.y)
            } else {
                for (i in 1 until stroke.points.size) {
                    p.lineTo(stroke.points[i].x, stroke.points[i].y)
                }
            }
            canvas.drawPath(p, paint)
        }
    }
}
