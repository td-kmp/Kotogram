package com.xbot.kotogram.core.designsystem.component

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import io.github.alexzhirkevich.qrose.Neighbors
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrPixelShape

private const val MODULE_CORNER = 1f / 3f
private const val INNER_CORNER = 1f / 4f
private const val FRAME_MODULES = 7f
private const val BALL_MODULES = 3f

private const val RING = 44f / 7f
private const val RING_CORNER = 12f / 44f
private const val DOT = 23f / 7f
private const val DOT_CORNER = 6f / 23f

private const val UP = 270f
private const val RIGHT = 0f
private const val DOWN = 90f
private const val LEFT = 180f

internal object TelegramPixelShape : QrPixelShape {
    override fun Path.path(
        size: Float,
        neighbors: Neighbors,
    ): Path = apply {
        addModule(size, neighbors)
        addInnerCorners(size, neighbors)
    }
}

private fun Path.addModule(
    size: Float,
    neighbors: Neighbors,
) {
    val radius = CornerRadius(size * MODULE_CORNER)
    addRoundRect(
        RoundRect(
            rect = Rect(0f, 0f, size, size),
            topLeft = if (neighbors.top || neighbors.left) CornerRadius.Zero else radius,
            topRight = if (neighbors.top || neighbors.right) CornerRadius.Zero else radius,
            bottomRight = if (neighbors.bottom || neighbors.right) CornerRadius.Zero else radius,
            bottomLeft = if (neighbors.bottom || neighbors.left) CornerRadius.Zero else radius,
        ),
    )
}

private fun Path.addInnerCorners(
    size: Float,
    neighbors: Neighbors,
) = with(neighbors) {
    val radius = size * INNER_CORNER
    if (right && bottom && !bottomRight) addInnerCorner(Offset(size, size), Offset(1f, 1f), UP, radius)
    if (left && bottom && !bottomLeft) addInnerCorner(Offset(0f, size), Offset(-1f, 1f), RIGHT, radius)
    if (left && top && !topLeft) addInnerCorner(Offset.Zero, Offset(-1f, -1f), DOWN, radius)
    if (right && top && !topRight) addInnerCorner(Offset(size, 0f), Offset(1f, -1f), LEFT, radius)
}

internal object TelegramFrameShape : QrFrameShape {
    override fun Path.path(
        size: Float,
        neighbors: Neighbors,
    ): Path = apply {
        val module = size / FRAME_MODULES
        val ring = module * RING
        val corner = ring * RING_CORNER
        val outer = ring + module
        addRoundRect(RoundRect(Rect(0f, 0f, outer, outer), CornerRadius(corner + module / 2)))
        addRoundRect(RoundRect(Rect(module, module, outer - module, outer - module), CornerRadius(corner - module / 2)))
    }
}

internal object TelegramBallShape : QrBallShape {
    override fun Path.path(
        size: Float,
        neighbors: Neighbors,
    ): Path = apply {
        val dot = size / BALL_MODULES * DOT
        addRoundRect(RoundRect(Rect(0f, 0f, dot, dot), CornerRadius(dot * DOT_CORNER)))
    }
}

private fun Path.addInnerCorner(
    corner: Offset,
    inwards: Offset,
    startAngle: Float,
    radius: Float,
) {
    moveTo(corner.x, corner.y)
    arcTo(Rect(corner + inwards * radius, radius), startAngle, sweepAngleDegrees = -90f, forceMoveTo = false)
    close()
}
