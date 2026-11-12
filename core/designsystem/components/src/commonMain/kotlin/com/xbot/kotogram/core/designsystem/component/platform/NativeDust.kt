package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode

internal interface NativeDust {
    val drawing: Boolean

    fun place(
        coordinates: LayoutCoordinates,
        bleed: Float,
    )

    fun show(
        areas: List<Rect>,
        color: Color,
        unit: Float,
    )

    fun release()
}

internal expect fun CompositionLocalConsumerModifierNode.nativeDust(
    onFrame: (frameTimeNanos: Long) -> Unit,
): NativeDust?
