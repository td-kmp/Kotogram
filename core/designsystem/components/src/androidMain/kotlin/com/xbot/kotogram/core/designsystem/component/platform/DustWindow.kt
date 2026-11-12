package com.xbot.kotogram.core.designsystem.component.platform

import android.os.Build
import android.view.AttachedSurfaceControl
import android.view.Choreographer
import android.view.SurfaceControl
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import com.xbot.kotogram.core.designsystem.extensions.surfaceOffset

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal class DustWindow private constructor(
    private val view: View,
    private val root: AttachedSurfaceControl,
) : ViewTreeObserver.OnPreDrawListener,
    Choreographer.FrameCallback {
    private val overlays = ArrayList<DustOverlay>()
    private val choreographer = Choreographer.getInstance()
    private val observer = view.viewTreeObserver.also { it.addOnPreDrawListener(this) }
    private var mode = Mode.Compose
    private var scheduled = false
    private var committing = false
    private var woken = false
    private var steps = 0
    private var traversals = 0

    var frame = 0L
        private set

    val native: Boolean get() = mode == Mode.Native

    fun wake() {
        if (mode != Mode.Compose) woken = true
        schedule()
    }

    fun leave(overlay: DustOverlay) {
        overlays -= overlay
        if (overlays.isEmpty()) {
            if (observer.isAlive) observer.removeOnPreDrawListener(this)
            choreographer.removeFrameCallback(this)
            windows.remove(root)
        }
    }

    override fun onPreDraw(): Boolean {
        traversals++
        if (committing) {
            committing = false
        } else if (mode == Mode.Native) {
            compose(System.nanoTime())
        }
        return true
    }

    override fun doFrame(frameTimeNanos: Long) {
        scheduled = false
        frame++
        val quiet = traversals == 0 && !woken
        val stirred = woken
        traversals = 0
        woken = false
        when (mode) {
            Mode.Compose -> step(frameTimeNanos)

            Mode.Probe -> if (quiet && overlays.all { it.eligible }) arm(frameTimeNanos) else step(frameTimeNanos)

            Mode.Arm -> when {
                stirred -> compose(frameTimeNanos)

                overlays.all { it.ready } -> commit(frameTimeNanos)

                ++steps > ARM_FRAMES -> {
                    overlays.forEach(DustOverlay::abandon)
                    compose(frameTimeNanos)
                }

                else -> invalidate(frameTimeNanos)
            }

            Mode.Native -> if (stirred) compose(frameTimeNanos) else committing = false
        }
    }

    private fun step(frameTimeNanos: Long) {
        mode = if (++steps % PROBE_FRAMES == 0) Mode.Probe else Mode.Compose
        if (mode == Mode.Compose) invalidate(frameTimeNanos) else schedule()
    }

    private fun arm(frameTimeNanos: Long) {
        overlays.forEach(DustOverlay::arm)
        mode = Mode.Arm
        steps = 0
        invalidate(frameTimeNanos)
    }

    private fun commit(frameTimeNanos: Long) {
        val transaction = SurfaceControl.Transaction()
        val offset = view.surfaceOffset()
        overlays.forEach { it.reveal(transaction, offset, fresh = it.shownFrame == frame - 1) }
        root.applyTransactionOnDraw(transaction)
        mode = Mode.Native
        committing = true
        invalidate(frameTimeNanos)
    }

    private fun compose(frameTimeNanos: Long) {
        val transaction = SurfaceControl.Transaction()
        overlays.forEach { it.conceal(transaction) }
        root.applyTransactionOnDraw(transaction)
        mode = Mode.Compose
        steps = 0
        invalidate(frameTimeNanos)
    }

    private fun invalidate(frameTimeNanos: Long) {
        for (index in overlays.indices) overlays[index].onFrame(frameTimeNanos)
        schedule()
    }

    private fun schedule() {
        if (!scheduled) {
            scheduled = true
            choreographer.postFrameCallback(this)
        }
    }

    private enum class Mode { Compose, Probe, Arm, Native }

    companion object {
        private val windows = HashMap<AttachedSurfaceControl, DustWindow>()

        fun join(
            view: View,
            root: AttachedSurfaceControl,
            overlay: DustOverlay,
        ): DustWindow = windows.getOrPut(root) { DustWindow(view, root) }.also {
            it.overlays += overlay
            it.wake()
        }
    }
}

private const val PROBE_FRAMES = 8
private const val ARM_FRAMES = 8
