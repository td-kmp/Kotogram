package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.nativePaint

internal actual fun Canvas.drawPointBatch(
    points: FloatArray,
    count: Int,
    paint: Paint,
) = nativeCanvas.drawPoints(points, 0, count, paint.nativePaint)
