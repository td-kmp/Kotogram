package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.extensions.glyphBounds
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.theme.platformRoundedFontFamily
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

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
class MessageThumbnail(
    val image: @Composable () -> Unit,
    val type: MessageThumbnailType = MessageThumbnailType.Photo,
    val spoiler: Boolean = false,
)

@Immutable
sealed interface TopicIcon {
    data object General : TopicIcon

    @Immutable
    class Bubble(
        val color: Int,
    ) : TopicIcon

    @Immutable
    class Emoji(
        val content: @Composable () -> Unit,
    ) : TopicIcon
}

@Immutable
class ForumTopic(
    val title: String,
    val icon: TopicIcon? = null,
    val unread: Boolean = false,
)

@Immutable
class FolderTag(
    val title: String,
    val colorIndex: Int,
)

@Immutable
class ArchivedChat(
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
    val annotated = remember(text, leading) {
        buildAnnotatedString {
            leading.forEach { appendInlineContent(it, " ") }
            append(text.hidingSpoilers())
        }
    }
    val leadingContent = leadingContent(icon, thumbnails)
    var textLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val highlight = remember { TopicHighlight() }
    val topics = (header as? HeaderContent.Topics)?.topics.orEmpty()
    val highlighted = topics.firstOrNull()?.unread == true
    val highlightColor = style.topicHighlightColor
    val dustColor = texts.text.color
    val dustTime = if (text.hasSpoilers()) dustTime() else null
    Layout(
        modifier = modifier.drawBehind {
            if (highlighted) highlight.path?.let { drawPath(it, highlightColor) }
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
                inlineContent = inlineContent + leadingContent,
                onTextLayout = { textLayout = it },
                modifier = Modifier
                    .layoutId(BlockPart.Text)
                    .drawWithContent {
                        drawContent()
                        val time = dustTime?.value
                        if (time != null) textLayout?.let { drawSpoilerDust(text, it, leading.size, dustColor, time) }
                    },
            )
            if (tags.isNotEmpty()) TagsLine(tags, Modifier.layoutId(BlockPart.Tags))
            if (highlighted) {
                Icon(
                    imageVector = KotogramIcons.TopicArrow,
                    contentDescription = null,
                    modifier = Modifier.layoutId(TOPIC_ARROW_ID).size(TopicArrowWidth, TopicArrowHeight),
                    tint = style.iconColor,
                )
            }
        },
        measurePolicy = remember(texts, highlight) { TextBlockMeasurePolicy(texts.text.lineHeight, highlight) },
    )
}

private class TextBlockMeasurePolicy(
    private val lineHeight: TextUnit,
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
        val textY = if (header != null) lineHeight.roundToPx() else 0
        val textBaseline = text.baselineOr(0)
        val tagsY = textY + textBaseline + TagsDrop.roundToPx()
        val height = maxOf(textY + text.height, tags?.let { tagsY + it.height } ?: 0)
        val firstBaseline = header?.baselineOr(textBaseline) ?: textBaseline
        val width = loose.maxWidth.takeIf { it != Constraints.Infinity } ?: text.width
        highlight.update(this, header, textY, text)
        return layout(width, height, mapOf(FirstBaseline to firstBaseline)) {
            header?.place(0, 0)
            text.place(0, textY)
            tags?.place(0, tagsY)
            arrow?.place(
                (highlight.bottom.right - arrow.width).roundToInt(),
                (highlight.bottom.top + floor((highlight.bottom.height - arrow.height) / 2)).roundToInt(),
            )
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
        if (thumbnail.spoiler) {
            Box(Modifier.size(style.thumbnailSize).blur(ThumbnailSpoilerBlur, BlurredEdgeTreatment.Rectangle)) {
                thumbnail.image()
            }
            val time = dustTime()
            Canvas(Modifier.size(style.thumbnailSize)) {
                drawDust(Rect(Offset.Zero, size), Color.White, time.value, seed = THUMBNAIL_DUST_SEED)
            }
        } else {
            thumbnail.image()
        }
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

private val ThumbnailSpoilerBlur = 3.dp

private val TagsDrop = 5.333.dp

private const val MAX_TAGS = 3

private val TagSpacing = 5.dp

private val TagCorner = 4.dp

private val TagPaddingX = 4.dp

private val TagPaddingY = 2.dp

private const val TAG_BACKGROUND_ALPHA = 0.1f

private val TopicArrowWidth = 4.5.dp

private val TopicArrowHeight = 9.dp

private val TopicArrowReserve = 18.dp

private class TopicHighlight {
    var path: Path? = null
    var bottom: Rect = Rect.Zero

    fun update(
        density: Density,
        header: Placeable?,
        textY: Int,
        text: Placeable,
    ) = with(density) {
        val topicWidth = header?.get(FirstTopicEnd)?.takeIf { it != AlignmentLine.Unspecified }
        if (header == null || topicWidth == null) {
            path = null
            return@with
        }
        val top = Rect(-1f, 0f, topicWidth + 1f, header.height.toFloat())
        val textBottom = (textY + text.height - 1).toFloat()
        val midY = floor((top.top + textBottom) / 2) + 1
        var bottomRight = text.width + TopicTextExtra.toPx()
        if (bottomRight < top.right && top.right - bottomRight < MERGE_DISTANCE.dp.toPx()) bottomRight = top.right
        bottom = Rect(top.left, midY, bottomRight, textBottom)
        val outer = floor(bottom.height / 2)
        val topPath = Path().apply {
            addRoundRect(RoundRect(Rect(top.left, top.top, top.right, midY + outer), CornerRadius(outer)))
        }
        val bottomPath = Path().apply { addRoundRect(RoundRect(bottom, CornerRadius(outer))) }
        path = Path().apply { op(topPath, bottomPath, PathOperation.Union) }
    }
}

internal val FirstTopicEnd = VerticalAlignmentLine(::minOf)
private val TopicTextExtra = 16.dp
private const val MERGE_DISTANCE = 5f
private const val THUMBNAIL_DUST_SEED = 7
