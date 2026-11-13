package com.editstudio.app.render

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.editstudio.app.domain.BlendMode
import com.editstudio.app.domain.EffectType
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.Project
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

object BlendModes {
    fun applyGlBlend(mode: BlendMode) {
        GLES30.glEnable(GLES30.GL_BLEND)
        when (mode) {
            BlendMode.ADD, BlendMode.COLOR_DODGE -> GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE)
            BlendMode.MULTIPLY -> GLES30.glBlendFunc(GLES30.GL_DST_COLOR, GLES30.GL_ONE_MINUS_SRC_ALPHA)
            BlendMode.SCREEN -> GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_COLOR)
            else -> GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        }
    }
}

object EffectPipeline {
    fun aggregateRgbSplit(layer: Layer): Float {
        return layer.effects
            .filter { it.isEnabled && (it.type == EffectType.RGB_SPLIT || it.type == EffectType.DIGITAL_GLITCH) }
            .maxOfOrNull { it.intensity } ?: 0f
    }

    fun aggregateVignette(layer: Layer): Float {
        return layer.effects
            .filter { it.isEnabled && it.type == EffectType.VIGNETTE }
            .maxOfOrNull { it.intensity } ?: 0f
    }
}

class GlRenderer : GLSurfaceView.Renderer {
    private var currentProject: Project? = null
    private var currentTimeMs: Long = 0L
    private val mvpMatrix = FloatArray(16)

    fun updateScene(project: Project, timeMs: Long) {
        currentProject = project
        currentTimeMs = timeMs
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.043f, 0.067f, 0.125f, 1.0f)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        Matrix.setIdentityM(mvpMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        val project = currentProject ?: return
        for (layer in project.layers) {
            if (!layer.isVisible || !layer.containsTime(currentTimeMs)) continue
            BlendModes.applyGlBlend(layer.blendMode)
        }
    }
}
