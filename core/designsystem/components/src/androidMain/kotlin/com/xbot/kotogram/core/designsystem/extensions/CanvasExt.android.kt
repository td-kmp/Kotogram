package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.nativePaint

internal actual fun Canvas.drawSprites(
    positions: FloatArray,
    textureCoordinates: FloatArray,
    colors: IntArray,
    indices: ShortArray,
    paint: Paint,
) = nativeCanvas.drawVertices(
    android.graphics.Canvas.VertexMode.TRIANGLES,
    positions.size,
    positions,
    0,
    textureCoordinates,
    0,
    colors,
    0,
    indices,
    0,
    indices.size,
    paint.nativePaint,
)
