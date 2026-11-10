package com.xbot.kotogram.core.resources

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

@Immutable
data class FormattedTextStyles(
    val bold: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold),
    val italic: SpanStyle = SpanStyle(fontStyle = FontStyle.Italic),
    val underline: SpanStyle = SpanStyle(textDecoration = TextDecoration.Underline),
    val strikethrough: SpanStyle = SpanStyle(textDecoration = TextDecoration.LineThrough),
    val spoiler: SpanStyle = SpanStyle(),
    val code: SpanStyle = SpanStyle(fontFamily = FontFamily.Monospace),
    val quote: SpanStyle = SpanStyle(fontStyle = FontStyle.Italic),
    val link: TextLinkStyles? = null,
)
