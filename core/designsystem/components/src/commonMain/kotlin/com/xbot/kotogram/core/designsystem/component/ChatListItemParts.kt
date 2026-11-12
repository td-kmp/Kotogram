package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.extensions.shown
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import kotlin.math.roundToInt

@Composable
fun ChatListItemScope.Date(
    text: String,
    modifier: Modifier = Modifier,
    status: MessageStatus? = null,
) {
    val lift = with(LocalDensity.current) { (texts.date.fontSize.toPx() * STATUS_ICON_LIFT).roundToInt() }
    val statuses = updateTransition(status)
    Row(modifier) {
        statuses.AnimatedVisibility(
            visible = { it != null },
            modifier = Modifier.alignBy { it.measuredHeight / 2 + lift },
            enter = fadeIn(tween(STATUS_FADE_MILLIS)),
            exit = fadeOut(tween(STATUS_FADE_MILLIS)),
        ) {
            statuses.shown?.let { MessageStatusIcon(status = it, modifier = Modifier.size(style.statusIconSize)) }
        }
        if (status != null) Spacer(Modifier.width(StatusGap))
        Text(text, style = texts.date, maxLines = 1, modifier = Modifier.alignByBaseline())
    }
}

@Composable
fun ChatListItemScope.Badges(
    modifier: Modifier = Modifier,
    unreadCount: String? = null,
    muted: Boolean = false,
    mention: Boolean = false,
    reaction: Boolean = false,
    pollVotes: Boolean = false,
    pinned: Boolean = false,
    archived: Boolean = false,
) {
    val marker = badgeMarker(mention, reaction, pollVotes, muted, archived)
    val markers = updateTransition(marker)
    val counts = updateTransition(unreadCount)
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        markers.AnimatedVisibility(visible = { it != null }, enter = popIn(bounce = true), exit = popOut()) {
            markers.shown?.let { Icon(it.icon, null, Modifier.size(BadgeHeight), tint = it.color) }
        }
        if (marker != null && unreadCount != null) Spacer(Modifier.width(style.badgeGap))
        counts.AnimatedVisibility(visible = { it != null }, enter = popIn(bounce = !muted), exit = popOut()) {
            val color = if (muted) style.mutedBadgeColor else style.badgeColor
            val content = if (muted) style.mutedBadgeContentColor else style.badgeContentColor
            CountBadge(counts.shown.orEmpty(), color, content, bounce = !muted)
        }
        if (pinned && marker == null && unreadCount == null) {
            Icon(KotogramIcons.PinFilled, null, Modifier.size(PinnedIconSize), tint = style.iconColor)
        }
    }
}

private fun ChatListItemScope.badgeMarker(
    mention: Boolean,
    reaction: Boolean,
    pollVotes: Boolean,
    muted: Boolean,
    archived: Boolean,
): BadgeMarker? = when {
    mention -> BadgeMarker(KotogramIcons.MentionBadge, if (archived) style.mutedBadgeColor else style.badgeColor)

    reaction -> BadgeMarker(
        KotogramIcons.ReactionBadge,
        if (muted) style.mutedBadgeColor else style.reactionBadgeColor,
    )

    pollVotes -> BadgeMarker(KotogramIcons.PollVotesBadge, if (muted) style.mutedBadgeColor else style.badgeColor)

    else -> null
}

private data class BadgeMarker(
    val icon: ImageVector,
    val color: Color,
)

private fun popIn(bounce: Boolean): EnterTransition = scaleIn(
    animationSpec = if (bounce) {
        keyframes {
            durationMillis = BOUNCE_IN_MILLIS + BOUNCE_BACK_MILLIS
            BOUNCE_SCALE at BOUNCE_IN_MILLIS
        }
    } else {
        tween(BOUNCE_IN_MILLIS)
    },
)

private fun popOut(): ExitTransition = scaleOut(tween(POP_OUT_MILLIS))

@Composable
fun ChatListItemScope.OpenAppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(style.openAppColor)
            .clickable(onClick = onClick)
            .padding(horizontal = OpenAppPaddingX, vertical = OpenAppPaddingY),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = style.openAppTextStyle.merge(color = style.openAppContentColor), maxLines = 1)
    }
}

@Composable
private fun ChatListItemScope.CountBadge(
    text: String,
    color: Color,
    contentColor: Color,
    bounce: Boolean,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val width = remember(text, texts, density) {
        val height = with(density) { BadgeHeight.roundToPx() }
        val widest = if (text.length < 2) "8" else text.dropLast(1) + "8"
        val measured = measurer.measure(widest, texts.badge).size.width
        with(density) { maxOf(height, measured + height / 2).toDp() }
    }
    val animatedWidth by animateDpAsState(width, tween(COUNT_CHANGE_MILLIS, easing = FastOutSlowInEasing))
    val pulse = remember { Animatable(1f) }
    var previous by remember { mutableStateOf(text) }
    LaunchedEffect(text) {
        val grew = (text.toIntOrNull() ?: 0) > (previous.toIntOrNull() ?: Int.MAX_VALUE)
        previous = text
        if (bounce && grew) {
            pulse.animateTo(BOUNCE_SCALE, tween(PULSE_MILLIS))
            pulse.animateTo(1f, tween(PULSE_MILLIS))
        }
    }
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = pulse.value
                scaleY = pulse.value
            }
            .size(animatedWidth, BadgeHeight)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                val shift = with(density) { CountShift.roundToPx() }
                val spec = tween<IntOffset>(COUNT_CHANGE_MILLIS, easing = FastOutSlowInEasing)
                (slideInVertically(spec) { shift } + fadeIn(tween(COUNT_CHANGE_MILLIS))) togetherWith
                    (slideOutVertically(spec) { -shift } + fadeOut(tween(COUNT_CHANGE_MILLIS)))
            },
        ) { value ->
            if (value.isNotEmpty()) {
                Text(text = value, style = texts.badge.merge(color = contentColor), maxLines = 1)
            }
        }
    }
}

private val StatusGap = 0.dp

private const val STATUS_ICON_LIFT = 8f / 21

private val BadgeHeight = 20.dp
private val CountShift = 8.dp
private const val BOUNCE_IN_MILLIS = 200
private const val BOUNCE_BACK_MILLIS = 120
private const val BOUNCE_SCALE = 1.2f
private const val POP_OUT_MILLIS = 120
private const val PULSE_MILLIS = 120
private const val COUNT_CHANGE_MILLIS = 150
private const val STATUS_FADE_MILLIS = 200

private val OpenAppPaddingX = 12.dp

private val OpenAppPaddingY = 4.dp

private val PinnedIconSize = 20.dp
