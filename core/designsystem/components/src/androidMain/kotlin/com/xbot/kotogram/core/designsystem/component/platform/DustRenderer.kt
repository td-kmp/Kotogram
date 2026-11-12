package com.xbot.kotogram.core.designsystem.component.platform

import android.app.ActivityManager
import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.opengl.GLES30
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Choreographer
import android.view.Surface
import com.xbot.kotogram.core.designsystem.component.dustFragmentShader
import com.xbot.kotogram.core.designsystem.component.dustSeconds
import com.xbot.kotogram.core.designsystem.component.dustVertexShader
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class DustFrame(
    val areas: FloatArray,
    val shares: FloatArray,
    val areaCount: Int,
    val speckles: Int,
    val color: FloatArray,
    val unit: Float,
    val width: Int,
    val height: Int,
    val originNanos: Long,
)

internal class DustTarget(
    val surface: Surface,
    val onReleased: () -> Unit,
) {
    @Volatile var frame: DustFrame? = null

    @Volatile var rendering = false

    @Volatile var swaps = 0

    var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
}

internal object DustRenderer {
    private val handler by lazy { Handler(HandlerThread(THREAD).apply { start() }.looper) }
    private val choreographer by lazy { Choreographer.getInstance() }
    private val targets = ArrayList<DustTarget>()
    private val frameCallback = Choreographer.FrameCallback(::render)
    private var gl: DustGl? = null
    private var scheduled = false

    @Volatile private var ready: Boolean? = null

    fun supports(context: Context): Boolean = ready ?: initialize(context)

    fun add(target: DustTarget) {
        handler.post {
            target.eglSurface = gl?.createSurface(target.surface) ?: EGL14.EGL_NO_SURFACE
            targets += target
            schedule()
        }
    }

    fun wake() {
        handler.post(::schedule)
    }

    fun remove(target: DustTarget) {
        handler.post {
            targets -= target
            gl?.destroySurface(target.eglSurface)
            target.eglSurface = EGL14.EGL_NO_SURFACE
            target.surface.release()
            target.onReleased()
        }
    }

    private fun initialize(context: Context): Boolean {
        val info = context.getSystemService(ActivityManager::class.java)?.deviceConfigurationInfo
        if ((info?.reqGlEsVersion ?: 0) >= GLES_3_0) {
            val done = CountDownLatch(1)
            handler.post {
                gl = DustGl.create()
                ready = gl != null
                done.countDown()
            }
            done.await(INIT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        }
        return (ready == true).also { ready = it }
    }

    private fun schedule() {
        if (!scheduled && targets.any { it.rendering && it.frame != null }) {
            scheduled = true
            choreographer.postFrameCallback(frameCallback)
        }
    }

    private fun render(frameTimeNanos: Long) {
        scheduled = false
        gl?.let { gl ->
            for (target in targets) {
                val frame = target.frame
                if (target.rendering && frame != null) {
                    val seconds = dustSeconds(frameTimeNanos, frame.originNanos).toFloat()
                    if (gl.draw(target.eglSurface, frame, seconds)) target.swaps++
                }
            }
        }
        schedule()
    }
}

private class DustGl(
    private val display: EGLDisplay,
    private val config: EGLConfig,
    private val context: EGLContext,
    private val idle: EGLSurface,
    private val program: Int,
) {
    private val time = GLES30.glGetUniformLocation(program, "uTime")
    private val unit = GLES30.glGetUniformLocation(program, "uUnit")
    private val size = GLES30.glGetUniformLocation(program, "uSize")
    private val areaCount = GLES30.glGetUniformLocation(program, "uAreaCount")
    private val areas = GLES30.glGetUniformLocation(program, "uAreas")
    private val shares = GLES30.glGetUniformLocation(program, "uShares")
    private val color = GLES30.glGetUniformLocation(program, "uColor")

    fun createSurface(surface: Surface): EGLSurface {
        val created = EGL14.eglCreateWindowSurface(display, config, surface, intArrayOf(EGL14.EGL_NONE), 0)
        if (created != EGL14.EGL_NO_SURFACE && EGL14.eglMakeCurrent(display, created, created, context)) {
            EGL14.eglSwapInterval(display, 0)
        }
        return created
    }

    fun destroySurface(surface: EGLSurface) {
        if (surface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglMakeCurrent(display, idle, idle, context)
            EGL14.eglDestroySurface(display, surface)
        }
    }

    fun draw(
        surface: EGLSurface,
        frame: DustFrame,
        seconds: Float,
    ): Boolean {
        if (surface == EGL14.EGL_NO_SURFACE || !EGL14.eglMakeCurrent(display, surface, surface, context)) return false
        GLES30.glViewport(0, 0, frame.width, frame.height)
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(program)
        GLES30.glUniform1f(time, seconds)
        GLES30.glUniform1f(unit, frame.unit)
        GLES30.glUniform2f(size, frame.width.toFloat(), frame.height.toFloat())
        GLES30.glUniform1i(areaCount, frame.areaCount)
        GLES30.glUniform4fv(areas, frame.areaCount, frame.areas, 0)
        GLES30.glUniform1fv(shares, frame.areaCount, frame.shares, 0)
        GLES30.glUniform4fv(color, 1, frame.color, 0)
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, frame.speckles)
        return EGL14.eglSwapBuffers(display, surface)
    }

    companion object {
        fun create(): DustGl? = initializedDisplay()?.let { display ->
            chooseConfig(display)?.let { config -> withContext(display, config) }
        }

        private fun initializedDisplay(): EGLDisplay? {
            val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val version = IntArray(2)
            return display.takeIf { it != EGL14.EGL_NO_DISPLAY && EGL14.eglInitialize(it, version, 0, version, 1) }
        }

        private fun chooseConfig(display: EGLDisplay): EGLConfig? {
            val configs = arrayOfNulls<EGLConfig>(1)
            val found = IntArray(1)
            EGL14.eglChooseConfig(display, ConfigAttributes, 0, configs, 0, 1, found, 0)
            return configs[0].takeIf { found[0] > 0 }
        }

        private fun withContext(
            display: EGLDisplay,
            config: EGLConfig,
        ): DustGl? {
            val context = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, ContextAttributes, 0)
            val idle = EGL14.eglCreatePbufferSurface(display, config, IdleAttributes, 0)
            val current = context != EGL14.EGL_NO_CONTEXT &&
                idle != EGL14.EGL_NO_SURFACE &&
                EGL14.eglMakeCurrent(display, idle, idle, context)
            val program = if (current) link(dustVertexShader(), dustFragmentShader()) else 0
            if (program != 0) {
                GLES30.glEnable(GLES30.GL_BLEND)
                GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA)
            }
            return if (program != 0) DustGl(display, config, context, idle, program) else null
        }

        private fun link(
            vertex: String,
            fragment: String,
        ): Int {
            val program = GLES30.glCreateProgram()
            val shaders = listOf(compile(GLES30.GL_VERTEX_SHADER, vertex), compile(GLES30.GL_FRAGMENT_SHADER, fragment))
            shaders.forEach { GLES30.glAttachShader(program, it) }
            GLES30.glLinkProgram(program)
            shaders.forEach(GLES30::glDeleteShader)
            val status = IntArray(1)
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, status, 0)
            if (status[0] == 0) {
                Log.e(THREAD, GLES30.glGetProgramInfoLog(program))
                GLES30.glDeleteProgram(program)
            }
            return if (status[0] != 0) program else 0
        }

        private fun compile(
            type: Int,
            source: String,
        ): Int {
            val shader = GLES30.glCreateShader(type)
            GLES30.glShaderSource(shader, source)
            GLES30.glCompileShader(shader)
            val status = IntArray(1)
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0)
            if (status[0] == 0) Log.e(THREAD, GLES30.glGetShaderInfoLog(shader))
            return shader
        }
    }
}

private val ConfigAttributes = intArrayOf(
    EGL14.EGL_RED_SIZE, CHANNEL_BITS,
    EGL14.EGL_GREEN_SIZE, CHANNEL_BITS,
    EGL14.EGL_BLUE_SIZE, CHANNEL_BITS,
    EGL14.EGL_ALPHA_SIZE, CHANNEL_BITS,
    EGL14.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR,
    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT or EGL14.EGL_PBUFFER_BIT,
    EGL14.EGL_NONE,
)
private val ContextAttributes = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, ES_VERSION, EGL14.EGL_NONE)
private val IdleAttributes = intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE)

private const val THREAD = "KotogramDust"
private const val GLES_3_0 = 0x30000
private const val ES_VERSION = 3
private const val CHANNEL_BITS = 8
private const val INIT_TIMEOUT_MS = 500L
