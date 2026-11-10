package com.xbot.kotogram.core.resources.extensions

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

private val Token = Regex("""\*\*|\[(?=[^\]]*]\([^)]*\))|]\(([^)]*)\)|%(?:(\d+)\$)?[@sd]|%%""")
private val Placeholder = Regex("""%(?:(\d+)\$)?[@sd]|%%""")
private val Bold = SpanStyle(fontWeight = FontWeight.Bold)

internal fun String.toTelegramText(
    args: Array<out Any>,
    link: ((url: String) -> LinkAnnotation)? = null,
): AnnotatedString = buildAnnotatedString {
    val template = this@toTelegramText
    val arguments = Arguments(args)
    var boldStart = -1
    var linkStart = -1
    var last = 0
    Token.findAll(template).forEach { match ->
        append(template, last, match.range.first)
        last = match.range.last + 1
        val token = match.value
        when {
            token == "**" -> if (boldStart < 0) {
                boldStart = length
            } else {
                addStyle(Bold, boldStart, length)
                boldStart = -1
            }

            token == "[" -> linkStart = length

            token.startsWith("](") -> if (linkStart < 0) {
                append(token)
            } else {
                link?.invoke(arguments.fill(match.groupValues[1]))?.let { addAnyLink(it, linkStart, length) }
                linkStart = -1
            }

            token == "%%" -> append('%')

            else -> append(arguments.at(match.groupValues[2]) ?: token)
        }
    }
    append(template, last, template.length)
}

internal fun String.formatTelegram(args: Array<out Any>): String = toTelegramText(args).text

private fun AnnotatedString.Builder.addAnyLink(
    link: LinkAnnotation,
    start: Int,
    end: Int,
) {
    when (link) {
        is LinkAnnotation.Url -> addLink(link, start, end)
        is LinkAnnotation.Clickable -> addLink(link, start, end)
    }
}

private class Arguments(
    private val values: Array<out Any>,
) {
    private var next = 0

    fun at(position: String): String? {
        val index = position.toIntOrNull()?.minus(1) ?: next++
        return values.getOrNull(index)?.toString()
    }

    fun fill(text: String): String = Placeholder.replace(text) {
        if (it.value == "%%") "%" else at(it.groupValues[1]) ?: it.value
    }
}
