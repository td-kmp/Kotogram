package com.xbot.kotogram.core.designsystem.component.platform

import android.os.Build
import android.view.AttachedSurfaceControl
import android.view.SurfaceControl
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.xbot.kotogram.core.designsystem.extensions.isOnlyTranslated
import com.xbot.kotogram.core.designsystem.extensions.windowClipOf
import kotlin.math.ceil
import kotlin.math.floor

internal actual fun CompositionLocalConsumerModifierNode.nativeDust(
    onFrame: (frameTimeNanos: Long) -> Unit,
): NativeDust? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    dustOverlay(currentValueOf(LocalView), currentValueOf(LocalLifecycleOwner).lifecycle, onFrame)
} else {
    null
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun dustOverlay(
    view: View,
    lifecycle: Lifecycle,
    onFrame: (frameTimeNanos: Long) -> Unit,
): NativeDust? {
    val root = view.rootSurfaceControl
    return if (root != null && view.isHardwareAccelerated && DustRenderer.supports(view.context)) {
        DustOverlay(view, root, lifecycle, onFrame)
    } else {
        null
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal class DustOverlay(
    private val view: View,
    private val root: AttachedSurfaceControl,
    private val lifecycle: Lifecycle,
    val onFrame: (frameTimeNanos: Long) -> Unit,
) : NativeDust,
    LifecycleEventObserver {
    private val matrix = Matrix()
    private var window: DustWindow? = null
    private var layer: DustLayer? = null
    private var areas: List<Rect> = emptyList()
    private var color = Color.Unspecified
    private var unit = 0f
    private var bleed = 0f
    private var position = Offset.Zero
    private var width = 0
    private var height = 0
    private var crop = android.graphics.Rect()
    private var translatedOnly = true
    private var broken = false
    private var started = false

    var shownFrame = NOT_SHOWN
        private set

    override val drawing: Boolean get() = window?.native == true

    val eligible: Boolean get() = translatedOnly && !broken

    val ready: Boolean get() = crop.isEmpty || layer?.ready == true

    init {
        lifecycle.addObserver(this)
    }

    override fun place(
        coordinates: LayoutCoordinates,
        bleed: Float,
    ) {
        val position = coordinates.positionInWindow() - Offset(bleed, bleed)
        val width = ceil(coordinates.size.width + 2 * bleed).toInt()
        val height = ceil(coordinates.size.height + 2 * bleed).toInt()
        val crop = cropOf(coordinates, position, width, height)
        translatedOnly = coordinates.isOnlyTranslated(matrix)
        val moved = position != this.position || crop != this.crop
        val resized = width != this.width || height != this.height || bleed != this.bleed
        if (moved || resized) {
            this.position = position
            this.crop = crop
            this.width = width
            this.height = height
            this.bleed = bleed
            publish()
            window?.wake()
        }
    }

    override fun show(
        areas: List<Rect>,
        color: Color,
        unit: Float,
    ) {
        if (areas !== this.areas || color != this.color || unit != this.unit) {
            this.areas = areas
            this.color = color
            this.unit = unit
            publish()
        }
        updateMembership()
        shownFrame = window?.frame ?: NOT_SHOWN
    }

    override fun release() {
        lifecycle.removeObserver(this)
        started = false
        updateMembership()
        layer?.release()
        layer = null
    }

    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event,
    ) {
        started = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        updateMembership()
    }

    fun arm() {
        if (crop.isEmpty) return
        val current = layer?.takeIf { it.width == width && it.height == height }
            ?: DustLayer(root, width, height).also {
                layer?.release()
                layer = it
            }
        current.publish(areas, bleed, color, unit)
        current.arm()
    }

    fun abandon() {
        if (!ready) broken = true
    }

    fun reveal(
        transaction: SurfaceControl.Transaction,
        offset: Offset,
        fresh: Boolean,
    ) {
        val fitting = layer?.takeIf { it.width == width && it.height == height }
        if (fitting != null && fresh && !crop.isEmpty) {
            fitting.reveal(transaction, position + offset, crop)
        } else {
            layer?.conceal(transaction)
        }
    }

    fun conceal(transaction: SurfaceControl.Transaction) {
        layer?.conceal(transaction)
    }

    private fun publish() {
        layer?.takeIf { it.width == width && it.height == height }?.publish(areas, bleed, color, unit)
    }

    private fun updateMembership() {
        val member = started && areas.isNotEmpty()
        val window = window
        if (member && window == null) {
            this.window = DustWindow.join(view, root, this)
        } else if (!member && window != null) {
            this.window = null
            layer?.let { SurfaceControl.Transaction().also(it::conceal).apply() }
            window.leave(this)
        }
    }
}

private fun cropOf(
    coordinates: LayoutCoordinates,
    position: Offset,
    width: Int,
    height: Int,
): android.graphics.Rect {
    val bounds = Rect(position, Size(width.toFloat(), height.toFloat()))
    val visible = bounds.intersect(coordinates.windowClipOf(bounds))
    return if (visible.width > 0 && visible.height > 0) {
        android.graphics.Rect(
            floor(visible.left - position.x).toInt(),
            floor(visible.top - position.y).toInt(),
            ceil(visible.right - position.x).toInt(),
            ceil(visible.bottom - position.y).toInt(),
        )
    } else {
        android.graphics.Rect()
    }
}

private const val NOT_SHOWN = -1L
