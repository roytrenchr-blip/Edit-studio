package com.editstudio.app.util

import java.util.Locale

object TimeFormat {
    fun formatTimecode(timeMs: Long): String {
        val totalSeconds = (timeMs / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val frames = ((timeMs % 1000) * 30 / 1000).coerceIn(0, 29)
        return String.format(Locale.US, "%02d:%02d:%02d", minutes, seconds, frames)
    }

    fun formatShortTime(timeMs: Long): String {
        val totalSeconds = (timeMs / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val millis = ((timeMs % 1000) / 100).coerceIn(0, 9)
        return String.format(Locale.US, "%02d:%02d.%d", minutes, seconds, millis)
    }

    fun framesToMs(frame: Int, fps: Int): Long {
        return ((frame.toDouble() / fps) * 1000.0).toLong()
    }

    fun msToFrames(timeMs: Long, fps: Int): Int {
        return ((timeMs.toDouble() / 1000.0) * fps).toInt()
    }
}
