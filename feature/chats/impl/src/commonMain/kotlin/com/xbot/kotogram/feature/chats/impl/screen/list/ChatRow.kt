package com.xbot.kotogram.feature.chats.impl.screen.list

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.xbot.kotogram.core.designsystem.component.Avatar
import com.xbot.kotogram.core.designsystem.component.NetworkImage
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.chatlist_draft
import com.xbot.kotogram.core.resources.peerinfo_action_message
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.chats.impl.component.CustomEmoji
import com.xbot.kotogram.feature.chats.impl.component.customEmojiContent
import com.xbot.kotogram.feature.chats.impl.component.toPreviewString
import com.xbot.kotogram.feature.chats.impl.extensions.label
import io.xbot.tdlib.api.Sticker
import kotlin.math.floor

// The frames of Telegram for iOS's ChatListItem and Telegram for macOS's ChatListRowView, from the style's values.
@Composable
context(style: ChatListScreenStyleScope)
internal fun ChatRow(
    row: ChatListRow,
    customEmoji: Map<Long, Sticker>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleStyle = textNodeStyle(style.titleTextStyle, style.titleLineSpacing)
    val dateStyle = textNodeStyle(style.dateTextStyle, style.titleLineSpacing)
    val authorStyle = textNodeStyle(style.authorTextStyle, style.titleLineSpacing)
    val draftStyle = textNodeStyle(style.draftTextStyle, style.titleLineSpacing)
    val previewStyle = textNodeStyle(style.previewTextStyle, style.previewLineSpacing)
    val measurer = rememberTextMeasurer()
    val titleLine = remember(titleStyle) { measurer.measure(" ", titleStyle).size.height }
    val dateLine = remember(dateStyle) { measurer.measure(" ", dateStyle).size.height }
    val author = when {
        row.preview is ChatPreview.Draft -> stringResource(Res.string.chatlist_draft)
        else -> row.senderName
    }
    Layout(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        content = {
            Avatar(
                photo = row.avatar.photo,
                letters = row.avatar.letters,
                colorIndex = row.avatar.colorIndex,
                thumbnail = row.avatar.thumbnail,
                modifier = Modifier.layoutId(Slot.Avatar).size(style.avatarSize)
            )
            TitleLine(row, customEmoji, titleStyle, Modifier.layoutId(Slot.Title))
            DateLine(row, dateStyle, Modifier.layoutId(Slot.Date))
            if (author != null) {
                Text(
                    text = author,
                    style = if (row.preview is ChatPreview.Draft) draftStyle else authorStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.layoutId(Slot.Author)
                )
            }
            Preview(
                row = row,
                customEmoji = customEmoji,
                textStyle = previewStyle,
                maxLines = if (author != null) 1 else 2,
                modifier = Modifier.layoutId(Slot.Text)
            )
            Badges(row, Modifier.layoutId(Slot.Badges))
            Box(Modifier.layoutId(Slot.Separator).background(style.separatorColor))
        }
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val contentLeft = style.contentStart.roundToPx()
        val contentRight = width - style.contentEnd.roundToPx()
        val contentWidth = (contentRight - contentLeft).coerceAtLeast(0)
        fun measurable(slot: Slot) = measurables.firstOrNull { it.layoutId == slot }
        val loose = Constraints()
        val date = measurable(Slot.Date)!!.measure(loose)
        val badges = measurable(Slot.Badges)!!.measure(loose)
        val title = measurable(Slot.Title)!!.measure(
            Constraints(maxWidth = (contentWidth - date.width - style.titleDateGap.roundToPx()).coerceAtLeast(0))
        )
        val textReserve = if (badges.width > 0) badges.width + style.badgeTextReserve.roundToPx() else 0
        val textConstraints = Constraints(maxWidth = (contentWidth - textReserve).coerceAtLeast(0))
        val authorPlaceable = measurable(Slot.Author)?.measure(textConstraints)
        val text = measurable(Slot.Text)!!.measure(textConstraints)
        val avatar = measurable(Slot.Avatar)!!.measure(loose)
        val height = if (style.rowHeight.isSpecified) {
            style.rowHeight.roundToPx()
        } else {
            titleLine * IOS_ROW_LINES - IOS_ROW_INSET.roundToPx()
        }
        val hairline = 1
        val separator = measurable(Slot.Separator)!!.measure(
            Constraints.fixed(
                (width - style.separatorStart.roundToPx() - style.separatorEnd.roundToPx()).coerceAtLeast(0),
                hairline
            )
        )
        layout(width, height) {
            val titleTop = style.titleTop.roundToPx()
            avatar.place(style.avatarStart.roundToPx(), (height - avatar.height) / 2)
            title.place(contentLeft, titleTop + (titleLine - title.height) / 2)
            date.place(contentRight - date.width, style.dateTop.roundToPx() + (dateLine - date.height) / 2)
            var textTop = titleTop + titleLine + style.textGap.roundToPx()
            if (authorPlaceable != null) {
                val authorTop = titleTop + titleLine + style.authorGap.roundToPx()
                authorPlaceable.place(contentLeft, authorTop)
                textTop = authorTop + authorPlaceable.height + style.authorToTextGap.roundToPx()
            }
            text.place(contentLeft, textTop)
            badges.place(contentRight - badges.width, height - style.badgeBottom.roundToPx() - badges.height)
            separator.place(style.separatorStart.roundToPx(), height - hairline)
        }
    }
}

// Telegram's TextNode lines: floor(ascent + descent) and floor of that times the spacing, the extra below each line.
@Composable
private fun textNodeStyle(base: TextStyle, spacing: Float): TextStyle {
    if (spacing == 0f) return base
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(base, spacing, density) {
        with(density) {
            val line = floor(measurer.measure(" ", base).size.height.toDp().value)
            val pitch = line + floor(line * spacing)
            base.merge(
                TextStyle(
                    lineHeight = pitch.dp.toSp(),
                    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Top, LineHeightStyle.Trim.None)
                )
            )
        }
    }
}

private enum class Slot { Avatar, Title, Date, Author, Text, Badges, Separator }

@Composable
context(style: ChatListScreenStyleScope)
private fun TitleLine(
    row: ChatListRow,
    customEmoji: Map<Long, Sticker>,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = row.title,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        row.emojiStatusId?.let { id ->
            Spacer(Modifier.width(style.titleIconGap))
            CustomEmoji(customEmoji[id], "", Modifier.size(style.emojiStatusSize))
        }
        if (row.isMuted) {
            val gap = style.mutedIconGap
            if (gap > Dp(0f)) Spacer(Modifier.width(gap))
            Icon(
                imageVector = KotogramIcons.BellOff,
                contentDescription = null,
                tint = style.iconColor,
                modifier = Modifier.offset(x = if (gap < Dp(0f)) gap else Dp(0f)).size(style.mutedIconSize)
            )
        }
    }
}

@Composable
context(style: ChatListScreenStyleScope)
private fun DateLine(row: ChatListRow, textStyle: TextStyle, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        row.outgoing?.let { status ->
            StatusChecks(
                read = status == OutgoingStatus.Read,
                color = style.checkColor,
                modifier = Modifier.size(style.statusIconSize)
            )
            Spacer(Modifier.width(style.statusGap))
        }
        Text(row.time, style = textStyle, maxLines = 1)
    }
}

@Composable
context(style: ChatListScreenStyleScope)
private fun Preview(
    row: ChatListRow,
    customEmoji: Map<Long, Sticker>,
    textStyle: TextStyle,
    maxLines: Int,
    modifier: Modifier = Modifier
) {
    val preview = row.preview
    val thumbnail = (preview as? ChatPreview.Media)?.thumbnail
    val label = when (preview) {
        is ChatPreview.Media -> stringResource(preview.kind.label())
        ChatPreview.Other -> stringResource(Res.string.peerinfo_action_message)
        else -> ""
    }
    val text = buildAnnotatedString {
        if (thumbnail != null) appendInlineContent(THUMBNAIL_SLOT, " ")
        val body = preview.text?.takeIf { it.text.isNotEmpty() }?.toPreviewString()
        if (body != null) append(body) else append(label)
    }
    val emojiSize = with(LocalDensity.current) { style.thumbnailSize.toSp() }
    val inline = customEmojiContent(customEmoji, row.customEmojiIds, emojiSize) + thumbnailContent(thumbnail)
    Text(
        text = text,
        style = textStyle,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        inlineContent = inline,
        modifier = modifier
    )
}

// The image and the gap after it, as the text's top-left cutout leaves room for them.
@Composable
context(style: ChatListScreenStyleScope)
private fun thumbnailContent(thumbnail: Any?): Map<String, InlineTextContent> {
    if (thumbnail == null) return emptyMap()
    val density = LocalDensity.current
    val width = with(density) { (style.thumbnailSize + style.thumbnailTrailing).toSp() }
    val height = with(density) { style.thumbnailSize.toSp() }
    return mapOf(
        THUMBNAIL_SLOT to InlineTextContent(Placeholder(width, height, PlaceholderVerticalAlign.TextCenter)) {
            Box(Modifier.fillMaxHeight()) {
                NetworkImage(
                    model = thumbnail,
                    contentDescription = null,
                    modifier = Modifier.size(style.thumbnailSize).clip(RoundedCornerShape(style.thumbnailCorner))
                )
            }
        }
    )
}

@Composable
context(style: ChatListScreenStyleScope)
private fun Badges(row: ChatListRow, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(style.badgeGap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (row.hasUnreadMention) Badge("@", style.badgeColor)
        when {
            row.unreadCount > 0 ->
                Badge(compactCount(row.unreadCount), if (row.isMuted) style.mutedBadgeColor else style.badgeColor)
            row.isMarkedAsUnread -> Badge("", if (row.isMuted) style.mutedBadgeColor else style.badgeColor)
            row.isPinned -> Icon(KotogramIcons.Pin, null, Modifier.size(style.pinnedIconSize), tint = style.iconColor)
        }
    }
}

@Composable
context(style: ChatListScreenStyleScope)
private fun Badge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .height(style.badgeHeight)
            .defaultMinSize(minWidth = style.badgeHeight)
            .background(color, CircleShape)
            .padding(horizontal = style.badgePadding),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = style.badgeTextStyle, maxLines = 1)
    }
}

// ChatListStatusChecksNode's checks at rest, in its 24-point box: one when sent, two when read.
@Composable
private fun StatusChecks(read: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val unit = size.width / CHECK_BOX
        // A line of a point and a pixel, as iOS strokes it.
        val stroke = Stroke(width = (1.dp.toPx() + 1f) / unit, cap = StrokeCap.Round, join = StrokeJoin.Round)
        scale(unit, unit, pivot = Offset.Zero) {
            drawPath(
                Path().apply {
                    moveTo(20.04f, 8.46f)
                    lineTo(12.5f, 16.5f)
                    if (!read) lineTo(9f, 13f)
                },
                color,
                style = stroke
            )
            if (read) {
                drawPath(
                    Path().apply {
                        moveTo(16.04f, 8.46f)
                        lineTo(8.5f, 16.5f)
                        lineTo(5f, 13f)
                    },
                    color,
                    style = stroke
                )
            }
        }
    }
}

// Telegram's compact counts: 537, 6.3K, 189.7K.
private fun compactCount(count: Int): String = when {
    count < THOUSAND -> count.toString()
    count < MILLION -> tenths(count, THOUSAND) + "K"
    else -> tenths(count, MILLION) + "M"
}

private fun tenths(count: Int, unit: Int): String {
    val tenths = count / (unit / TEN)
    return if (tenths % TEN == 0) "${tenths / TEN}" else "${tenths / TEN}.${tenths % TEN}"
}

private const val THUMBNAIL_SLOT = "thumbnail"
private const val THOUSAND = 1_000
private const val MILLION = 1_000_000
private const val TEN = 10
private const val CHECK_BOX = 24f

// ChatListItem's height: the title's line and three more, less 8.
private const val IOS_ROW_LINES = 4
private val IOS_ROW_INSET = Dp(8f)
