package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.abs
import kotlin.math.floor

private enum class StatusKind { Clock, Checks, Failed }

private val MessageStatus.kind: StatusKind
    get() = when (this) {
        MessageStatus.Sending -> StatusKind.Clock
        MessageStatus.Sent, MessageStatus.Read -> StatusKind.Checks
        MessageStatus.Failed -> StatusKind.Failed
    }

private val MessageStatus.checks: Float
    get() = if (this == MessageStatus.Read) READ else SENT

@Composable
internal fun ChatListItemScope.MessageStatusIcon(
    status: MessageStatus,
    modifier: Modifier = Modifier,
) {
    var shown by remember { mutableStateOf(status) }
    val checks = remember { Animatable(status.checks) }
    LaunchedEffect(status) {
        val previous = shown
        shown = status
        when {
            previous == MessageStatus.Sent && status == MessageStatus.Read -> checks.animateTo(READ, ChecksSpec)

            previous == MessageStatus.Sending && status == MessageStatus.Sent -> {
                checks.snapTo(0f)
                checks.animateTo(SENT, ChecksSpec)
            }

            else -> checks.snapTo(status.checks)
        }
    }
    val pending = style.pendingColor
    val checkColor = style.checkColor
    val failed = style.failedColor
    val failedContent = style.failedContentColor
    AnimatedContent(
        targetState = status.kind,
        modifier = modifier,
        transitionSpec = {
            val enter = if (targetState == StatusKind.Checks && initialState == StatusKind.Clock) {
                EnterTransition.None
            } else {
                fadeIn(tween(TRANSITION_MILLIS))
            }
            enter togetherWith fadeOut(tween(TRANSITION_MILLIS))
        },
    ) { kind ->
        when (kind) {
            StatusKind.Clock -> Clock(pending)
            StatusKind.Checks -> Canvas(Modifier.fillMaxSize()) { inBox { drawChecks(checks.value, checkColor) } }
            StatusKind.Failed -> Canvas(Modifier.fillMaxSize()) { inBox { drawFailed(failed, failedContent) } }
        }
    }
}

@Composable
private fun Clock(color: Color) {
    val transition = rememberInfiniteTransition()
    val hour by transition.animateFloat(
        0f,
        FULL_TURN,
        infiniteRepeatable(tween(HOUR_TURN_MILLIS, easing = LinearEasing)),
    )
    val minute by transition.animateFloat(
        0f,
        FULL_TURN,
        infiniteRepeatable(tween(MINUTE_TURN_MILLIS, easing = LinearEasing)),
    )
    Canvas(Modifier.fillMaxSize()) {
        val unit = size.width / STATUS_BOX
        val inset = floor((size.width - CLOCK * unit) / 2) / unit
        inBox {
            val corner = Offset(inset, inset)
            val center = corner + Offset(CLOCK / 2, CLOCK / 2)
            drawCircle(color, radius = (CLOCK - CLOCK_LINE) / 2, center = center, style = Stroke(CLOCK_LINE))
            val hand = (CLOCK - CLOCK_LINE) / 2
            rotate(hour, pivot = center) {
                drawRect(color, corner + Offset(hand, CLOCK / 2), Size(CLOCK_LINE, CLOCK / 2 - CLOCK_LINE * 3))
            }
            rotate(minute, pivot = center) {
                drawRect(color, corner + Offset(hand, hand), Size(CLOCK / 2 - CLOCK_LINE, CLOCK_LINE))
            }
        }
    }
}

private inline fun DrawScope.inBox(block: DrawScope.() -> Unit) {
    val unit = size.width / STATUS_BOX
    scale(unit, unit, pivot = Offset.Zero, block)
}

// Workaround: CMP 1.12's Skia paint never passes StrokeJoin.Round on; round caps of lone segments join round.
private fun DrawScope.drawChecks(
    progress: Float,
    color: Color,
) {
    val stroke = Stroke(width = CHECK_LINE, cap = StrokeCap.Round)
    fun segment(
        from: Offset,
        to: Offset,
    ) = drawPath(
        Path().apply {
            moveTo(from.x, from.y)
            lineTo(to.x, to.y)
        },
        color,
        style = stroke,
    )
    val firstLegs = (progress * LEG_SPEED).coerceIn(0f, 1f)
    val secondLegs = ((progress - 1) * LEG_SPEED).coerceIn(0f, 1f)
    scale(pop(progress.coerceIn(0f, 1f)), pivot = FirstPivot) {
        if (firstLegs > 0f && firstLegs < 1f) {
            segment(FirstStart + ShortLeg * firstLegs, FirstStart)
        } else if (firstLegs >= 1f) {
            val corner = FirstStart + ShortLeg
            segment(corner + LongLeg * longLeg(progress.coerceAtMost(1f)), corner)
            segment(corner, FirstStart + ShortLeg * secondLegs)
        }
    }
    scale(pop((progress - 1).coerceIn(0f, 1f)), pivot = SecondPivot) {
        if (secondLegs > 0f && secondLegs < 1f) {
            segment(SecondStart + ShortLeg * secondLegs, SecondStart)
        } else if (secondLegs >= 1f) {
            val corner = SecondStart + ShortLeg
            segment(corner + LongLeg * longLeg((progress - 1).coerceAtLeast(0f)), corner)
            segment(corner, SecondStart)
        }
    }
}

private fun longLeg(progress: Float) = (progress - LONG_LEG_START) * LONG_LEG_SPEED

private fun pop(progress: Float) = POP - abs((POP - 1) * (progress - HALF) / HALF)

private fun DrawScope.drawFailed(
    fill: Color,
    content: Color,
) {
    val center = Offset(STATUS_BOX / 2, STATUS_BOX / 2)
    drawCircle(fill, radius = FAILED / 2, center = center)
    drawRoundRect(
        color = content,
        topLeft = center + Offset(-FAILED_LINE / 2, FAILED_BAR_TOP),
        size = Size(FAILED_LINE, FAILED_BAR),
        cornerRadius = CornerRadius(FAILED_LINE / 2),
    )
    drawCircle(content, radius = FAILED_LINE * FAILED_DOT, center = center + Offset(0f, FAILED_DOT_Y))
}

private val ChecksSpec = tween<Float>(TRANSITION_MILLIS, easing = LinearEasing)
private const val TRANSITION_MILLIS = 200
private const val HOUR_TURN_MILLIS = 6_000
private const val MINUTE_TURN_MILLIS = 1_000
private const val FULL_TURN = 360f
private const val SENT = 1f
private const val READ = 2f
private const val LEG_SPEED = 3f
private const val LONG_LEG_START = 0.33f
private const val LONG_LEG_SPEED = 1.5f
private const val POP = 1.2f
private const val HALF = 0.5f
private const val STATUS_BOX = 24f
private const val CLOCK = 11f
private const val CLOCK_LINE = 1f
private const val CHECK_LINE = 1.333f
private const val FAILED = 14f
private const val FAILED_LINE = 1.5f
private const val FAILED_BAR_TOP = -4f
private const val FAILED_BAR = 5.5f
private const val FAILED_DOT = 0.6f
private const val FAILED_DOT_Y = 3.5f
private val FirstStart = Offset(9f, 13f)
private val SecondStart = Offset(5f, 13f)
private val FirstPivot = Offset(16f, 13f)
private val SecondPivot = Offset(12f, 13f)
private val ShortLeg = Offset(3.5f, 3.5f)
private val LongLeg = Offset(7.5f - 1f / 3, -8f)
