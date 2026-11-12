package com.xbot.kotogram.core.designsystem.component.platform

import android.graphics.RenderNode
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.CanvasHolder
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntRect

internal actual fun CompositionLocalConsumerModifierNode.frameLayer(): FrameLayer {
    val host = currentValueOf(LocalView)
    return if (host is ViewGroup) RenderNodeFrameLayer(host) else SceneFrameLayer
}

private class RenderNodeFrameLayer(
    private val host: ViewGroup,
) : FrameLayer {
    // Hack: HWUI keeps damage inside a node that clips to its bounds; Compose's layers clip to an outline instead.
    private val node = RenderNode("FrameLayer")

    // Hack: a view of the overlay, invalidated, asks for a frame without AndroidComposeView recording itself again.
    private val tick = View(host.context).apply { layout(0, 0, 1, 1) }
    private val holder = CanvasHolder()
    private var left = 0
    private var top = 0
    private var onScreen = false

    init {
        host.overlay.add(tick)
    }

    override fun DrawScope.draw(
        bounds: IntRect,
        picture: (Canvas) -> Unit,
    ) {
        val canvas = drawContext.canvas.nativeCanvas
        onScreen = canvas.isHardwareAccelerated
        if (onScreen) {
            left = bounds.left
            top = bounds.top
            node.setPosition(bounds.left, bounds.top, bounds.right, bounds.bottom)
            record(picture)
            canvas.drawRenderNode(node)
        } else {
            with(SceneFrameLayer) { draw(bounds, picture) }
        }
    }

    override fun update(picture: (Canvas) -> Unit): Boolean {
        val shown = onScreen && host.isAttachedToWindow && host.isHardwareAccelerated
        if (shown) {
            record(picture)
            tick.invalidate()
        }
        return shown
    }

    override fun release() {
        host.overlay.remove(tick)
        node.discardDisplayList()
    }

    private fun record(picture: (Canvas) -> Unit) {
        val canvas = node.beginRecording()
        try {
            canvas.translate(-left.toFloat(), -top.toFloat())
            holder.drawInto(canvas) { picture(this) }
        } finally {
            node.endRecording()
        }
    }
}
