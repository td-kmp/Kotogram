package com.xbot.kotogram.core.designsystem.extensions

import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.os.Build
import android.text.TextPaint
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight

internal actual fun TextLayoutResult.glyphPathBounds(): Rect? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null
    val text = layoutInput.text.text
    val outline = Path()
    outlinePaint()?.getTextPath(text, 0, text.length, 0f, 0f, outline)
    return if (outline.isEmpty) null else outline.approximate(MAX_ERROR).pointBounds()
}

private fun TextLayoutResult.outlinePaint(): TextPaint? {
    val input = layoutInput
    val style = input.style
    val typeface = input.fontFamilyResolver.resolve(
        fontFamily = style.fontFamily,
        fontWeight = style.fontWeight ?: FontWeight.Normal,
        fontStyle = style.fontStyle ?: FontStyle.Normal,
        fontSynthesis = style.fontSynthesis ?: FontSynthesis.All,
    ).value as? Typeface
    // Workaround: emoji are bitmaps without outlines, so text with symbols is left to the raster.
    if (typeface == null || !style.fontSize.isSp || input.text.text.any(::mayBeEmoji)) return null
    val fontSize = with(input.density) { style.fontSize.toPx() }
    return TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        textSize = fontSize
        letterSpacing = when {
            style.letterSpacing.isEm -> style.letterSpacing.value
            style.letterSpacing.isSp -> with(input.density) { style.letterSpacing.toPx() } / fontSize
            else -> 0f
        }
    }
}

private fun mayBeEmoji(char: Char): Boolean = char.isSurrogate() ||
    char == EMOJI_PRESENTATION ||
    char == KEYCAP ||
    char.category == CharCategory.OTHER_SYMBOL

private fun FloatArray.pointBounds(): Rect? {
    if (size < POINT) return null
    var left = Float.POSITIVE_INFINITY
    var top = Float.POSITIVE_INFINITY
    var right = Float.NEGATIVE_INFINITY
    var bottom = Float.NEGATIVE_INFINITY
    for (at in 0 until size step POINT) {
        val x = this[at + 1]
        val y = this[at + 2]
        left = minOf(left, x)
        top = minOf(top, y)
        right = maxOf(right, x)
        bottom = maxOf(bottom, y)
    }
    return Rect(left, top, right, bottom)
}

private const val POINT = 3
private const val MAX_ERROR = 0.1f
private const val EMOJI_PRESENTATION = '️'
private const val KEYCAP = '⃣'
