package com.xbot.kotogram.core.resources.extensions

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.xbot.kotogram.core.resources.FormattedTextStyles
import io.xbot.tdlib.api.FormattedText
import io.xbot.tdlib.api.TextEntity
import io.xbot.tdlib.api.TextEntityType
import io.xbot.tdlib.api.TextEntityTypeBlockQuote
import io.xbot.tdlib.api.TextEntityTypeBold
import io.xbot.tdlib.api.TextEntityTypeBotCommand
import io.xbot.tdlib.api.TextEntityTypeCashtag
import io.xbot.tdlib.api.TextEntityTypeCode
import io.xbot.tdlib.api.TextEntityTypeEmailAddress
import io.xbot.tdlib.api.TextEntityTypeExpandableBlockQuote
import io.xbot.tdlib.api.TextEntityTypeHashtag
import io.xbot.tdlib.api.TextEntityTypeItalic
import io.xbot.tdlib.api.TextEntityTypeMention
import io.xbot.tdlib.api.TextEntityTypeMentionName
import io.xbot.tdlib.api.TextEntityTypePhoneNumber
import io.xbot.tdlib.api.TextEntityTypePre
import io.xbot.tdlib.api.TextEntityTypePreCode
import io.xbot.tdlib.api.TextEntityTypeSpoiler
import io.xbot.tdlib.api.TextEntityTypeStrikethrough
import io.xbot.tdlib.api.TextEntityTypeTextUrl
import io.xbot.tdlib.api.TextEntityTypeUnderline
import io.xbot.tdlib.api.TextEntityTypeUrl

fun FormattedText.toAnnotatedString(
    styles: FormattedTextStyles = FormattedTextStyles(),
    onEntityClick: ((TextEntity) -> Unit)? = null,
): AnnotatedString = buildAnnotatedString {
    append(text)
    entities.forEach { entity -> addEntity(entity, text, styles, onEntityClick) }
}

private fun AnnotatedString.Builder.addEntity(
    entity: TextEntity,
    text: String,
    styles: FormattedTextStyles,
    onEntityClick: ((TextEntity) -> Unit)?,
) {
    val start = entity.offset.coerceIn(0, text.length)
    val end = (entity.offset + entity.length).coerceIn(start, text.length)
    if (start == end) return
    val content = text.substring(start, end)
    val style = inlineStyle(entity.type, styles)
    val url = url(entity.type, content)
    when {
        style != null -> addStyle(style, start, end)

        url != null -> addLink(LinkAnnotation.Url(url, styles.link), start, end)

        !isTappable(entity.type) -> Unit

        onEntityClick != null -> {
            addLink(LinkAnnotation.Clickable(content, styles.link) { onEntityClick(entity) }, start, end)
        }

        else -> addStyle(styles.link?.style ?: SpanStyle(), start, end)
    }
}

private fun inlineStyle(
    type: TextEntityType,
    styles: FormattedTextStyles,
): SpanStyle? = when (type) {
    TextEntityTypeBold -> styles.bold
    TextEntityTypeItalic -> styles.italic
    TextEntityTypeUnderline -> styles.underline
    TextEntityTypeStrikethrough -> styles.strikethrough
    TextEntityTypeSpoiler -> styles.spoiler
    TextEntityTypeCode, TextEntityTypePre, is TextEntityTypePreCode -> styles.code
    TextEntityTypeBlockQuote, TextEntityTypeExpandableBlockQuote -> styles.quote
    else -> null
}

private fun url(
    type: TextEntityType,
    content: String,
): String? = when (type) {
    is TextEntityTypeTextUrl -> type.url
    TextEntityTypeUrl -> content.withScheme()
    TextEntityTypeEmailAddress -> "mailto:$content"
    TextEntityTypePhoneNumber -> "tel:${content.dialable()}"
    else -> null
}

private fun isTappable(type: TextEntityType): Boolean = type == TextEntityTypeMention ||
    type == TextEntityTypeHashtag ||
    type == TextEntityTypeCashtag ||
    type == TextEntityTypeBotCommand ||
    type is TextEntityTypeMentionName

private fun String.withScheme(): String = if ("://" in this) this else "https://$this"

private fun String.dialable(): String = filter { it.isDigit() || it == '+' }
