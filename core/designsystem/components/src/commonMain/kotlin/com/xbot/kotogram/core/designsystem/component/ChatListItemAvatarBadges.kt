package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.extensions.background
import com.xbot.kotogram.core.designsystem.extensions.shown
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import kotlin.math.floor

@Composable
internal fun ChatListItemScope.OnlineDot(visible: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.offset(x = -OnlineMargin, y = -OnlineMargin),
            enter = scaleIn(tween(MARKER_MILLIS)),
            exit = scaleOut(tween(MARKER_MILLIS)),
        ) {
            Box(
                modifier = Modifier
                    .size(OnlineSize)
                    .background(containerColor, CircleShape)
                    .padding(OnlineRing)
                    .background(style.onlineColor, CircleShape),
            )
        }
    }
}

@Composable
internal fun ChatListItemScope.VoiceChatDot(visible: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.offset(x = VoiceChatOverhang, y = VoiceChatOverhang),
            enter = scaleIn(tween(MARKER_MILLIS)),
            exit = scaleOut(tween(MARKER_MILLIS)),
        ) { VoiceChatBars() }
    }
}

@Composable
private fun ChatListItemScope.VoiceChatBars() {
    val transition = rememberInfiniteTransition()
    val side by transition.animateFloat(SIDE_BARS[0], SIDE_BARS[0], barsSpec(SIDE_BARS))
    val middle by transition.animateFloat(MIDDLE_BARS[0], MIDDLE_BARS[0], barsSpec(MIDDLE_BARS))
    val barColor = style.voiceChatBarColor
    Canvas(
        Modifier
            .size(VoiceChatSize)
            .background(containerColor, CircleShape)
            .padding(OnlineRing)
            .background(style.onlineColor, CircleShape),
    ) {
        val unit = 1.dp.toPx()
        val bar = BAR_WIDTH * unit
        listOf(BAR_LEFT to side, BAR_MIDDLE to middle, BAR_RIGHT to side).forEach { (x, height) ->
            val h = height * unit
            drawRoundRect(
                color = barColor,
                topLeft = Offset((x - OnlineRing.value) * unit, size.height / 2 - h / 2),
                size = Size(bar, h),
                cornerRadius = CornerRadius(bar / 2),
            )
        }
    }
}

private fun barsSpec(values: FloatArray): InfiniteRepeatableSpec<Float> = InfiniteRepeatableSpec(
    animation = keyframes {
        durationMillis = BARS_DURATION
        values.forEachIndexed { index, value -> value at BARS_DURATION * index / (values.size - 1) using EaseInOut }
    },
    repeatMode = RepeatMode.Restart,
)

@Composable
internal fun ChatListItemScope.AutoDeleteBadge(
    text: String?,
    avatar: @Composable (blur: ImageBlur?) -> Unit,
) {
    val timers = updateTransition(text)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        timers.AnimatedVisibility(
            visible = { it != null },
            enter = scaleIn(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = scaleOut(spring(stiffness = Spring.StiffnessMediumLow)),
        ) { AutoDeleteContent(timers.shown.orEmpty(), avatar) }
    }
}

@Composable
private fun ChatListItemScope.AutoDeleteContent(
    text: String,
    avatar: @Composable (blur: ImageBlur?) -> Unit,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val contentColor = style.autoDeleteContentColor
    val family = style.titleTextStyle.fontFamily
    Box(Modifier.size(AutoDeleteSize).clip(CircleShape)) {
        BlurredCorner(style.avatarSize, avatar)
        Spacer(
            Modifier
                .fillMaxSize()
                .background(AutoDeleteShade)
                .drawWithCache {
                    val maxWidth = size.width - with(density) { AutoDeleteTextInset.toPx() } * 2
                    var fontSize = floor(AutoDeleteSize.value * AUTO_DELETE_TEXT_SCALE)
                    var layout = measurer.measure(text, autoDeleteText(fontSize, contentColor, family))
                    while (layout.size.width > maxWidth && fontSize > 2f) {
                        fontSize -= 1f
                        layout = measurer.measure(text, autoDeleteText(fontSize, contentColor, family))
                    }
                    val topLeft = Offset(
                        floor((size.width - layout.size.width) / 2),
                        floor((size.height - layout.size.height) / 2),
                    )
                    onDrawBehind {
                        drawText(layout, topLeft = topLeft)
                        drawTimerRing(contentColor)
                    }
                },
        )
    }
}

// Hack: the backdrop is a second, blurred copy of the avatar's slot, so whatever the slot holds runs twice.
@Composable
private fun BoxScope.BlurredCorner(
    avatarSize: Dp,
    avatar: @Composable (blur: ImageBlur?) -> Unit,
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .layout { measurable, constraints ->
                val side = avatarSize.roundToPx()
                val placeable = measurable.measure(Constraints.fixed(side, side))
                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.place(constraints.maxWidth - placeable.width, constraints.maxHeight - placeable.height)
                }
            },
    ) { avatar(ImageBlur.Backdrop) }
}

private fun autoDeleteText(
    size: Float,
    color: Color,
    family: FontFamily?,
) = TextStyle(color = color, fontSize = size.sp, fontWeight = FontWeight.Bold, fontFamily = family)

private fun DrawScope.drawTimerRing(color: Color) {
    val line = TIMER_LINE.dp.toPx()
    val radius = size.width / 2 - TIMER_INSET.dp.toPx() - line / 2
    val topLeft = Offset(size.width / 2 - radius, size.height / 2 - radius)
    val box = Size(radius * 2, radius * 2)
    val stroke = Stroke(width = line, cap = StrokeCap.Round)
    drawArc(color, DOWN, HALF_TURN, useCenter = false, topLeft = topLeft, size = box, style = stroke)
    val section = HALF_TURN / TIMER_SECTIONS
    for (index in 1 until TIMER_DASHES step 2) {
        val start = DOWN - index * section - section * TIMER_DASH_LEAD
        drawArc(
            color,
            start,
            -section * TIMER_DASH,
            useCenter = false,
            topLeft = topLeft,
            size = box,
            style = stroke,
        )
    }
}

@Composable
internal fun ChatListItemScope.SubscriptionStar() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .offset(x = StarOverhang, y = StarOverhang)
                .size(StarSize)
                .background(containerColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(KotogramIcons.PremiumStar, null, Modifier.requiredSize(StarSize), tint = style.subscriptionColor)
        }
    }
}

@Composable
internal fun ChatListItemScope.LiveBadge(label: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Box(
            modifier = Modifier
                .offset(y = LiveDrop + LiveOutline)
                .background(containerColor, PillShape)
                .padding(LiveOutline)
                .background(style.liveBadgeColor, PillShape)
                .padding(horizontal = LivePaddingX, vertical = LivePaddingY),
        ) {
            Text(
                text = label,
                style = style.smallLabelTextStyle.merge(color = style.liveBadgeContentColor),
                maxLines = 1,
            )
        }
    }
}
private val OnlineSize = 14.dp

private val OnlineRing = 2.dp

private val OnlineMargin = 2.dp

private val VoiceChatSize = 22.dp

private val VoiceChatOverhang = 0.667.dp

private const val BAR_WIDTH = 2f

private const val BAR_LEFT = 6f

private const val BAR_MIDDLE = 10f

private const val BAR_RIGHT = 14f

private val SIDE_BARS: FloatArray = [10f, 4f, 8f, 4f, 10f]

private val MIDDLE_BARS: FloatArray = [6f, 10f, 4f, 12f, 6f]

private const val BARS_DURATION = 2200

private val AutoDeleteSize = 24.dp

private const val AUTO_DELETE_TEXT_SCALE = 0.48f

private val AutoDeleteTextInset = 5.dp

private val AutoDeleteShade = Color.Black.copy(alpha = 0.06f)

private const val TIMER_LINE = 1.5f

private const val TIMER_INSET = 2f

private const val TIMER_SECTIONS = 11f

private const val TIMER_DASHES = 10

private const val TIMER_DASH_LEAD = 0.15f

private const val TIMER_DASH = 0.75f

private val StarSize = 20.dp

private val StarOverhang = 1.dp

private val LiveDrop = 5.dp

private val LiveOutline = 2.dp

private val LivePaddingX = 4.dp

private val LivePaddingY = 2.dp
private val PillShape = RoundedCornerShape(percent = 50)
private const val DOWN = 90f
private const val HALF_TURN = 180f
private const val MARKER_MILLIS = 200
private val EaseInOut = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
