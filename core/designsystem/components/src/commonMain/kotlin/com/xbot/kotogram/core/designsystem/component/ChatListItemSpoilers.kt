package com.xbot.kotogram.core.designsystem.component

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString

internal typealias Spoiler = AnnotatedString.Range<String>

internal fun AnnotatedString.spoilers(): List<Spoiler> = getStringAnnotations(SPOILER_ANNOTATION, 0, length)

internal fun AnnotatedString.hiding(spoilers: List<Spoiler>): AnnotatedString {
    if (spoilers.isEmpty()) return this
    return buildAnnotatedString {
        append(this@hiding)
        spoilers.forEach { addStyle(SpanStyle(color = Color.Transparent), it.start, it.end) }
    }
}

internal fun TextLayoutResult.shownRight(ellipsis: Float): Float {
    if (lineCount == 0) return 0f
    // Workaround: Skia leaves the ellipsis out of the line's right.
    val right = getLineRight(0) + if (hasVisualOverflow) ellipsis else 0f
    return right.coerceAtMost(size.width.toFloat())
}

internal class SpoilerAreas {
    var rects: List<Rect> = emptyList()
        private set

    fun update(
        layout: TextLayoutResult,
        spoilers: List<Spoiler>,
        offset: Int,
        ink: SpoilerInk,
    ) {
        val areas = ArrayList<Rect>()
        val text = layout.layoutInput.text.text
        val lastLine = layout.lineCount - 1
        for (spoiler in spoilers) {
            val start = (spoiler.start + offset).coerceIn(0, text.length)
            val end = (spoiler.end + offset).coerceIn(start, text.length)
            if (start == end || lastLine < 0) continue
            for (line in layout.getLineForOffset(start)..minOf(layout.getLineForOffset(end - 1), lastLine)) {
                layout.addWords(areas, text, line, start, end, line == lastLine, ink)
            }
        }
        if (areas != rects) rects = areas
    }
}

internal class SpoilerInk(
    val height: Float,
    val inset: Float,
    val truncated: Float,
)

@Suppress("LongParameterList")
private fun TextLayoutResult.addWords(
    areas: MutableList<Rect>,
    text: String,
    line: Int,
    start: Int,
    end: Int,
    isLastLine: Boolean,
    ink: SpoilerInk,
) {
    val shown = getLineEnd(line, visibleEnd = true)
    val from = maxOf(start, getLineStart(line))
    val to = minOf(end, shown)
    val top = getLineTop(line) + ink.inset
    val bottom = getLineTop(line) + ink.height - ink.inset
    var word = -1
    for (index in from..to) {
        val boundary = index == to || text[index].isWhitespace()
        if (!boundary && word < 0) word = index
        if (!boundary || word < 0) continue
        val left = getHorizontalPosition(word, usePrimaryDirection = true)
        var right = if (index >=
            getLineEnd(line)
        ) getLineRight(line) else getHorizontalPosition(index, usePrimaryDirection = true)
        if (index == to && isLastLine && end > shown) right = minOf(right + ink.truncated, size.width.toFloat())
        areas += Rect(minOf(left, right), top, maxOf(left, right), bottom)
        word = -1
    }
}
