package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.extensions.addRectStack
import com.xbot.kotogram.core.designsystem.extensions.shownLineRight
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

enum class MessageIcon {
    Forwarded,
    VoiceMessage,
    Audio,
    Location,
    Poll,
    Todo,
    Game,
    StoryReply,
    Gift,
    IncomingCall,
    OutgoingCall,
    IncomingVideoCall,
    OutgoingVideoCall,
}

enum class MessageThumbnailType { Photo, Video, RoundVideo }

@Immutable
data class MessageThumbnail(
    val image: @Composable (blur: ImageBlur?) -> Unit,
    val type: MessageThumbnailType = MessageThumbnailType.Photo,
    val spoiler: Boolean = false,
)

@Immutable
sealed interface TopicIcon {
    data object General : TopicIcon

    @Immutable
    data class Bubble(
        val color: Int,
    ) : TopicIcon

    @Immutable
    data class Emoji(
        val content: @Composable () -> Unit,
    ) : TopicIcon
}

@Immutable
data class ForumTopic(
    val title: String,
    val icon: TopicIcon? = null,
    val unread: Boolean = false,
)

@Immutable
data class FolderTag(
    val title: String,
    val colorIndex: Int,
)

@Immutable
data class ArchivedChat(
    val title: String,
    val unread: Boolean = false,
)

const val SPOILER_ANNOTATION = "com.xbot.kotogram.spoiler"

@Composable
fun ChatListItemScope.Message(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    author: String? = null,
    icon: MessageIcon? = null,
    thumbnails: List<MessageThumbnail> = emptyList(),
    topics: List<ForumTopic> = emptyList(),
    tags: List<FolderTag> = emptyList(),
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
) {
    val forum = topics.isNotEmpty() && tags.isEmpty()
    val inlineAuthor = author != null && (forum || tags.isNotEmpty())
    val body = if (inlineAuthor) text.prefixed("$author: ", texts.author) else text
    TextBlock(
        text = body,
        header = when {
            forum -> HeaderContent.Topics(topics)
            author != null && !inlineAuthor -> HeaderContent.Line(author, texts.author)
            else -> null
        },
        tags = tags,
        icon = icon,
        thumbnails = thumbnails,
        inlineContent = inlineContent,
        modifier = modifier,
    )
}

@Composable
fun ChatListItemScope.Draft(
    label: String,
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    tags: List<FolderTag> = emptyList(),
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
) {
    if (tags.isEmpty()) {
        TextBlock(text, HeaderContent.Line(label, texts.draft), tags, null, emptyList(), inlineContent, modifier)
    } else {
        TextBlock(text.prefixed("$label: ", texts.draft), null, tags, null, emptyList(), inlineContent, modifier)
    }
}

@Composable
fun ChatListItemScope.ArchivedChats(
    chats: List<ArchivedChat>,
    modifier: Modifier = Modifier,
) {
    val unreadColor = texts.author.color
    val text = remember(chats, unreadColor) {
        buildAnnotatedString {
            chats.forEachIndexed { index, chat ->
                if (index > 0) append(", ")
                if (chat.unread) withStyle(SpanStyle(color = unreadColor)) {
                    append(chat.title)
                } else append(chat.title)
            }
        }
    }
    TextBlock(text, null, emptyList(), null, emptyList(), emptyMap(), modifier)
}

private fun AnnotatedString.prefixed(
    prefix: String,
    style: TextStyle,
): AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(color = style.color)) { append(prefix) }
    append(this@prefixed)
}

private sealed interface HeaderContent {
    class Line(
        val text: String,
        val style: TextStyle,
    ) : HeaderContent

    class Topics(
        val topics: List<ForumTopic>,
    ) : HeaderContent
}

private enum class BlockPart { Header, Text, Tags }

@Composable
private fun ChatListItemScope.TextBlock(
    text: AnnotatedString,
    header: HeaderContent?,
    tags: List<FolderTag>,
    icon: MessageIcon?,
    thumbnails: List<MessageThumbnail>,
    inlineContent: Map<String, InlineTextContent>,
    modifier: Modifier = Modifier,
) {
    val leading = remember(icon, thumbnails.size) {
        buildList {
            if (icon != null) add(ICON_SLOT)
            repeat(thumbnails.size) { add("$THUMBNAIL_SLOT$it") }
        }
    }
    val spoilers = remember(text) { text.spoilers() }
    val slots = inlineContent.keys
    val annotated = remember(text, leading, slots) {
        buildAnnotatedString {
            leading.forEach { appendInlineContent(it, " ") }
            append(text.hiding(spoilers, slots))
        }
    }
    val shownContent = inlineContent.withHiddenSlots(spoilers)
    val readText = remember(text, spoilers) { AnnotatedString(text.withoutSpoilers(spoilers)) }
    val leadingContent = leadingContent(icon, thumbnails)
    val highlight = remember { TopicHighlight() }
    val topics = (header as? HeaderContent.Topics)?.topics.orEmpty()
    val highlighted = topics.firstOrNull()?.unread == true
    val highlightColor = style.topicHighlightColor
    val dustColor = texts.text.color
    val spoilerAreas = remember { SpoilerAreas() }
    val spoilerInk = with(LocalDensity.current) {
        remember(texts, this) { SpoilerInk(texts.textInk, SpoilerInset.toPx(), SpoilerTruncated.toPx()) }
    }
    Layout(
        modifier = modifier.drawWithCache {
            val shape = highlight.shape?.takeIf { highlighted }
            val path = shape?.let {
                Path().apply { addRectStack(listOf(it.top, it.bottom), radius = it.radius, snap = it.snap) }
            }
            onDrawBehind { path?.let { drawPath(it, highlightColor) } }
        },
        content = {
            when (header) {
                is HeaderContent.Line -> Text(
                    text = header.text,
                    style = header.style,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.layoutId(BlockPart.Header),
                )

                is HeaderContent.Topics -> TopicsLine(header.topics, Modifier.layoutId(BlockPart.Header))

                null -> Unit
            }
            Text(
                text = annotated,
                style = texts.text,
                maxLines = if (header != null || tags.isNotEmpty()) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                inlineContent = shownContent + leadingContent,
                onTextLayout = {
                    spoilerAreas.update(it, spoilers, leading.size, spoilerInk)
                    // Workaround: onTextLayout runs inside the text's measure, before the block reads the line's end.
                    highlight.lineRight = it.shownLineRight(0, texts.textEllipsis)
                },
                modifier = Modifier
                    .layoutId(BlockPart.Text)
                    .then(
                        if (spoilers.isEmpty()) {
                            Modifier
                        } else {
                            Modifier
                                .semantics { this.text = readText }
                                .invisibleInk(dustColor) { spoilerAreas.rects }
                        },
                    ),
            )
            if (tags.isNotEmpty()) TagsLine(tags, Modifier.layoutId(BlockPart.Tags))
            if (highlighted) {
                Icon(
                    imageVector = KotogramIcons.DisclosureArrow,
                    contentDescription = null,
                    modifier = Modifier.layoutId(TOPIC_ARROW_ID).size(TopicArrowSize),
                    tint = style.topicArrowColor,
                )
            }
        },
        measurePolicy = remember(texts, highlight) { TextBlockMeasurePolicy(texts.text, highlight) },
    )
}

private class TextBlockMeasurePolicy(
    private val textStyle: TextStyle,
    private val highlight: TopicHighlight,
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val arrowMeasurable = measurables.firstOrNull { it.layoutId == TOPIC_ARROW_ID }
        val arrowReserve = if (arrowMeasurable != null) TopicArrowReserve.roundToPx() else 0
        val header = measurables.firstOrNull { it.layoutId == BlockPart.Header }?.measure(loose)
        val text = measurables.first { it.layoutId == BlockPart.Text }.measure(
            loose.copy(maxWidth = (loose.maxWidth - arrowReserve).coerceAtLeast(0)),
        )
        val tags = measurables.firstOrNull { it.layoutId == BlockPart.Tags }?.measure(loose)
        val arrow = arrowMeasurable?.measure(Constraints())
        val textY = if (header != null) textStyle.lineHeight.roundToPx() else 0
        val textBaseline = text.baselineOr(0)
        val tagsY = textY + textBaseline + TagsDrop.roundToPx()
        val height = maxOf(textY + text.height, tags?.let { tagsY + it.height } ?: 0)
        val firstBaseline = header?.baselineOr(textBaseline) ?: textBaseline
        val width = loose.maxWidth.takeIf { it != Constraints.Infinity } ?: text.width
        val bottom = highlight.update(this, header, textY, text, textStyle.fontSize)?.bottom
        return layout(width, height, mapOf(FirstBaseline to firstBaseline)) {
            header?.place(0, 0)
            text.place(0, textY)
            tags?.place(0, tagsY)
            if (arrow != null && bottom != null) {
                arrow.place(
                    (bottom.right - arrow.width).roundToInt(),
                    (bottom.top + floor((bottom.height - arrow.height) / 2)).roundToInt(),
                )
            }
        }
    }
}

internal fun Placeable.baselineOr(fallback: Int): Int =
    get(FirstBaseline).takeIf { it != AlignmentLine.Unspecified } ?: fallback

private const val TOPIC_ARROW_ID = "chat-list-item:topic-arrow"

@Composable
private fun ChatListItemScope.TagsLine(
    tags: List<FolderTag>,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(TagSpacing)) {
        tags.take(MAX_TAGS).forEach { tag ->
            val color = style.tagColors.getOrNull(tag.colorIndex) ?: style.tagOverflowColor
            Tag(tag.title.uppercase(), color)
        }
        if (tags.size > MAX_TAGS) Tag("+${tags.size - MAX_TAGS}", style.tagOverflowColor)
    }
}

@Composable
private fun ChatListItemScope.Tag(
    text: String,
    color: Color,
) {
    Box(
        Modifier
            .background(color.copy(alpha = TAG_BACKGROUND_ALPHA), RoundedCornerShape(TagCorner))
            .padding(horizontal = TagPaddingX, vertical = TagPaddingY),
    ) {
        Text(text, style = texts.smallLabel.merge(color = color), maxLines = 1)
    }
}

@Composable
private fun ChatListItemScope.leadingContent(
    icon: MessageIcon?,
    thumbnails: List<MessageThumbnail>,
): Map<String, InlineTextContent> = with(LocalDensity.current) {
    buildMap {
        if (icon != null) {
            val look = MessageIcons.getValue(icon)
            val gap = if (thumbnails.isEmpty()) MessageIconTextGap else MessageIconThumbnailGap
            val placeholder = Placeholder((look.size + gap).toSp(), look.size.toSp(), PlaceholderVerticalAlign.Top)
            put(
                ICON_SLOT,
                InlineTextContent(placeholder) {
                    Icon(look.icon, null, Modifier.offset(y = look.offset).size(look.size), tint = style.iconColor)
                },
            )
        }
        thumbnails.forEachIndexed { index, thumbnail ->
            val gap = if (index == thumbnails.lastIndex) ThumbnailTrailing else ThumbnailSpacing
            val placeholder = Placeholder(
                (style.thumbnailSize + gap).toSp(),
                style.thumbnailSize.toSp(),
                PlaceholderVerticalAlign.Top,
            )
            put("$THUMBNAIL_SLOT$index", InlineTextContent(placeholder) { Thumbnail(thumbnail) })
        }
    }
}

@Composable
private fun ChatListItemScope.Thumbnail(thumbnail: MessageThumbnail) {
    val shape = if (thumbnail.type == MessageThumbnailType.RoundVideo) CircleShape else ThumbnailShape
    Box(Modifier.size(style.thumbnailSize).clip(shape), contentAlignment = Alignment.Center) {
        thumbnail.image(if (thumbnail.spoiler) ImageBlur.Spoiler else null)
        if (thumbnail.type != MessageThumbnailType.Photo) {
            Icon(KotogramIcons.ThumbnailPlay, null, Modifier.size(ThumbnailPlaySize), tint = Color.White)
        }
    }
}

private const val ICON_SLOT = "chat-list-item:icon"

private const val THUMBNAIL_SLOT = "chat-list-item:thumbnail:"

private class MessageIconLook(
    val icon: ImageVector,
    val size: Dp,
    val offset: Dp,
)

private val MessageIcons = mapOf(
    MessageIcon.Forwarded to MessageIconLook(KotogramIcons.ForwardFilled, 14.dp, 3.dp),
    MessageIcon.VoiceMessage to MessageIconLook(KotogramIcons.MessageVoice, 20.dp, (-1).dp),
    MessageIcon.Audio to MessageIconLook(KotogramIcons.MessageAudio, 20.dp, (-1).dp),
    MessageIcon.Location to MessageIconLook(KotogramIcons.MessageLocation, 20.dp, (-1.333).dp),
    MessageIcon.Poll to MessageIconLook(KotogramIcons.MessagePoll, 20.dp, (-1).dp),
    MessageIcon.Todo to MessageIconLook(KotogramIcons.MessageTodo, 20.dp, (-1).dp),
    MessageIcon.Game to MessageIconLook(KotogramIcons.MessageGame, 20.dp, (-1).dp),
    MessageIcon.StoryReply to MessageIconLook(KotogramIcons.MessageStoryReply, 20.dp, 0.dp),
    MessageIcon.Gift to MessageIconLook(KotogramIcons.MessageGift, 20.dp, (-2.333).dp),
    MessageIcon.IncomingCall to MessageIconLook(KotogramIcons.MessageCallIncoming, 20.dp, 0.dp),
    MessageIcon.OutgoingCall to MessageIconLook(KotogramIcons.MessageCallOutgoing, 20.dp, 0.dp),
    MessageIcon.IncomingVideoCall to MessageIconLook(KotogramIcons.MessageVideoCallIncoming, 20.dp, 0.dp),
    MessageIcon.OutgoingVideoCall to MessageIconLook(KotogramIcons.MessageVideoCallOutgoing, 20.dp, 0.dp),
)

private val MessageIconTextGap = 4.dp

private val MessageIconThumbnailGap = 6.dp

private val ThumbnailSpacing = 2.dp

private val ThumbnailTrailing = 5.dp

private val ThumbnailShape = RoundedCornerShape(2.dp)

private val ThumbnailPlaySize = 16.dp

private val TagsDrop = 5.333.dp

private const val MAX_TAGS = 3

private val TagSpacing = 5.dp

private val TagCorner = 4.dp

private val TagPaddingX = 4.dp

private val TagPaddingY = 2.dp

private const val TAG_BACKGROUND_ALPHA = 0.1f

private val TopicArrowSize = 18.dp

private val TopicArrowReserve = 18.dp

private class TopicHighlight {
    var lineRight: Float = 0f
    var shape: HighlightShape? by mutableStateOf(null)
        private set

    fun update(
        density: Density,
        header: Placeable?,
        textY: Int,
        text: Placeable,
        fontSize: TextUnit,
    ): HighlightShape? = with(density) {
        val topicWidth = header?.get(FirstTopicEnd)?.takeIf { it != AlignmentLine.Unspecified }
        if (header == null || topicWidth == null) return@with null
        val unit = 1.dp.toPx()
        val left = -TopicHighlightStart.toPx()
        val top = header.baselineOr(0) - fontSize.toPx() * TOPIC_HIGHLIGHT_RISE
        val bottomEdge = textY + text.baselineOr(0) + fontSize.toPx() * TOPIC_HIGHLIGHT_DROP
        val midY = top + (floor((bottomEdge - top) / 2 / unit) + 1) * unit
        val topRight = topicWidth + TopicHighlightEnd.toPx()
        var bottomRight = ceil(minOf(lineRight, text.width.toFloat()) / unit) * unit + TopicTextExtra.toPx()
        if (bottomRight < topRight && topRight - bottomRight < TopicMergeDistance.toPx()) bottomRight = topRight
        val bottom = Rect(left, midY, bottomRight, bottomEdge)
        HighlightShape(
            top = Rect(left, top, topRight, midY),
            bottom = bottom,
            radius = floor(bottom.height / 2 / unit) * unit,
            snap = TopicSnap.toPx(),
        )
    }.also { shape = it }
}

private data class HighlightShape(
    val top: Rect,
    val bottom: Rect,
    val radius: Float,
    val snap: Float,
)

internal val FirstTopicEnd = VerticalAlignmentLine(::minOf)
private val TopicTextExtra = 15.dp
private val TopicHighlightStart = 3.dp
private val TopicHighlightEnd = 2.dp
private const val TOPIC_HIGHLIGHT_RISE = 49f / 45
private const val TOPIC_HIGHLIGHT_DROP = 20f / 45
private val TopicMergeDistance = 5.dp
private val TopicSnap = 2.dp
private val SpoilerInset = 1.dp
private val SpoilerTruncated = 12.dp
