package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.extensions.glyphBounds
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.theme.platformRoundedFontFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

internal fun AnnotatedString.hidingSpoilers(): AnnotatedString {
    val spoilers = getStringAnnotations(SPOILER_ANNOTATION, 0, length)
    if (spoilers.isEmpty()) return this
    return buildAnnotatedString {
        append(this@hidingSpoilers)
        spoilers.forEach { addStyle(SpanStyle(color = Color.Transparent), it.start, it.end) }
    }
}

internal fun DrawScope.drawSpoilerDust(
    text: AnnotatedString,
    layout: TextLayoutResult,
    offset: Int,
    color: Color,
    time: Float,
) {
    val spoilers = text.getStringAnnotations(SPOILER_ANNOTATION, 0, text.length)
    if (spoilers.isEmpty()) return
    val visibleEnd = layout.getLineEnd(layout.lineCount - 1, visibleEnd = true)
    spoilers.forEach { range ->
        for (index in range.start + offset until (range.end + offset).coerceAtMost(visibleEnd)) {
            drawDust(layout.getBoundingBox(index), color, time, seed = index * DUST_SEED + range.end)
        }
    }
}

internal fun DrawScope.drawDust(
    area: Rect,
    color: Color,
    time: Float,
    seed: Int,
) {
    val random = Random(seed)
    val radius = DUST_RADIUS.dp.toPx()
    val step = DUST_STEP.dp.toPx()
    val drift = DUST_DRIFT.dp.toPx()
    var y = area.top + step / 2
    while (y < area.bottom) {
        var x = area.left + random.nextFloat() * step
        while (x < area.right) {
            val phase = random.nextFloat()
            val angle = random.nextFloat() * FULL_TURN
            val wave = sin(FULL_TURN * (time + phase))
            val twinkle = DUST_CENTER + DUST_CENTER * sin(FULL_TURN * (time * 2 + phase))
            drawCircle(
                color = color.copy(alpha = color.alpha * (DUST_MIN_ALPHA + twinkle * (1 - DUST_MIN_ALPHA))),
                radius = radius,
                center = Offset(x + cos(angle) * drift * wave, y + sin(angle) * drift * wave),
            )
            x += step
        }
        y += step
    }
}

private const val DUST_RADIUS = 0.6f
private const val DUST_STEP = 2.2f
private const val DUST_DRIFT = 0.8f
private const val DUST_MIN_ALPHA = 0.3f
private const val DUST_SEED = 31
private const val DUST_CENTER = 0.5f
private const val FULL_TURN = (2 * PI).toFloat()

@Composable
internal fun dustTime(): State<Float> = rememberInfiniteTransition().animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(DUST_PERIOD_MILLIS, easing = LinearEasing)),
)

internal fun AnnotatedString.hasSpoilers(): Boolean = getStringAnnotations(SPOILER_ANNOTATION, 0, length).isNotEmpty()

private const val DUST_PERIOD_MILLIS = 3_000
