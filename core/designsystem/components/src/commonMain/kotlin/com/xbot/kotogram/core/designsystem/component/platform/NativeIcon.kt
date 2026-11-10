package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

@Composable
internal fun rememberIconBitmap(
    icon: ImageVector,
    size: Dp,
    color: Color,
): ImageBitmap {
    val painter = rememberVectorPainter(icon)
    val density = LocalDensity.current
    return remember(painter, size, color, density) {
        val side = with(density) { size.roundToPx() }
        val canvasSize = Size(side.toFloat(), side.toFloat())
        ImageBitmap(side, side).also { bitmap ->
            CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(bitmap), canvasSize) {
                with(painter) { draw(canvasSize, colorFilter = ColorFilter.tint(color)) }
            }
        }
    }
}
