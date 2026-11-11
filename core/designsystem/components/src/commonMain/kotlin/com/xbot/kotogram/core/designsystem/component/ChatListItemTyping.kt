@file:Suppress("MatchingDeclarationName")

package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow

enum class TypingAction { Typing, RecordingVoice, RecordingVideo, Uploading, PlayingGame, ChoosingSticker }

@Composable
fun ChatListItemScope.Typing(
    text: String,
    action: TypingAction,
    modifier: Modifier = Modifier,
) {
    val look = TypingLooks.getValue(action)
    val transition = rememberInfiniteTransition()
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(look.durationMillis, easing = look.easing), RepeatMode.Restart),
    )
    val color = texts.text.color.copy(alpha = texts.text.color.alpha * INDICATOR_ALPHA)
    Layout(
        modifier = modifier,
        content = {
            Text(text, style = texts.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Canvas(Modifier.size(IndicatorWidth, IndicatorHeight)) {
                when (action) {
                    TypingAction.Typing -> drawTypingDots(progress, color)
                    TypingAction.RecordingVoice -> drawVoiceWaves(progress, color)
                    TypingAction.RecordingVideo -> drawVideoDot(progress, color)
                    TypingAction.Uploading -> drawUploadBar(progress, color)
                    TypingAction.PlayingGame -> drawGame(progress, color)
                    TypingAction.ChoosingSticker -> drawEyes(progress, color)
                }
            }
        },
    ) { measurables, constraints ->
        val indicator = measurables[1].measure(Constraints())
        val shift = IndicatorShift.roundToPx()
        val textStart = indicator.width - shift
        val textPlaceable = measurables[0].measure(
            Constraints(maxWidth = (constraints.maxWidth - textStart).coerceAtLeast(0)),
        )
        val baseline = textPlaceable[FirstBaseline].takeIf { it != AlignmentLine.Unspecified } ?: 0
        val glyphHeight = textPlaceable.height.coerceAtMost(GlyphLine.roundToPx())
        layout(textStart + textPlaceable.width, textPlaceable.height, mapOf(FirstBaseline to baseline)) {
            textPlaceable.place(textStart, 0)
            val y = if (action == TypingAction.Typing) floor((glyphHeight - indicator.height) / 2f).toInt() else 0
            indicator.place(-shift, y)
        }
    }
}

private class TypingLook(
    val durationMillis: Int,
    val easing: Easing,
)

private val EaseInOut = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

@Suppress("MagicNumber")
private val TypingLooks = mapOf(
    TypingAction.Typing to TypingLook(700, LinearEasing),
    TypingAction.RecordingVoice to TypingLook(700, LinearEasing),
    TypingAction.RecordingVideo to TypingLook(900, EaseInOut),
    TypingAction.Uploading to TypingLook(1750, EaseInOut),
    TypingAction.PlayingGame to TypingLook(900, LinearEasing),
    TypingAction.ChoosingSticker to TypingLook(2000, LinearEasing),
)

@Suppress("MagicNumber")
private fun dotDiameter(
    value: Float,
    timeOffset: Float,
): Float {
    var clamped = value + timeOffset
    if (clamped > 1f) clamped -= floor(clamped)
    return when {
        clamped < 0.4f -> lerp(MIN_DOT, MAX_DOT, clamped / 0.4f)
        clamped < 0.8f -> lerp(MAX_DOT, MIN_DOT, (clamped - 0.4f) / 0.4f)
        else -> MIN_DOT
    }
}

private fun lerp(
    from: Float,
    to: Float,
    value: Float,
) = (1 - value) * from + value * to

@Suppress("MagicNumber")
private fun DrawScope.drawTypingDots(
    progress: Float,
    color: Color,
) {
    val unit = 1.dp.toPx()
    listOf(0.4f, 0.2f, 0f).forEachIndexed { index, offset ->
        val grow = (dotDiameter(progress, offset).coerceAtLeast(MIN_DOT) - MIN_DOT) / (MAX_DOT - MIN_DOT) * DOT_GROWTH
        val alpha = (grow * (1 - DOT_MIN_ALPHA) + DOT_MIN_ALPHA) * color.alpha
        val diameter = MIN_DOT + grow
        drawCircle(
            color = color.copy(alpha = alpha.coerceIn(0f, 1f)),
            radius = diameter / 2 * unit,
            center = Offset((DOTS_LEFT + DOTS_DISTANCE * index) * unit, DOTS_TOP * unit),
        )
    }
}

private fun DrawScope.drawVoiceWaves(
    progress: Float,
    color: Color,
) {
    val unit = 1.dp.toPx()
    val origin = Offset(WAVES_LEFT * unit, size.height / 2 + unit)
    val angle = WAVES_ANGLE
    listOf(0f to WAVES_FIRST_POWER, 1f to WAVES_POWER, 2f to WAVES_POWER).forEach { (step, power) ->
        val radius = progress * WAVES_DELTA + step * WAVES_DELTA
        val alpha = 1 - cos(radius / (3 * WAVES_DELTA) * PI).pow(power).toFloat()
        if (radius <= 0f) return@forEach
        drawArc(
            color = color.copy(alpha = color.alpha * alpha.coerceIn(0f, 1f)),
            startAngle = -angle,
            sweepAngle = angle * 2,
            useCenter = false,
            topLeft = Offset(origin.x - radius * unit, origin.y - radius * unit),
            size = Size(radius * 2 * unit, radius * 2 * unit),
            style = Stroke(width = 2 * unit, cap = StrokeCap.Round),
        )
    }
}

private fun DrawScope.drawVideoDot(
    progress: Float,
    color: Color,
) {
    val unit = 1.dp.toPx()
    val phase = if (progress < 0.5f) progress / 0.5f else (1 - progress) / 0.5f
    val radius = (VIDEO_RADIUS - phase * VIDEO_SHRINK) * unit
    drawCircle(
        color.copy(alpha = color.alpha * (1 - phase * VIDEO_FADE)),
        radius,
        Offset(VIDEO_X * unit, VIDEO_Y * unit),
    )
}

private fun DrawScope.drawUploadBar(
    progress: Float,
    color: Color,
) {
    val unit = 1.dp.toPx()
    val origin = Offset((UPLOAD_X + 1 / density) * unit, UPLOAD_Y * unit)
    val bar = Size(UPLOAD_WIDTH * unit, UPLOAD_HEIGHT * unit)
    val corner = CornerRadius(UPLOAD_CORNER * unit)
    drawRoundRect(color.copy(alpha = color.alpha * UPLOAD_TRACK_ALPHA), origin, bar, corner)
    val track = Path().apply { addRoundRect(RoundRect(Rect(origin, bar), corner)) }
    clipPath(track) {
        drawRoundRect(color, origin + Offset(-bar.width + lerp(0f, bar.width * 2, progress), 0f), bar, corner)
    }
}

private fun DrawScope.drawGame(
    progress: Float,
    color: Color,
) {
    val unit = 1.dp.toPx()
    val dotsColor = color.copy(alpha = color.alpha * GAME_DOTS_ALPHA)
    val y = size.height / 2 + unit
    val originX = (size.width / unit - GAME_DISTANCE * 2) / 2 + GAME_DISTANCE
    val dotsProgress = ((progress * 100).toInt() % 50) / 50f
    val dotsX = 1.5f + originX - GAME_DISTANCE * dotsProgress
    drawCircle(dotsColor, unit, Offset(dotsX * unit, y))
    drawCircle(dotsColor, unit, Offset((dotsX + GAME_DISTANCE) * unit, y))
    drawCircle(
        dotsColor.copy(alpha = dotsColor.alpha * dotsProgress),
        unit,
        Offset((dotsX + GAME_DISTANCE * 2) * unit, y),
    )
    val closing = (progress * 4).toInt() % 2 == 1
    var bite = ((progress * 100).toInt() % 25) / 25f
    if (closing) bite = 1 - bite
    val mouth = GAME_MOUTH * bite
    val center = Offset((GAME_RADIUS + GAME_LEFT) * unit, y)
    val radius = GAME_RADIUS * unit
    drawArc(
        color = color,
        startAngle = mouth,
        sweepAngle = 360f - mouth * 2,
        useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
    )
}

@Suppress("MagicNumber")
private fun DrawScope.drawEyes(
    progress: Float,
    color: Color,
) {
    val unit = 1.dp.toPx()
    var height = progress * 4
    height = when {
        height > 3f -> 4f - height
        height > 2f -> (height - 2f).let { it * it }
        height > 1f -> 2f - height
        else -> height * height
    }
    var pupil = progress * 4
    if (pupil > 2f) pupil = 3f - pupil
    pupil = pupil.coerceIn(0f, 1f).let { it * it }
    var position = progress * 2
    if (position > 1f) position = 2f - position
    val eyeHeight = EYE_HEIGHT - 2 * height
    val eyeOffset = -1f + position * 2
    val centerY = size.height / 2
    val left = Offset(size.width / 2 - (EYE_WIDTH + 1 - eyeOffset) * unit, centerY)
    val right = Offset(size.width / 2 + (1 + eyeOffset) * unit, centerY)
    val pupilX = (-1f + pupil * 2) * EYE_WIDTH / 4
    listOf(left, right).forEach { eye ->
        drawOval(
            color = color,
            topLeft = Offset(eye.x - EYE_WIDTH / 2 * unit, eye.y - eyeHeight / 2 * unit),
            size = Size(EYE_WIDTH * unit, eyeHeight * unit),
            style = Stroke(width = unit),
        )
        drawCircle(color, PUPIL / 2 * unit, Offset(eye.x + pupilX * unit, eye.y))
    }
}

private val IndicatorWidth = 24.dp
private val IndicatorHeight = 16.dp
private val IndicatorShift = 3.dp
private val GlyphLine = 18.dp
private const val INDICATOR_ALPHA = 0.85f
private const val MIN_DOT = 3f
private const val MAX_DOT = 4.5f
private const val DOT_GROWTH = 1.5f
private const val DOT_MIN_ALPHA = 0.75f
private const val DOTS_LEFT = 6f
private const val DOTS_TOP = 9f
private const val DOTS_DISTANCE = 5.5f
private const val WAVES_LEFT = 3f
private const val WAVES_DELTA = 5f
private const val WAVES_ANGLE = 18f
private const val WAVES_FIRST_POWER = 50.0
private const val WAVES_POWER = 10.0
private const val VIDEO_RADIUS = 3.5f
private const val VIDEO_SHRINK = 0.66f
private const val VIDEO_FADE = 0.6f
private const val VIDEO_X = 16f
private const val VIDEO_Y = 9f
private const val UPLOAD_X = 4f
private const val UPLOAD_Y = 7f
private const val UPLOAD_WIDTH = 13f
private const val UPLOAD_HEIGHT = 4f
private const val UPLOAD_CORNER = 1.25f
private const val UPLOAD_TRACK_ALPHA = 0.3f
private const val GAME_DOTS_ALPHA = 0.5f
private const val GAME_DISTANCE = 4f
private const val GAME_RADIUS = 3.5f
private const val GAME_LEFT = 4.5f
private const val GAME_MOUTH = 42f
private const val EYE_WIDTH = 6f
private const val EYE_HEIGHT = 11f
private const val PUPIL = 4f
