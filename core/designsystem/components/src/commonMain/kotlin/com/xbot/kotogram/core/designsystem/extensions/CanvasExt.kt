package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint

internal expect fun Canvas.drawPointBatch(
    points: FloatArray,
    count: Int,
    paint: Paint,
)
