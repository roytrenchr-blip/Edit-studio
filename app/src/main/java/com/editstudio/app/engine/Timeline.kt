package com.editstudio.app.engine

import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.Project
import kotlin.math.abs

object Timeline {
    const val SNAP_THRESHOLD_MS = 120L

    fun snapTime(candidateTimeMs: Long, project: Project, excludeLayerId: String? = null): Long {
        return snapTimeWithResult(candidateTimeMs, project, excludeLayerId).first
    }

    fun snapTimeWithResult(
        candidateTimeMs: Long,
        project: Project,
        excludeLayerId: String? = null,
        extraSnapPoints: List<Long> = emptyList()
    ): Pair<Long, Boolean> {
        var closest = candidateTimeMs
        var minDiff = SNAP_THRESHOLD_MS
        var snapped = false

        val targets = mutableListOf(0L, project.durationMs)
        targets.addAll(project.bookmarksMs)
        targets.addAll(extraSnapPoints)

        for (layer in project.layers) {
            if (layer.id == excludeLayerId) continue
            targets.add(layer.startTimeMs)
            targets.add(layer.endTimeMs)
        }

        for (target in targets) {
            val diff = abs(candidateTimeMs - target)
            if (diff < minDiff) {
                minDiff = diff
                closest = target
                snapped = true
            }
        }
        return Pair(closest.coerceAtLeast(0L), snapped)
    }

    fun splitLayer(layer: Layer, splitTimeMs: Long): Pair<Layer, Layer>? {
        if (splitTimeMs <= layer.startTimeMs || splitTimeMs >= layer.endTimeMs) {
            return null
        }
        val firstDuration = splitTimeMs - layer.startTimeMs
        val secondDuration = layer.endTimeMs - splitTimeMs

        val firstPart = layer.copy(durationMs = firstDuration)
        val secondPart = layer.copy(
            id = java.util.UUID.randomUUID().toString(),
            name = "${layer.name} (Split)",
            startTimeMs = splitTimeMs,
            durationMs = secondDuration
        )
        return Pair(firstPart, secondPart)
    }

    fun trimLayer(layer: Layer, newStartTimeMs: Long, newDurationMs: Long): Layer {
        val safeDuration = newDurationMs.coerceAtLeast(100L)
        val safeStart = newStartTimeMs.coerceAtLeast(0L)
        return layer.copy(startTimeMs = safeStart, durationMs = safeDuration)
    }

    fun stepPlayhead(currentTimeMs: Long, forward: Boolean, fps: Int, totalDurationMs: Long): Long {
        val frameDurationMs = (1000.0 / fps).toLong().coerceAtLeast(1L)
        val target = if (forward) currentTimeMs + frameDurationMs else currentTimeMs - frameDurationMs
        return target.coerceIn(0L, totalDurationMs)
    }

    fun findAdjacentKeyframeTime(layer: Layer?, currentTimeMs: Long, forward: Boolean): Long? {
        if (layer == null) return null
        val allGlobalTimes = layer.keyframeTracks
            .flatMap { it.keyframes }
            .map { layer.startTimeMs + it.timeMs }
            .distinct()
            .sorted()

        return if (forward) {
            allGlobalTimes.firstOrNull { it > currentTimeMs + 20L }
        } else {
            allGlobalTimes.lastOrNull { it < currentTimeMs - 20L }
        }
    }
}
