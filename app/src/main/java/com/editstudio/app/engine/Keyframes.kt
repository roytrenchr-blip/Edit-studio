package com.editstudio.app.engine

import com.editstudio.app.domain.AnimProperty
import com.editstudio.app.domain.EasingType
import com.editstudio.app.domain.Keyframe
import com.editstudio.app.domain.KeyframeTrack
import com.editstudio.app.domain.Layer

object Keyframes {
    fun evaluateProperty(layer: Layer, property: AnimProperty, timeMs: Long, defaultValue: Float): Float {
        val track = layer.keyframeTracks.firstOrNull { it.property == property } ?: return defaultValue
        val frames = track.keyframes.sortedBy { it.timeMs }
        if (frames.isEmpty()) return defaultValue
        if (frames.size == 1 || timeMs <= frames.first().timeMs) return frames.first().value
        if (timeMs >= frames.last().timeMs) return frames.last().value

        for (i in 0 until frames.size - 1) {
            val kf1 = frames[i]
            val kf2 = frames[i + 1]
            if (timeMs in kf1.timeMs..kf2.timeMs) {
                val span = (kf2.timeMs - kf1.timeMs).coerceAtLeast(1L)
                val linearT = (timeMs - kf1.timeMs).toFloat() / span.toFloat()
                val easedT = Easing.evaluate(kf1.easing, linearT, kf1.bezier)
                return kf1.value + (kf2.value - kf1.value) * easedT
            }
        }
        return defaultValue
    }

    fun smoothKeyframes(track: KeyframeTrack): KeyframeTrack {
        val smoothed = track.keyframes.mapIndexed { index, kf ->
            if (index == 0 || index == track.keyframes.size - 1) kf
            else kf.copy(easing = EasingType.EASE_IN_OUT)
        }
        return track.copy(keyframes = smoothed)
    }

    fun toggleKeyframe(layer: Layer, property: AnimProperty, layerTimeMs: Long, value: Float): Layer {
        val tracks = layer.keyframeTracks.toMutableList()
        val existingIdx = tracks.indexOfFirst { it.property == property }
        val quantizedTime = (layerTimeMs / 10L) * 10L
        if (existingIdx != -1) {
            val kfs = tracks[existingIdx].keyframes
            val match = kfs.firstOrNull { kotlin.math.abs(it.timeMs - quantizedTime) <= 30L }
            val updated = if (match != null) {
                kfs.filter { it !== match }
            } else {
                (kfs + Keyframe(timeMs = quantizedTime, value = value)).sortedBy { it.timeMs }
            }
            if (updated.isEmpty()) tracks.removeAt(existingIdx)
            else tracks[existingIdx] = tracks[existingIdx].copy(keyframes = updated)
        } else {
            tracks.add(KeyframeTrack(property = property, keyframes = listOf(Keyframe(timeMs = quantizedTime, value = value))))
        }
        return layer.copy(keyframeTracks = tracks)
    }

    fun hasKeyframeNear(layer: Layer, property: AnimProperty, layerTimeMs: Long): Boolean {
        val track = layer.keyframeTracks.firstOrNull { it.property == property } ?: return false
        return track.keyframes.any { kotlin.math.abs(it.timeMs - layerTimeMs) <= 35L }
    }
}
