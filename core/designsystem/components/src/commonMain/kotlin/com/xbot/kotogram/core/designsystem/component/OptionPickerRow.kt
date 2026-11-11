package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons

@Composable
context(style: OptionPickerStyleScope)
internal fun OptionRow(
    label: OptionLabel,
    selected: Boolean,
    query: String,
    position: ListItemPosition,
    onClick: () -> Unit,
) {
    val rowStyle = remember(style) { style.rowStyle() }
    val leading = remember(label, style) { label.leading?.let { leadingContent(it) }.orEmpty() }
    ListItem(
        headlineContent = {
            Text(
                text = remember(label, query) {
                    buildAnnotatedString {
                        if (label.leading != null) {
                            appendInlineContent(LEADING, label.leading)
                            append(' ')
                        }
                        append(highlighted(label.text, query, style.matchStyle))
                    }
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                inlineContent = leading,
            )
        },
        modifier = Modifier.fillMaxWidth(),
        supportingContent = label.supporting?.let { supporting ->
            @Composable { Text(supporting, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        },
        trailingContent = if (label.trailing != null || selected) {
            @Composable { OptionTrailing(label.trailing, selected) }
        } else {
            null
        },
        onClick = onClick,
        position = position,
        style = rowStyle,
    )
}

// Hack: UIKit draws an emoji in a line of text larger than the text and on the text's own line height; a placeholder does.
context(style: OptionPickerStyleScope)
private fun leadingContent(leading: String): Map<String, InlineTextContent> = mapOf(
    LEADING to InlineTextContent(
        Placeholder(style.leadingWidth, style.leadingHeight, PlaceholderVerticalAlign.TextCenter),
    ) {
        Text(
            text = leading,
            modifier = Modifier.wrapContentSize(unbounded = true).offset(x = style.leadingShift),
            style = style.leadingTextStyle,
        )
    },
)

@Composable
context(style: OptionPickerStyleScope)
private fun OptionTrailing(
    value: String?,
    selected: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(style.checkGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        value?.let { Text(it) }
        if (selected) Icon(KotogramIcons.Check, contentDescription = null, tint = style.checkColor)
    }
}

private fun highlighted(
    text: String,
    query: String,
    match: SpanStyle,
): AnnotatedString {
    val start = if (query.isEmpty()) -1 else text.indexOf(query, ignoreCase = true)
    if (start < 0) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text)
        addStyle(match, start, start + query.length)
    }
}

private const val LEADING = "leading"
