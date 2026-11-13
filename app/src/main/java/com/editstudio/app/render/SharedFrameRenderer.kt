package com.editstudio.app.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import com.editstudio.app.domain.AnimProperty
import com.editstudio.app.domain.BlendMode
import com.editstudio.app.domain.EffectType
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.LayerType
import com.editstudio.app.domain.Project
import com.editstudio.app.engine.Keyframes
import com.editstudio.app.motion.AudioReactive
import com.editstudio.app.motion.MotionPresets
import com.editstudio.app.motion.PathAnimator
import com.editstudio.app.motion.Stagger
import com.editstudio.app.motion.Wiggle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object SharedFrameRenderer {

    fun drawProjectFrame(
        canvas: Canvas,
        project: Project,
        timeMs: Long,
        width: Float,
        height: Float,
        isExport: Boolean = false
    ) {
        if (isExport) {
            canvas.drawColor(Color.parseColor("#0B1120"))
        }

        val cx = width / 2f
        val cy = height / 2f
        val orderedLayers = project.layers.sortedBy { it.zIndex }

        for (layer in orderedLayers) {
            if (!layer.isVisible || !layer.containsTime(timeMs)) continue
            if (layer.type == LayerType.AUDIO) continue

            val localTime = (timeMs - layer.startTimeMs).coerceAtLeast(0L)
            val motion = MotionPresets.evaluatePresets(layer.motionPresets, localTime, layer.durationMs)
            val wiggle = Wiggle.evaluate(layer.wiggle, localTime)
            val pathEval = PathAnimator.evaluate(
                layer.pathAnimation,
                localTime.toFloat() / layer.durationMs.coerceAtLeast(1L).toFloat()
            )
            val audioPulse = AudioReactive.evaluatePulse(layer.audioReactive, timeMs)

            // Resolve parent offset recursively (1-level safe lookup)
            val parent = layer.parentLayerId?.let { pid -> project.layers.firstOrNull { it.id == pid } }
            val parentX = parent?.transform?.x ?: 0f
            val parentY = parent?.transform?.y ?: 0f

            val x = Keyframes.evaluateProperty(layer, AnimProperty.POSITION_X, localTime, layer.transform.x) +
                motion.offsetX + wiggle.dx + parentX + (pathEval?.x ?: 0f)
            val y = Keyframes.evaluateProperty(layer, AnimProperty.POSITION_Y, localTime, layer.transform.y) +
                motion.offsetY + wiggle.dy + parentY + (pathEval?.y ?: 0f)

            val sx = Keyframes.evaluateProperty(layer, AnimProperty.SCALE_X, localTime, layer.transform.scaleX) *
                motion.scaleX * (1f + if (layer.audioReactive.targetScale) audioPulse * 0.15f else 0f)
            val sy = Keyframes.evaluateProperty(layer, AnimProperty.SCALE_Y, localTime, layer.transform.scaleY) *
                motion.scaleY * (1f + if (layer.audioReactive.targetScale) audioPulse * 0.15f else 0f)

            val rotZ = Keyframes.evaluateProperty(layer, AnimProperty.ROTATION, localTime, layer.transform.rotationZ) +
                motion.rotationZ + wiggle.dRot + (pathEval?.angleDeg ?: 0f)

            val opacity = (Keyframes.evaluateProperty(layer, AnimProperty.OPACITY, localTime, layer.transform.opacity) *
                motion.opacityMultiplier).coerceIn(0f, 1f)

            val borderW = Keyframes.evaluateProperty(layer, AnimProperty.BORDER_WIDTH, localTime, layer.borderWidth)
            val shadowR = Keyframes.evaluateProperty(layer, AnimProperty.SHADOW_RADIUS, localTime, layer.shadowRadius)

            val alphaInt = (opacity * 255).toInt().coerceIn(0, 255)

            canvas.save()
            val scaleToCanvas = width / 1080f
            canvas.translate(cx + x * scaleToCanvas, cy + y * scaleToCanvas)
            canvas.rotate(rotZ)
            val flipX = if (layer.transform.flipHorizontal) -1f else 1f
            val flipY = if (layer.transform.flipVertical) -1f else 1f
            canvas.scale(sx * flipX * scaleToCanvas, sy * flipY * scaleToCanvas)

            drawLayerContent(canvas, layer, localTime, width, height, alphaInt, borderW, shadowR)

            // Particles if enabled
            if (layer.particles.isEnabled) {
                drawParticles(canvas, layer, localTime, alphaInt)
            }

            canvas.restore()
        }
    }

    private fun drawLayerContent(
        canvas: Canvas,
        layer: Layer,
        localTimeMs: Long,
        canvasW: Float,
        canvasH: Float,
        alpha: Int,
        borderW: Float,
        shadowR: Float
    ) {
        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.alpha = alpha
            xfermode = mapBlendMode(layer.blendMode)
            if (shadowR > 0f) {
                setShadowLayer(shadowR, 0f, 6f, layer.shadowColor.toInt())
            }
        }

        when (layer.type) {
            LayerType.SHAPE -> {
                val sc = layer.shapeContent ?: return
                ShapeRenderer.drawShapeOnCanvas(canvas, sc, 260f, 260f, alpha)
            }
            LayerType.DRAWING -> {
                val dc = layer.drawingContent ?: return
                ShapeRenderer.drawDrawingOnCanvas(canvas, dc, alpha)
            }
            LayerType.TEXT -> {
                val tc = layer.textContent ?: return
                val visibleCount = Stagger.visibleCharacterCount(tc.text, layer.stagger, localTimeMs)
                val shownText = tc.text.take(visibleCount)
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = tc.textColor.toInt()
                    this.alpha = alpha
                    textSize = tc.fontSizeSp * 2.4f
                    isFakeBoldText = tc.isBold
                    letterSpacing = tc.letterSpacing * 0.05f
                    textAlign = when (tc.alignment) {
                        com.editstudio.app.domain.TextAlignment.LEFT -> Paint.Align.LEFT
                        com.editstudio.app.domain.TextAlignment.CENTER -> Paint.Align.CENTER
                        com.editstudio.app.domain.TextAlignment.RIGHT -> Paint.Align.RIGHT
                    }
                    if (tc.shadowRadius > 0f) {
                        setShadowLayer(tc.shadowRadius, 0f, 4f, tc.shadowColor.toInt())
                    }
                }
                if ((tc.backgroundColor ushr 24) > 0) {
                    val tw = textPaint.measureText(shownText) + 48f
                    val th = textPaint.textSize + 36f
                    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = tc.backgroundColor.toInt()
                        this.alpha = alpha
                    }
                    canvas.drawRoundRect(RectF(-tw / 2f, -th / 2f, tw / 2f, th / 2f), 16f, 16f, bgPaint)
                }
                if (tc.outlineWidth > 0f) {
                    val outlinePaint = Paint(textPaint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = tc.outlineWidth * 2f
                        color = tc.outlineColor.toInt()
                    }
                    canvas.drawText(shownText, 0f, textPaint.textSize * 0.35f, outlinePaint)
                }
                canvas.drawText(shownText, 0f, textPaint.textSize * 0.35f, textPaint)
            }
            LayerType.SOLID -> {
                basePaint.color = layer.solidColor.toInt()
                canvas.drawRoundRect(RectF(-380f, -380f, 380f, 380f), 16f, 16f, basePaint)
            }
            LayerType.GRADIENT -> {
                val c1 = layer.gradientColors.getOrElse(0) { 0xFF38BDF8 }.toInt()
                val c2 = layer.gradientColors.getOrElse(1) { 0xFF14B8A6 }.toInt()
                basePaint.shader = LinearGradient(-300f, -300f, 300f, 300f, c1, c2, Shader.TileMode.CLAMP)
                canvas.drawRoundRect(RectF(-380f, -380f, 380f, 380f), 16f, 16f, basePaint)
            }
            LayerType.VIDEO, LayerType.IMAGE -> {
                basePaint.color = if (layer.type == LayerType.VIDEO) Color.parseColor("#1E3A5F") else Color.parseColor("#1D4E4F")
                canvas.drawRoundRect(RectF(-320f, -220f, 320f, 220f), 20f, 20f, basePaint)
            }
            LayerType.ADJUSTMENT -> {
                val hasBw = layer.effects.any { it.type == EffectType.BLACK_WHITE && it.isEnabled }
                basePaint.color = if (hasBw) Color.argb(60, 180, 190, 210) else Color.argb(40, 56, 189, 248)
                canvas.drawRect(-540f, -960f, 540f, 960f, basePaint)
            }
            else -> {
                basePaint.color = Color.parseColor("#253456")
                canvas.drawRoundRect(RectF(-160f, -90f, 160f, 90f), 16f, 16f, basePaint)
            }
        }

        if (borderW > 0.5f && layer.type != LayerType.SHAPE && layer.type != LayerType.DRAWING) {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = borderW
                color = layer.borderColor.toInt()
                this.alpha = alpha
            }
            canvas.drawRoundRect(RectF(-320f, -220f, 320f, 220f), 20f, 20f, borderPaint)
        }
    }

    private fun drawParticles(canvas: Canvas, layer: Layer, localTimeMs: Long, alpha: Int) {
        val p = layer.particles
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.primaryColor.toInt()
            this.alpha = alpha
        }
        val t = localTimeMs / 1000f
        for (i in 0 until p.amount.coerceIn(5, 80)) {
            val angle = (i * 137.5f) * (PI / 180.0)
            val dist = ((t * 140f * p.speed + i * 27f) % 420f)
            val px = (cos(angle) * dist).toFloat()
            val py = (sin(angle) * dist).toFloat()
            canvas.drawCircle(px, py, (4f * p.size).coerceIn(2f, 18f), paint)
        }
    }

    private fun mapBlendMode(mode: BlendMode): PorterDuffXfermode? {
        return when (mode) {
            BlendMode.NORMAL -> null
            BlendMode.MULTIPLY -> PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
            BlendMode.SCREEN -> PorterDuffXfermode(PorterDuff.Mode.SCREEN)
            BlendMode.OVERLAY -> PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
            BlendMode.ADD -> PorterDuffXfermode(PorterDuff.Mode.ADD)
            BlendMode.DARKEN -> PorterDuffXfermode(PorterDuff.Mode.DARKEN)
            BlendMode.LIGHTEN -> PorterDuffXfermode(PorterDuff.Mode.LIGHTEN)
            else -> PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        }
    }
}
