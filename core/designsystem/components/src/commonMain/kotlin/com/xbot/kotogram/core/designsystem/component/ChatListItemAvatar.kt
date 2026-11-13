package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.floor

@Immutable
sealed interface StoryRing {
    @Immutable
    data class Stories(
        val total: Int,
        val unseen: Int,
        val closeFriends: Boolean = false,
    ) : StoryRing

    @Immutable
    data class Live(
        val label: String,
    ) : StoryRing
}

@Immutable
sealed interface AvatarBadge {
    data object Online : AvatarBadge

    data object VoiceChat : AvatarBadge

    @Immutable
    data class AutoDelete(
        val text: String,
    ) : AvatarBadge

    data object Subscription : AvatarBadge
}

@Composable
fun ChatListItemScope.ChatAvatar(
    modifier: Modifier = Modifier,
    stories: StoryRing? = null,
    badge: AvatarBadge? = null,
    shape: AvatarShape = AvatarShape.Circle,
    content: @Composable (blur: ImageBlur?) -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (stories != null) {
                        val scale = (size.width - StoryLine.toPx() * 4) / size.width
                        scaleX = scale
                        scaleY = scale
                    }
                },
        ) { content(null) }
        if (stories != null) StoryRingCanvas(stories, shape)
        OnlineDot(visible = badge == Online)
        VoiceChatDot(visible = badge == VoiceChat)
        AutoDeleteBadge(text = (badge as? AvatarBadge.AutoDelete)?.text, avatar = content)
        if (badge == Subscription) SubscriptionStar()
        if (stories is StoryRing.Live) LiveBadge(stories.label)
    }
}

@Composable
private fun ChatListItemScope.StoryRingCanvas(
    stories: StoryRing,
    shape: AvatarShape,
) {
    val unseenColors = when {
        stories is StoryRing.Live -> style.storyLiveColors
        stories is StoryRing.Stories && stories.closeFriends -> style.storyCloseFriendsColors
        else -> style.storyUnseenColors
    }
    val seenColors = style.storySeenColor to style.storySeenColor
    Spacer(
        Modifier
            .fillMaxSize()
            .drawWithCache {
                val active = Brush.verticalGradient([unseenColors.first, unseenColors.second])
                val seen = Brush.verticalGradient([seenColors.first, seenColors.second])
                onDrawBehind { drawStoryRing(stories, shape, active, seen) }
            },
    )
}

private fun DrawScope.drawStoryRing(
    stories: StoryRing,
    shape: AvatarShape,
    active: Brush,
    seen: Brush,
) {
    val activeLine = StoryLine.toPx()
    val seenLine = StorySeenLine.toPx()
    val side = size.minDimension
    if (stories is StoryRing.Stories && stories.total > 1) {
        val seenCount = stories.total - stories.unseen.coerceIn(0, stories.total)
        val outline = ringPath(shape, side, activeLine)
        val measure = PathMeasure().apply { setPath(outline, false) }
        val spacing = activeLine * 2
        val segment = (measure.length - spacing * stories.total) / stories.total
        repeat(stories.total) { index ->
            val isSeen = index < seenCount
            val start = spacing / 2 + index * (segment + spacing)
            val piece = Path()
            measure.getSegment(start, start + segment, piece, startWithMoveTo = true)
            drawPath(
                path = piece,
                brush = if (isSeen) seen else active,
                style = Stroke(width = if (isSeen) seenLine else activeLine, cap = StrokeCap.Round),
            )
        }
    } else {
        val lit = stories is StoryRing.Live || (stories is StoryRing.Stories && stories.unseen > 0)
        val line = if (lit) activeLine else seenLine
        drawPath(ringPath(shape, side, line), if (lit) active else seen, style = Stroke(width = line))
    }
}

private fun ringPath(
    shape: AvatarShape,
    side: Float,
    line: Float,
): Path = Path().apply {
    val inset = line / 2
    val far = side - inset
    val radius = when (shape) {
        Circle -> (side - line) / 2
        RoundedRect -> floor(side * RING_CORNER)
    }
    moveTo(side / 2, inset)
    lineTo(far - radius, inset)
    arcTo(rect(far - radius * 2, inset, far, inset + radius * 2), TOP, QUARTER, forceMoveTo = false)
    lineTo(far, far - radius)
    arcTo(rect(far - radius * 2, far - radius * 2, far, far), RIGHT, QUARTER, forceMoveTo = false)
    lineTo(inset + radius, far)
    arcTo(rect(inset, far - radius * 2, inset + radius * 2, far), BOTTOM, QUARTER, forceMoveTo = false)
    lineTo(inset, inset + radius)
    arcTo(rect(inset, inset, inset + radius * 2, inset + radius * 2), LEFT, QUARTER, forceMoveTo = false)
    close()
}

private fun rect(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
) = Rect(left, top, right, bottom)

private val StoryLine = 2.33.dp

private val StorySeenLine = 1.33.dp

private const val RING_CORNER = 0.27f
private const val TOP = -90f
private const val RIGHT = 0f
private const val BOTTOM = 90f
private const val LEFT = 180f
private const val QUARTER = 90f
