package com.xbot.kotogram.feature.chats.impl.extensions

internal fun String.firstGrapheme(): String {
    if (isEmpty()) return this
    var end = codePointEnd(0)
    if (codePointAt(0) in REGIONAL_INDICATORS && end < length && codePointAt(end) in REGIONAL_INDICATORS) {
        end = codePointEnd(end)
    } else {
        while (end < length && codePointAt(end).extendsGrapheme()) {
            val joiner = codePointAt(end) == ZERO_WIDTH_JOINER
            end = codePointEnd(end)
            if (joiner && end < length) end = codePointEnd(end)
        }
    }
    return substring(0, end)
}

private fun String.codePointEnd(index: Int): Int {
    val pair = this[index].isHighSurrogate() && index + 1 < length && this[index + 1].isLowSurrogate()
    return if (pair) index + 2 else index + 1
}

private fun String.codePointAt(index: Int): Int {
    if (codePointEnd(index) == index + 1) return this[index].code
    return SUPPLEMENTARY_BASE + ((this[index].code - HIGH_SURROGATE) shl SURROGATE_BITS) +
        (this[index + 1].code - LOW_SURROGATE)
}

private fun Int.extendsGrapheme(): Boolean =
    this == ZERO_WIDTH_JOINER || this in EMOJI_MODIFIERS || this in TAGS || isMark()

private fun Int.isMark(): Boolean = this < SUPPLEMENTARY_BASE &&
    toChar().category.let {
        it == CharCategory.NON_SPACING_MARK || it == CharCategory.COMBINING_SPACING_MARK ||
            it == CharCategory.ENCLOSING_MARK
    }

private const val ZERO_WIDTH_JOINER = 0x200D
private val REGIONAL_INDICATORS = 0x1F1E6..0x1F1FF
private val EMOJI_MODIFIERS = 0x1F3FB..0x1F3FF
private val TAGS = 0xE0020..0xE007F
private const val SUPPLEMENTARY_BASE = 0x10000
private const val HIGH_SURROGATE = 0xD800
private const val LOW_SURROGATE = 0xDC00
private const val SURROGATE_BITS = 10
