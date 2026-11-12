package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.unit.IntRect

internal interface FrameLayer {
    fun DrawScope.draw(
        bounds: IntRect,
        picture: (Canvas) -> Unit,
    )

    fun update(picture: (Canvas) -> Unit): Boolean

    fun release()
}

internal expect fun CompositionLocalConsumerModifierNode.frameLayer(): FrameLayer

internal object SceneFrameLayer : FrameLayer {
    override fun DrawScope.draw(
        bounds: IntRect,
        picture: (Canvas) -> Unit,
    ) = clipRect(bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat(), bounds.bottom.toFloat()) {
        drawIntoCanvas(picture)
    }

    override fun update(picture: (Canvas) -> Unit): Boolean = false

    override fun release() = Unit
}
