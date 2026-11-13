package com.editstudio.app.motion

import com.editstudio.app.domain.EasingType
import com.editstudio.app.domain.MotionPhase
import com.editstudio.app.domain.MotionPresetConfig
import com.editstudio.app.domain.MotionPresetType
import com.editstudio.app.engine.Easing
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

data class MotionEvaluationResult(
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotationZ: Float = 0f,
    val rotationY: Float = 0f,
    val opacityMultiplier: Float = 1f,
    val revealProgress: Float = 1f,
    val glitchAmount: Float = 0f,
    val flashAlpha: Float = 0f
)

object MotionPresets {

    fun evaluatePresets(
        presets: List<MotionPresetConfig>,
        layerLocalTimeMs: Long,
        layerDurationMs: Long
    ): MotionEvaluationResult {
        var result = MotionEvaluationResult()
        for (preset in presets) {
            if (!preset.isEnabled) continue
            val eval = evaluateSinglePreset(preset, layerLocalTimeMs, layerDurationMs)
            result = result.copy(
                offsetX = result.offsetX + eval.offsetX,
                offsetY = result.offsetY + eval.offsetY,
                scaleX = result.scaleX * eval.scaleX,
                scaleY = result.scaleY * eval.scaleY,
                rotationZ = result.rotationZ + eval.rotationZ,
                rotationY = result.rotationY + eval.rotationY,
                opacityMultiplier = (result.opacityMultiplier * eval.opacityMultiplier).coerceIn(0f, 1f),
                revealProgress = minOf(result.revealProgress, eval.revealProgress),
                glitchAmount = maxOf(result.glitchAmount, eval.glitchAmount),
                flashAlpha = maxOf(result.flashAlpha, eval.flashAlpha)
            )
        }
        return result
    }

    private fun evaluateSinglePreset(
        config: MotionPresetConfig,
        localTimeMs: Long,
        layerDurationMs: Long
    ): MotionEvaluationResult {
        val dur = config.durationMs.coerceAtLeast(100L)
        val rawT: Float = when (config.presetType.phase) {
            MotionPhase.IN -> {
                if (localTimeMs >= dur) return MotionEvaluationResult()
                (localTimeMs.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
            }
            MotionPhase.OUT -> {
                val startOut = (layerDurationMs - dur).coerceAtLeast(0L)
                if (localTimeMs <= startOut) return MotionEvaluationResult()
                ((localTimeMs - startOut).toFloat() / dur.toFloat()).coerceIn(0f, 1f)
            }
            MotionPhase.LOOP -> {
                ((localTimeMs % dur).toFloat() / dur.toFloat()).coerceIn(0f, 1f)
            }
        }

        val t = Easing.evaluate(config.easing, rawT)
        val intensity = config.intensity

        return when (config.presetType) {
            MotionPresetType.FADE_IN -> MotionEvaluationResult(opacityMultiplier = t)
            MotionPresetType.FADE_OUT -> MotionEvaluationResult(opacityMultiplier = 1f - t)
            MotionPresetType.SLIDE_UP -> MotionEvaluationResult(offsetY = (1f - t) * 400f * intensity, opacityMultiplier = t)
            MotionPresetType.SLIDE_DOWN -> MotionEvaluationResult(offsetY = -(1f - t) * 400f * intensity, opacityMultiplier = t)
            MotionPresetType.SLIDE_LEFT -> MotionEvaluationResult(offsetX = (1f - t) * 400f * intensity, opacityMultiplier = t)
            MotionPresetType.SLIDE_RIGHT -> MotionEvaluationResult(offsetX = -(1f - t) * 400f * intensity, opacityMultiplier = t)
            MotionPresetType.SLIDE_OUT_DOWN -> MotionEvaluationResult(offsetY = t * 400f * intensity, opacityMultiplier = 1f - t)
            MotionPresetType.ZOOM_IN -> MotionEvaluationResult(scaleX = 0.2f + 0.8f * t, scaleY = 0.2f + 0.8f * t, opacityMultiplier = t)
            MotionPresetType.ZOOM_OUT -> MotionEvaluationResult(scaleX = 1.8f - 0.8f * t, scaleY = 1.8f - 0.8f * t, opacityMultiplier = t)
            MotionPresetType.ZOOM_OUT_DISAPPEAR -> MotionEvaluationResult(scaleX = 1f - 0.8f * t, scaleY = 1f - 0.8f * t, opacityMultiplier = 1f - t)
            MotionPresetType.SPIN_IN -> MotionEvaluationResult(rotationZ = (1f - t) * -360f * intensity, scaleX = t, scaleY = t, opacityMultiplier = t)
            MotionPresetType.FLIP_IN -> MotionEvaluationResult(rotationY = (1f - t) * 90f, opacityMultiplier = t)
            MotionPresetType.BOUNCE_IN -> {
                val b = Easing.evaluate(EasingType.BOUNCE, rawT)
                MotionEvaluationResult(offsetY = (1f - b) * -280f * intensity, scaleX = 0.5f + 0.5f * b, scaleY = 0.5f + 0.5f * b)
            }
            MotionPresetType.POP_IN -> {
                val e = Easing.evaluate(EasingType.ELASTIC, rawT)
                MotionEvaluationResult(scaleX = e, scaleY = e, opacityMultiplier = rawT.coerceIn(0f, 1f))
            }
            MotionPresetType.RUBBER_BAND -> {
                val s = sin(rawT * PI * 3f).toFloat() * (1f - rawT) * 0.35f * intensity
                MotionEvaluationResult(scaleX = 1f + s, scaleY = 1f - s)
            }
            MotionPresetType.PULSE_LOOP -> {
                val p = 1f + sin(rawT * 2f * PI).toFloat() * 0.12f * intensity
                MotionEvaluationResult(scaleX = p, scaleY = p)
            }
            MotionPresetType.FLOAT_LOOP -> {
                val y = sin(rawT * 2f * PI).toFloat() * 18f * intensity
                MotionEvaluationResult(offsetY = y)
            }
            MotionPresetType.SWING_LOOP -> {
                val rot = sin(rawT * 2f * PI).toFloat() * 12f * intensity
                MotionEvaluationResult(rotationZ = rot)
            }
            MotionPresetType.WIGGLE_LOOP -> {
                val wx = sin(rawT * 6f * PI).toFloat() * 12f * intensity
                val wy = cos(rawT * 4f * PI).toFloat() * 10f * intensity
                MotionEvaluationResult(offsetX = wx, offsetY = wy)
            }
            MotionPresetType.TADA_POP -> {
                val s = 1f + sin(rawT * 2f * PI).toFloat() * 0.15f * intensity
                val r = sin(rawT * 6f * PI).toFloat() * 8f * intensity
                MotionEvaluationResult(scaleX = s, scaleY = s, rotationZ = r)
            }
            MotionPresetType.FLASH_BLINK -> {
                val a = if (rawT < 0.5f) 1f else 0.2f
                MotionEvaluationResult(opacityMultiplier = a)
            }
            MotionPresetType.WHIP_PAN -> MotionEvaluationResult(offsetX = (1f - t) * 700f * intensity)
            MotionPresetType.PARALLAX_DRIFT -> MotionEvaluationResult(offsetX = sin(rawT * 2f * PI).toFloat() * 24f * intensity)
            MotionPresetType.DOLLY_ZOOM -> MotionEvaluationResult(scaleX = 0.85f + 0.15f * t, scaleY = 0.85f + 0.15f * t)
            MotionPresetType.RACK_FOCUS -> MotionEvaluationResult(scaleX = 1.05f - 0.05f * t, scaleY = 1.05f - 0.05f * t, opacityMultiplier = 0.4f + 0.6f * t)
            MotionPresetType.HANDHELD_SHAKE -> {
                val hx = (sin(rawT * 8f * PI) + cos(rawT * 13f * PI)).toFloat() * 6f * intensity
                val hy = (cos(rawT * 9f * PI) + sin(rawT * 5f * PI)).toFloat() * 6f * intensity
                MotionEvaluationResult(offsetX = hx, offsetY = hy)
            }
            MotionPresetType.TILT_PAN -> MotionEvaluationResult(offsetX = (1f - t) * -120f, rotationZ = (1f - t) * -6f)
            MotionPresetType.ORBIT_3D -> MotionEvaluationResult(rotationY = rawT * 360f)
            MotionPresetType.MASK_WIPE,
            MotionPresetType.CIRCLE_REVEAL,
            MotionPresetType.LINE_REVEAL,
            MotionPresetType.CURTAIN_REVEAL,
            MotionPresetType.CLOCK_WIPE,
            MotionPresetType.INK_DROP,
            MotionPresetType.STROKE_DRAW -> MotionEvaluationResult(revealProgress = t)
            MotionPresetType.GLITCH_IN,
            MotionPresetType.VHS_JUMP,
            MotionPresetType.DATA_MOSH,
            MotionPresetType.SCAN_LINES_SWEEP -> MotionEvaluationResult(
                glitchAmount = (1f - abs(rawT - 0.5f) * 2f) * intensity,
                offsetX = sin(rawT * 20f * PI).toFloat() * 14f * intensity
            )
            MotionPresetType.ZOOM_PUNCH_BEAT -> {
                val punch = 1f + (1f - rawT) * 0.25f * intensity
                MotionEvaluationResult(scaleX = punch, scaleY = punch)
            }
            MotionPresetType.FLASH_CUT -> MotionEvaluationResult(flashAlpha = (1f - rawT) * intensity)
            MotionPresetType.SWIPE_UP_STORY -> MotionEvaluationResult(offsetY = (1f - t) * 500f, opacityMultiplier = t)
            MotionPresetType.HEART_BURST -> {
                val e = Easing.evaluate(EasingType.BACK, rawT)
                MotionEvaluationResult(scaleX = e, scaleY = e, opacityMultiplier = rawT.coerceIn(0f, 1f))
            }
            MotionPresetType.COUNTDOWN_POP -> {
                val s = 1.6f - 0.6f * t
                MotionEvaluationResult(scaleX = s, scaleY = s, opacityMultiplier = t)
            }
        }
    }
}
