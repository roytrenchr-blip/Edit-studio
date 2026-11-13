package com.editstudio.app.engine

import com.editstudio.app.domain.BezierControlPoints
import com.editstudio.app.domain.EasingType
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

object Easing {
    fun evaluate(type: EasingType, t: Float, bezier: BezierControlPoints = BezierControlPoints()): Float {
        val clamped = t.coerceIn(0f, 1f)
        return when (type) {
            EasingType.LINEAR -> clamped
            EasingType.EASE_IN -> clamped * clamped
            EasingType.EASE_OUT -> clamped * (2f - clamped)
            EasingType.EASE_IN_OUT -> if (clamped < 0.5f) 2f * clamped * clamped else -1f + (4f - 2f * clamped) * clamped
            EasingType.BOUNCE -> evaluateBounce(clamped)
            EasingType.ELASTIC -> evaluateElastic(clamped)
            EasingType.BACK -> evaluateBack(clamped)
            EasingType.BEZIER_CUSTOM -> evaluateCubicBezier(clamped, bezier.p1x, bezier.p1y, bezier.p2x, bezier.p2y)
        }
    }

    private fun evaluateBounce(t: Float): Float {
        var p = t
        return if (p < 1f / 2.75f) {
            7.5625f * p * p
        } else if (p < 2f / 2.75f) {
            p -= 1.5f / 2.75f
            7.5625f * p * p + 0.75f
        } else if (p < 2.5f / 2.75f) {
            p -= 2.25f / 2.75f
            7.5625f * p * p + 0.9375f
        } else {
            p -= 2.625f / 2.75f
            7.5625f * p * p + 0.984375f
        }
    }

    private fun evaluateElastic(t: Float): Float {
        if (t == 0f) return 0f
        if (t == 1f) return 1f
        val p = 0.3f
        val s = p / 4f
        return (2f.pow(-10f * t) * sin((t - s) * (2f * PI.toFloat()) / p) + 1f)
    }

    private fun evaluateBack(t: Float): Float {
        val s = 1.70158f
        val p = t - 1f
        return p * p * ((s + 1f) * p + s) + 1f
    }

    fun evaluateCubicBezier(t: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        var low = 0f
        var high = 1f
        var mid = t
        for (i in 0 until 12) {
            mid = (low + high) * 0.5f
            val x = sampleBezier(mid, x1, x2)
            if (x < t) low = mid else high = mid
        }
        return sampleBezier(mid, y1, y2)
    }

    private fun sampleBezier(t: Float, c1: Float, c2: Float): Float {
        val inv = 1f - t
        return 3f * inv * inv * t * c1 + 3f * inv * t * t * c2 + t * t * t
    }
}
