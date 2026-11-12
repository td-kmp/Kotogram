package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint

internal expect fun Canvas.drawSprites(
    positions: FloatArray,
    textureCoordinates: FloatArray,
    colors: IntArray,
    indices: ShortArray,
    paint: Paint,
)
