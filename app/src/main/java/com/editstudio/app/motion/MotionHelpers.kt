package com.editstudio.app.motion

import com.editstudio.app.domain.Camera3DConfig
import com.editstudio.app.domain.MotionBlurConfig
import com.editstudio.app.domain.ParticleConfig
import com.editstudio.app.domain.PathAnimationConfig
import com.editstudio.app.domain.StaggerConfig
import com.editstudio.app.domain.WiggleConfig
import com.editstudio.app.domain.AudioReactiveConfig
import com.editstudio.app.engine.Easing
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object MotionBlur {
    fun computeShutterAlpha(config: MotionBlurConfig): Float {
        if (!config.isEnabled) return 0f
        return (config.shutterAngle / 360f).coerceIn(0f, 1f)
    }
}

data class PathEval(val x: Float, val y: Float, val angleDeg: Float)

object PathAnimator {
    fun evaluate(config: PathAnimationConfig, progress: Float): PathEval? {
        if (!config.isEnabled || config.points.size < 2) return null
        val t = Easing.evaluate(config.easing, if (config.isLooping) progress % 1f else progress.coerceIn(0f, 1f))
        val pts = config.points
        val totalSegs = pts.size - 1
        val scaled = t * totalSegs
        val idx = scaled.toInt().coerceIn(0, totalSegs - 1)
        val segT = (scaled - idx).coerceIn(0f, 1f)
        val p1 = pts[idx]
        val p2 = pts[idx + 1]
        val x = p1.x + (p2.x - p1.x) * segT
        val y = p1.y + (p2.y - p1.y) * segT
        val angle = if (config.autoOrient) {
            Math.toDegrees(atan2((p2.y - p1.y).toDouble(), (p2.x - p1.x).toDouble())).toFloat()
        } else 0f
        return PathEval(x, y, angle)
    }
}

data class WiggleOffset(val dx: Float, val dy: Float, val dRot: Float)

object Wiggle {
    fun evaluate(config: WiggleConfig, timeMs: Long): WiggleOffset {
        if (!config.isEnabled) return WiggleOffset(0f, 0f, 0f)
        val sec = timeMs / 1000f
        val f = config.frequencyHz
        val dx = (sin(sec * f * 2f * PI) * 0.7f + cos(sec * f * 3.1f * PI) * 0.3f).toFloat() * config.amplitudePx
        val dy = (cos(sec * f * 2.3f * PI) * 0.6f + sin(sec * f * 1.7f * PI) * 0.4f).toFloat() * config.amplitudePx
        val dRot = sin(sec * f * 1.9f * PI).toFloat() * config.rotateAmplitudeDeg
        return WiggleOffset(dx, dy, dRot)
    }
}

object Stagger {
    fun visibleCharacterCount(fullText: String, config: StaggerConfig, layerLocalTimeMs: Long): Int {
        if (!config.isEnabled || fullText.isEmpty()) return fullText.length
        val delay = config.delayMsPerUnit.coerceAtLeast(10L)
        val units = (layerLocalTimeMs / delay).toInt().coerceAtLeast(0)
        return units.coerceAtMost(fullText.length)
    }
}

object Camera3D {
    fun applyParallax(x: Float, y: Float, zIndex: Int, config: Camera3DConfig): Pair<Float, Float> {
        if (!config.isEnabled) return Pair(x, y)
        val depthScale = 1f + (zIndex * 0.08f * config.parallaxFactor)
        return Pair(
            (x - config.posX) * config.zoom * depthScale,
            (y - config.posY) * config.zoom * depthScale
        )
    }
}

object BeatDetector {
    fun generateBeatMarkers(durationMs: Long, bpm: Int = 120): List<Long> {
        val interval = (60_000L / bpm.coerceIn(40, 240)).coerceAtLeast(100L)
        val result = mutableListOf<Long>()
        var t = 0L
        while (t <= durationMs) {
            result.add(t)
            t += interval
        }
        return result
    }
}

object AudioReactive {
    fun evaluatePulse(config: AudioReactiveConfig, timeMs: Long, bpm: Int = 120): Float {
        if (!config.isEnabled) return 0f
        val intervalMs = (60_000f / bpm.coerceIn(40, 240)).coerceAtLeast(100f)
        val phase = ((timeMs % intervalMs.toLong()).toFloat() / intervalMs)
        return (1f - phase).coerceIn(0f, 1f) * config.sensitivity
    }
}
