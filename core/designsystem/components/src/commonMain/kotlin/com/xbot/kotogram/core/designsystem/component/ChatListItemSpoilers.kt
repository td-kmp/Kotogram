package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString

internal typealias Spoiler = AnnotatedString.Range<String>

internal fun AnnotatedString.spoilers(): List<Spoiler> = getStringAnnotations(SPOILER_ANNOTATION, 0, length)

internal fun AnnotatedString.hiding(
    spoilers: List<Spoiler>,
    slots: Set<String>,
): AnnotatedString {
    if (spoilers.isEmpty()) return this
    val covered = getStringAnnotations(0, length)
        .filter { slot -> slot.item in slots && spoilers.any { it.start < slot.end && slot.start < it.end } }
        .sortedBy { it.start }
    return buildAnnotatedString {
        var cursor = 0
        covered.forEach { slot ->
            append(this@hiding, cursor, slot.start)
            appendInlineContent(hiddenSlot(slot.item), this@hiding.text.substring(slot.start, slot.end))
            cursor = slot.end
        }
        append(this@hiding, cursor, this@hiding.length)
        spoilers.forEach { addStyle(SpanStyle(color = Color.Transparent), it.start, it.end) }
    }
}

internal fun Map<String, InlineTextContent>.withHiddenSlots(spoilers: List<Spoiler>): Map<String, InlineTextContent> =
    if (spoilers.isEmpty()) {
        this
    } else {
        this + entries.associate { (slot, content) -> hiddenSlot(slot) to InlineTextContent(content.placeholder) {} }
    }

internal fun AnnotatedString.withoutSpoilers(spoilers: List<Spoiler>): String = buildString {
    var cursor = 0
    spoilers.sortedBy { it.start }.forEach { spoiler ->
        if (spoiler.start > cursor) append(text, cursor, spoiler.start)
        cursor = maxOf(cursor, spoiler.end)
    }
    if (cursor < text.length) append(text, cursor, text.length)
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
    val lineEnd = getLineEnd(line)
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
        val atLineEnd = index >= lineEnd
        var right = if (atLineEnd) getLineRight(line) else getHorizontalPosition(index, usePrimaryDirection = true)
        if (index == to && isLastLine && end > shown) right = minOf(right + ink.truncated, size.width.toFloat())
        areas += Rect(minOf(left, right), top, maxOf(left, right), bottom)
        word = -1
    }
}

private fun hiddenSlot(slot: String) = "$HIDDEN_SLOT$slot"

private const val HIDDEN_SLOT = "chat-list-item:hidden:"
