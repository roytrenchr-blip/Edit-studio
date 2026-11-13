package com.editstudio.app

import com.editstudio.app.domain.AnimProperty
import com.editstudio.app.domain.AspectRatio
import com.editstudio.app.domain.EasingType
import com.editstudio.app.domain.FrameRate
import com.editstudio.app.domain.Keyframe
import com.editstudio.app.domain.KeyframeTrack
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.LayerType
import com.editstudio.app.domain.Project
import com.editstudio.app.domain.SpeedPoint
import com.editstudio.app.domain.SpeedRampConfig
import com.editstudio.app.domain.SpeedRampPreset
import com.editstudio.app.domain.VideoResolution
import com.editstudio.app.engine.Easing
import com.editstudio.app.engine.Keyframes
import com.editstudio.app.engine.ProjectSerializer
import com.editstudio.app.engine.Timeline
import com.editstudio.app.engine.UndoManager
import com.editstudio.app.motion.SpeedRamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorEngineUnitTest {

    @Test
    fun testEasingCalculations() {
        assertEquals(0f, Easing.evaluate(EasingType.LINEAR, 0f), 0.001f)
        assertEquals(1f, Easing.evaluate(EasingType.LINEAR, 1f), 0.001f)
        assertEquals(0.5f, Easing.evaluate(EasingType.LINEAR, 0.5f), 0.001f)

        // Ease in accelerates
        val easeIn = Easing.evaluate(EasingType.EASE_IN, 0.5f)
        assertTrue(easeIn < 0.5f)

        // Ease out decelerates
        val easeOut = Easing.evaluate(EasingType.EASE_OUT, 0.5f)
        assertTrue(easeOut > 0.5f)
    }

    @Test
    fun testTimelineSnapping() {
        val layer1 = Layer(startTimeMs = 1000L, durationMs = 2000L)
        val project = Project(durationMs = 10000L, layers = listOf(layer1))

        // Near 1000ms should snap to 1000ms
        val snappedStart = Timeline.snapTime(1050L, project)
        assertEquals(1000L, snappedStart)

        // Near 3000ms (end of layer1) should snap to 3000ms
        val snappedEnd = Timeline.snapTime(2960L, project)
        assertEquals(3000L, snappedEnd)
    }

    @Test
    fun testLayerSplitting() {
        val layer = Layer(startTimeMs = 1000L, durationMs = 4000L)
        val split = Timeline.splitLayer(layer, 2500L)
        assertNotNull(split)

        assertEquals(1000L, split!!.first.startTimeMs)
        assertEquals(1500L, split.first.durationMs)

        assertEquals(2500L, split.second.startTimeMs)
        assertEquals(2500L, split.second.durationMs)
    }

    @Test
    fun testKeyframeInterpolation() {
        val track = KeyframeTrack(
            property = AnimProperty.SCALE_X,
            keyframes = listOf(
                Keyframe(timeMs = 0L, value = 1.0f, easing = EasingType.LINEAR),
                Keyframe(timeMs = 1000L, value = 2.0f, easing = EasingType.LINEAR)
            )
        )
        val layer = Layer(keyframeTracks = listOf(track))

        val midVal = Keyframes.evaluateProperty(layer, AnimProperty.SCALE_X, 500L, 1.0f)
        assertEquals(1.5f, midVal, 0.05f)
    }

    @Test
    fun testUndoRedoManager() {
        val manager = UndoManager(maxHistory = 50)
        val p1 = Project(name = "Project 1")
        val p2 = Project(name = "Project 2")

        manager.recordSnapshot(p1)
        assertTrue(manager.canUndo)
        assertFalse(manager.canRedo)

        val undone = manager.undo(p2)
        assertEquals("Project 1", undone?.name)
        assertTrue(manager.canRedo)

        val redone = manager.redo(p1)
        assertEquals("Project 2", redone?.name)
    }

    @Test
    fun testSpeedRampMapping() {
        val config = SpeedRampConfig(
            isEnabled = true,
            preset = SpeedRampPreset.BULLET_TIME,
            points = listOf(SpeedPoint(0f, 1f), SpeedPoint(1f, 1f))
        )
        val mediaTime = SpeedRamp.mapLayerTimeToMediaTime(2000L, 5000L, config)
        assertTrue(mediaTime >= 0L)
    }

    @Test
    fun testProjectSerialization() {
        val project = Project(
            name = "Test Serialize",
            aspectRatio = AspectRatio.PORTRAIT_9_16,
            resolution = VideoResolution.FHD_1080P,
            frameRate = FrameRate.FPS_30,
            layers = listOf(Layer(name = "Title Text", type = LayerType.TEXT))
        )
        val json = ProjectSerializer.serializeProject(project)
        assertTrue(json.contains("Test Serialize"))

        val deserialized = ProjectSerializer.deserializeProject(json)
        assertEquals(project.name, deserialized.name)
        assertEquals(project.aspectRatio, deserialized.aspectRatio)
        assertEquals(1, deserialized.layers.size)
    }
}
