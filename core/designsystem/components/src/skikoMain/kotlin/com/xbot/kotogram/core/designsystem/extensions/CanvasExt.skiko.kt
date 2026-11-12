package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.skiaCanvas
import androidx.compose.ui.graphics.skiaPaint
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.VertexMode

internal actual fun Canvas.drawSprites(
    positions: FloatArray,
    textureCoordinates: FloatArray,
    colors: IntArray,
    indices: ShortArray,
    paint: Paint,
) {
    skiaCanvas.drawVertices(
        VertexMode.TRIANGLES,
        positions,
        colors,
        textureCoordinates,
        indices,
        BlendMode.MODULATE,
        paint.skiaPaint,
    )
}
