package com.editstudio.app.motion

import com.editstudio.app.domain.SpeedPoint
import com.editstudio.app.domain.SpeedRampConfig
import com.editstudio.app.domain.SpeedRampPreset

object SpeedRamp {

    fun getPresetPoints(preset: SpeedRampPreset): List<SpeedPoint> {
        return when (preset) {
            SpeedRampPreset.CUSTOM -> listOf(SpeedPoint(0f, 1f), SpeedPoint(0.5f, 1f), SpeedPoint(1f, 1f))
            SpeedRampPreset.HERO_TIME -> listOf(
                SpeedPoint(0.0f, 2.5f),
                SpeedPoint(0.3f, 0.25f),
                SpeedPoint(0.7f, 0.25f),
                SpeedPoint(1.0f, 2.0f)
            )
            SpeedRampPreset.BULLET_TIME -> listOf(
                SpeedPoint(0.0f, 3.0f),
                SpeedPoint(0.25f, 0.15f),
                SpeedPoint(0.75f, 0.15f),
                SpeedPoint(1.0f, 3.0f)
            )
            SpeedRampPreset.MONTAGE -> listOf(
                SpeedPoint(0.0f, 1.0f),
                SpeedPoint(0.25f, 4.0f),
                SpeedPoint(0.5f, 0.5f),
                SpeedPoint(0.75f, 4.0f),
                SpeedPoint(1.0f, 1.0f)
            )
            SpeedRampPreset.FLASH_IN -> listOf(
                SpeedPoint(0.0f, 8.0f),
                SpeedPoint(0.35f, 1.0f),
                SpeedPoint(1.0f, 1.0f)
            )
            SpeedRampPreset.FLASH_OUT -> listOf(
                SpeedPoint(0.0f, 1.0f),
                SpeedPoint(0.65f, 1.0f),
                SpeedPoint(1.0f, 8.0f)
            )
            SpeedRampPreset.PULSE -> listOf(
                SpeedPoint(0.0f, 0.5f),
                SpeedPoint(0.33f, 3.0f),
                SpeedPoint(0.66f, 0.5f),
                SpeedPoint(1.0f, 3.0f)
            )
            SpeedRampPreset.JUMP_CUT -> listOf(
                SpeedPoint(0.0f, 1.0f),
                SpeedPoint(0.5f, 5.0f),
                SpeedPoint(1.0f, 1.0f)
            )
            SpeedRampPreset.SLOW_FAST_REVERSE -> listOf(
                SpeedPoint(0.0f, 0.4f),
                SpeedPoint(0.5f, 3.5f),
                SpeedPoint(1.0f, 0.5f)
            )
        }
    }

    fun evaluateSpeedAt(normalizedTime: Float, config: SpeedRampConfig): Float {
        if (!config.isEnabled || config.points.isEmpty()) return 1.0f
        val sorted = config.points.sortedBy { it.normalizedTime }
        val t = normalizedTime.coerceIn(0f, 1f)
        if (t <= sorted.first().normalizedTime) return sorted.first().speedMultiplier
        if (t >= sorted.last().normalizedTime) return sorted.last().speedMultiplier

        for (i in 0 until sorted.size - 1) {
            val p1 = sorted[i]
            val p2 = sorted[i + 1]
            if (t in p1.normalizedTime..p2.normalizedTime) {
                val span = (p2.normalizedTime - p1.normalizedTime).coerceAtLeast(0.001f)
                val alpha = (t - p1.normalizedTime) / span
                return (p1.speedMultiplier + (p2.speedMultiplier - p1.speedMultiplier) * alpha).coerceIn(0.1f, 10f)
            }
        }
        return 1.0f
    }

    fun mapLayerTimeToMediaTime(
        layerLocalTimeMs: Long,
        layerDurationMs: Long,
        config: SpeedRampConfig
    ): Long {
        val safeDur = layerDurationMs.coerceAtLeast(1L)
        val clamped = layerLocalTimeMs.coerceIn(0L, safeDur)
        val baseTime = if (config.isReversed) safeDur - clamped else clamped

        if (!config.isEnabled || config.points.size < 2) {
            return baseTime
        }

        val steps = 24
        val targetNorm = baseTime.toFloat() / safeDur.toFloat()
        val dt = targetNorm / steps.toFloat()
        var accumMediaNorm = 0f
        for (i in 0 until steps) {
            val sampleT = (i + 0.5f) * dt
            val speed = evaluateSpeedAt(sampleT, config)
            accumMediaNorm += speed * dt
        }
        return (accumMediaNorm * safeDur).toLong().coerceAtLeast(0L)
    }
}
