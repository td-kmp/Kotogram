package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.extensions.glyphBounds
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.theme.platformRoundedFontFamily
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
internal fun ChatListItemScope.TopicsLine(
    topics: List<ForumTopic>,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier,
        content = { topics.forEach { TopicChip(it) } },
    ) { measurables, constraints ->
        var remaining = constraints.maxWidth
        val placeables = mutableListOf<Placeable>()
        for (measurable in measurables) {
            if (remaining <= TopicMinWidth.roundToPx()) break
            val placeable = measurable.measure(Constraints(maxWidth = remaining))
            placeables += placeable
            remaining -= placeable.width + TopicSpacing.roundToPx()
        }
        val height = placeables.maxOfOrNull { it.height } ?: 0
        val start = -TopicLineStart.roundToPx()
        val firstEnd = placeables.firstOrNull()?.let { start + it.width - TopicTitleInset.roundToPx() } ?: 0
        val baseline = placeables.firstOrNull()?.baselineOr(0) ?: 0
        val width = placeables.sumOf { it.width } + TopicSpacing.roundToPx() * (placeables.size - 1).coerceAtLeast(0)
        layout(
            width.coerceAtMost(constraints.maxWidth),
            height,
            mapOf(
                FirstBaseline to baseline,
                FirstTopicEnd to firstEnd,
            ),
        ) {
            var x = start
            placeables.forEach {
                it.place(x, 0)
                x += it.width + TopicSpacing.roundToPx()
            }
        }
    }
}

@Composable
private fun ChatListItemScope.TopicChip(topic: ForumTopic) {
    val titleStyle = if (topic.unread) texts.author else texts.text
    Layout(
        content = {
            Text(topic.title, style = titleStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            when (val icon = topic.icon) {
                TopicIcon.General -> Icon(
                    imageVector = KotogramIcons.GeneralTopic,
                    contentDescription = null,
                    modifier = Modifier.size(TopicIconSize),
                    tint = style.generalTopicColor,
                )

                is TopicIcon.Bubble -> TopicBubble(topic.title, icon.color, Modifier.size(TopicIconSize))

                is TopicIcon.Emoji -> Box(Modifier.size(TopicIconSize), Alignment.Center) { icon.content() }

                null -> Unit
            }
        },
    ) { measurables, constraints ->
        val iconMeasurable = measurables.getOrNull(1)
        val iconWidth = if (iconMeasurable !=
            null
        ) TopicIconSize.roundToPx() + TopicIconGap.roundToPx() else TopicNoIcon.roundToPx()
        val title = measurables[0].measure(Constraints(maxWidth = (constraints.maxWidth - iconWidth).coerceAtLeast(0)))
        val icon = iconMeasurable?.measure(Constraints())
        val baseline = title.baselineOr(title.height)
        val inset = TopicTitleInset.roundToPx()
        val titleWidth = ceil(title.width.toDp().value).dp.roundToPx()
        layout(iconWidth + titleWidth + inset * 2, title.height, mapOf(FirstBaseline to baseline)) {
            title.place(iconWidth + inset, 0)
            icon?.place(0, baseline - (TOPIC_ICON_LIFT * texts.text.fontSize.toPx()).roundToInt() - icon.height / 2)
        }
    }
}

private val TopicIconSize = 18.dp

private val TopicIconGap = 2.dp

private val TopicNoIcon = 1.dp

internal val TopicTitleInset = 1.dp

private val TopicLineStart = 2.dp

private val TopicSpacing = 4.dp

private val TopicMinWidth = 34.dp

private const val TOPIC_ICON_LIFT = 0.35f

@Composable
private fun ChatListItemScope.TopicBubble(
    title: String,
    color: Int,
    modifier: Modifier = Modifier,
) {
    val (fill, stroke) = TopicColors[color] ?: DefaultTopicColors
    val measurer = rememberTextMeasurer()
    val family = platformRoundedFontFamily ?: texts.title.fontFamily
    val letter = title.firstCodePointString()
    Spacer(
        modifier.drawWithCache {
            val scale = size.width / TOPIC_CANVAS
            val path = TopicBubblePath
            val fillBrush = Brush.verticalGradient(fill.map(::Color), 0f, TOPIC_CANVAS)
            val strokeBrush = Brush.verticalGradient(stroke.map(::Color), 0f, TOPIC_CANVAS)
            val pixel = 1f / density
            val text = measurer.measure(
                letter,
                TextStyle(
                    color = Color.White,
                    fontSize = (TOPIC_LETTER * scale).roundToInt().toSp(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = family,
                ),
            )
            val ink = text.glyphBounds()
            val rise = floor(size.height * TOPIC_LETTER_RISE)
            val baseline = size.height - floor(ink.bottom + (size.height - ink.height) / 2) - rise
            val topLeft = Offset(
                floor((size.width - ink.width) / 2 - ink.left) + (1 - pixel) * density,
                baseline - text.firstBaseline,
            )
            onDrawBehind {
                withTransform({
                    scale(scale, scale, pivot = Offset.Zero)
                    translate(TOPIC_SHIFT - pixel, TOPIC_SHIFT - pixel)
                }) {
                    drawPath(path, fillBrush)
                    drawPath(path, strokeBrush, style = Stroke(width = 1f + pixel))
                }
                drawText(text, topLeft = topLeft)
            }
        },
    )
}

private fun String.firstCodePointString(): String {
    if (isEmpty()) return ""
    val high = this[0]
    return if (high.isHighSurrogate() && length > 1) substring(0, 2) else substring(0, 1)
}

private val TopicBubblePath: Path = PathParser().parsePathString(
    "M24.1835,4.71703 C21.7304,2.42169 18.2984,0.995605 14.5,0.995605 C7.04416,0.995605 1.0,6.49029 1.0,13.2683 " +
        "C1.0,17.1341 2.80572,20.3028 5.87839,22.5523 C6.27132,22.84 6.63324,24.4385 5.75738,25.7811 " +
        "C5.39922,26.3301 5.00492,26.7573 4.70138,27.0861 C4.26262,27.5614 4.01347,27.8313 4.33716,27.967 " +
        "C4.67478,28.1086 6.66968,28.1787 8.10952,27.3712 C9.23649,26.7392 9.91903,26.1087 10.3787,25.6842 " +
        "C10.7588,25.3331 10.9864,25.1228 11.187,25.1688 C11.9059,25.3337 12.6478,25.4461 13.4075,25.5015 " +
        "C13.4178,25.5022 13.4282,25.503 13.4386,25.5037 C13.7888,25.5284 14.1428,25.5411 14.5,25.5411 " +
        "C21.9558,25.5411 28.0,20.0464 28.0,13.2683 C28.0,9.94336 26.5455,6.92722 24.1835,4.71703 Z",
).toPath()

@Suppress("MagicNumber")
private val TopicColors: Map<Int, Pair<List<Long>, List<Long>>> = mapOf(
    0x6FB9F0 to (listOf(0xFF6FB9F0, 0xFF0261E4) to listOf(0xFF026CB5, 0xFF064BB7)),
    0xFFD67E to (listOf(0xFFFFD67E, 0xFFFC8601) to listOf(0xFFDA9400, 0xFFFA5F00)),
    0xCB86DB to (listOf(0xFFCB86DB, 0xFF9338AF) to listOf(0xFF812E98, 0xFF6F2B87)),
    0x8EEE98 to (listOf(0xFF8EEE98, 0xFF02B504) to listOf(0xFF02A01B, 0xFF009716)),
    0xFF93B2 to (listOf(0xFFFF93B2, 0xFFE23264) to listOf(0xFFFC447A, 0xFFC80C46)),
    0xFB6F5F to (listOf(0xFFFB6F5F, 0xFFD72615) to listOf(0xFFDC1908, 0xFFB61506)),
)

private val DefaultTopicColors = TopicColors.getValue(0x6FB9F0)

private const val TOPIC_CANVAS = 32f

private const val TOPIC_SHIFT = 2f

private const val TOPIC_LETTER = 15f

private const val TOPIC_LETTER_RISE = 0.05f
