package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.TextLayoutInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextDecoration
import com.xbot.kotogram.core.designsystem.component.platform.LineRightCountsEllipsis
import kotlin.math.ceil

internal fun TextLayoutResult.glyphBounds(): Rect {
    if (size.width <= 0 || size.height <= 0) return Rect.Zero
    return InkCache[layoutInput] ?: (outlineBounds() ?: rasterInkBounds()).also { InkCache[layoutInput] = it }
}

internal expect fun TextLayoutResult.glyphPathBounds(): Rect?

internal fun TextLayoutResult.outlineBounds(): Rect? =
    if (isPlainLine()) glyphPathBounds()?.translate(getLineLeft(0), 0f) else null

private fun TextLayoutResult.isPlainLine(): Boolean = lineCount == 1 &&
    !hasVisualOverflow &&
    layoutInput.placeholders.isEmpty() &&
    layoutInput.text.spanStyles.isEmpty() &&
    layoutInput.text.paragraphStyles.isEmpty() &&
    isPlain(layoutInput.style)

private fun isPlain(style: TextStyle): Boolean = style.brush == null &&
    style.shadow == null &&
    style.textDecoration.let { it == null || it == TextDecoration.None } &&
    style.drawStyle.let { it == null || it == Fill } &&
    style.textGeometricTransform == null &&
    style.baselineShift == null &&
    style.fontFeatureSettings == null

internal fun TextLayoutResult.shownLineRight(
    line: Int,
    ellipsis: Float,
): Float {
    if (line >= lineCount) return 0f
    // Workaround: Skia leaves the ellipsis out of a cut line's right; Android's layout counts it.
    val cut = line == lineCount - 1 && hasVisualOverflow && !LineRightCountsEllipsis
    return (getLineRight(line) + if (cut) ellipsis else 0f).coerceAtMost(size.width.toFloat())
}

internal fun TextLayoutResult.rasterInkBounds(): Rect {
    // Hack: Compose has no glyph bounds, so text the platform can't outline is drawn offscreen and read back.
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
        drawText(this@rasterInkBounds, topLeft = Offset(pad.toFloat(), originY - baseline))
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

private object InkCache {
    private val entries = LinkedHashMap<TextLayoutInput, Rect>()

    operator fun get(input: TextLayoutInput): Rect? = entries.remove(input)?.also { entries[input] = it }

    operator fun set(
        input: TextLayoutInput,
        ink: Rect,
    ) {
        entries[input] = ink
        if (entries.size > INK_CACHE_SIZE) entries.remove(entries.keys.first())
    }
}

private const val ALPHA_SHIFT = 24
private const val ALPHA_MAX = 255f
private const val INK_CACHE_SIZE = 128
