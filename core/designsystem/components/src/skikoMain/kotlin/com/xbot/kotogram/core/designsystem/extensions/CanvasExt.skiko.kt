package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.skiaCanvas
import androidx.compose.ui.graphics.skiaPaint

internal actual fun Canvas.drawPointBatch(
    points: FloatArray,
    count: Int,
    paint: Paint,
) {
    // Hack: Skia's canvas takes no count, so the unused end of the array goes off screen.
    if (count < points.size) points.fill(OFF_SCREEN, count, points.size)
    skiaCanvas.drawPoints(points, paint.skiaPaint)
}

private const val OFF_SCREEN = -1e6f
