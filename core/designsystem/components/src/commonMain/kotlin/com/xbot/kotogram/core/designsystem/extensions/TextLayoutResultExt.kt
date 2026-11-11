package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import kotlin.math.ceil

internal fun TextLayoutResult.glyphBounds(): Rect = if (size.width > 0 && size.height > 0) ink() else Rect.Zero

private fun TextLayoutResult.ink(): Rect {
    // Hack: Compose has no glyph bounds, so the text is drawn offscreen and its ink is read back.
    val baseline = firstBaseline
    val pad = size.height / 2
    val width = size.width + pad * 2
    val height = size.height + pad * 2
    val originY = pad + ceil(baseline).toInt()
    val bitmap = ImageBitmap(width, height)
    CanvasDrawScope().draw(
        density = layoutInput.density,
        layoutDirection = layoutInput.layoutDirection,
        canvas = Canvas(bitmap),
        size = Size(width.toFloat(), height.toFloat()),
    ) {
        drawText(this@ink, topLeft = Offset(pad.toFloat(), originY - baseline))
    }
    val pixels = IntArray(width * height)
    bitmap.readPixels(pixels)
    val columns = FloatArray(width)
    val rows = FloatArray(height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val coverage = (pixels[y * width + x] ushr ALPHA_SHIFT) / ALPHA_MAX
            if (coverage > columns[x]) columns[x] = coverage
            if (coverage > rows[y]) rows[y] = coverage
        }
    }
    val left = columns.indexOfFirst { it > 0f }
    if (left < 0) return Rect.Zero
    val right = columns.indexOfLast { it > 0f }
    val top = rows.indexOfFirst { it > 0f }
    val bottom = rows.indexOfLast { it > 0f }
    return Rect(
        left = left + 1 - columns[left] - pad,
        top = top + 1 - rows[top] - originY,
        right = right + columns[right] - pad,
        bottom = bottom + rows[bottom] - originY,
    )
}

private const val ALPHA_SHIFT = 24
private const val ALPHA_MAX = 255f
