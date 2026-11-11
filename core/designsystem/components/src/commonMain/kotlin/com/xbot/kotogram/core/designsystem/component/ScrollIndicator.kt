package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ScrollIndicator(
    state: LazyListState,
    modifier: Modifier = Modifier,
    style: ScrollIndicatorStyle = ScrollIndicatorStyle,
) {
    ScrollIndicator(rememberScrollIndicatorState(state), modifier, style)
}

@Composable
fun ScrollIndicator(
    state: ScrollState,
    modifier: Modifier = Modifier,
    style: ScrollIndicatorStyle = ScrollIndicatorStyle,
) {
    ScrollIndicator(rememberScrollIndicatorState(state), modifier, style)
}

@Composable
private fun ScrollIndicator(
    state: ScrollIndicatorState,
    modifier: Modifier = Modifier,
    style: ScrollIndicatorStyle = ScrollIndicatorStyle,
) {
    val indicatorStyle = rememberStyle(::ScrollIndicatorStyleScope, ScrollIndicatorStyle.Default then style)
    with(LocalScrollIndicatorOverride.current) {
        ScrollIndicatorOverrideScope(state, modifier, indicatorStyle).ScrollIndicator()
    }
}

internal interface ScrollIndicatorOverride {
    @Composable
    fun ScrollIndicatorOverrideScope.ScrollIndicator()
}

internal class ScrollIndicatorOverrideScope(
    val state: ScrollIndicatorState,
    val modifier: Modifier,
    val style: ScrollIndicatorStyleScope,
)

// Adapted from AOSP's View.awakenScrollBars: shown at once, faded after a rest, and four rests long when it appears.
internal object DefaultScrollIndicatorOverride : ScrollIndicatorOverride {
    @Composable
    override fun ScrollIndicatorOverrideScope.ScrollIndicator() {
        val awake by produceState(false, state, style) {
            var rests = INITIAL_RESTS
            snapshotFlow { state.isScrollInProgress }.collectLatest { scrolling ->
                value = true
                if (!scrolling) {
                    val rest = style.hideDelay * rests
                    rests = 1
                    delay(rest)
                    value = false
                }
            }
        }
        val shown = awake && state.isScrollable
        val alpha by animateFloatAsState(if (shown) 1f else 0f, if (shown) style.showSpec else style.hideSpec)
        context(style) {
            ScrollIndicatorBar(
                position = { state.positionFraction },
                size = { state.sizeFraction },
                modifier = modifier.graphicsLayer { this.alpha = alpha },
            )
        }
    }
}

// Adapted from Wear Compose Material 3's ScrollIndicator: its track, thumb and gaps, drawn straight along the whole edge.
@Composable
context(style: ScrollIndicatorStyleScope)
internal fun ScrollIndicatorBar(
    position: () -> Float,
    size: () -> Float,
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier
            .padding(end = style.edgePadding)
            .width(style.indicatorWidth)
            .drawBehind { drawIndicator(position(), size()) },
    )
}

// Adapted from AOSP's ScrollBarUtils.getThumbLength: as long as the part in view, and no shorter than the minimum.
context(style: ScrollIndicatorStyleScope)
internal fun Density.indicatorThumbLength(
    trackLength: Float,
    sizeFraction: Float,
): Float {
    val minLength = style.minIndicatorHeight.toPx().coerceAtMost(trackLength)
    return (trackLength * sizeFraction).coerceIn(minLength, trackLength)
}

context(style: ScrollIndicatorStyleScope)
private fun DrawScope.drawIndicator(
    position: Float,
    sizeFraction: Float,
) {
    val gapHeight = style.gapHeight.toPx()
    val gap = size.width + gapHeight
    val length = size.height + gapHeight
    val origin = -gapHeight / 2
    val thumb = indicatorThumbLength(size.height, sizeFraction) + gapHeight
    val start = position.coerceIn(0f, 1f) * (length - thumb)
    drawSegment(origin, start, gap, style.trackColor)
    drawSegment(origin + start, thumb, gap, style.indicatorColor)
    drawSegment(origin + start + thumb, length - start - thumb, gap, style.trackColor)
}

private fun DrawScope.drawSegment(
    start: Float,
    length: Float,
    gap: Float,
    color: Color,
) {
    val width = size.width
    val x = width / 2
    if (length <= gap) {
        if (length <= 0f) return
        val fraction = length / gap
        drawCircle(
            color = color.copy(alpha = color.alpha * fraction),
            radius = lerp(0f, width / 2, fraction),
            center = Offset(x, start + length / 2),
        )
    } else {
        drawLine(color, Offset(x, start + gap / 2), Offset(x, start + length - gap / 2), width, StrokeCap.Round)
    }
}

internal val LocalScrollIndicatorOverride: ProvidableCompositionLocal<ScrollIndicatorOverride> =
    staticCompositionLocalOf { DefaultScrollIndicatorOverride }

private const val INITIAL_RESTS = 4

@Suppress("MagicNumber")
fun interface ScrollIndicatorStyle : ComponentStyle<ScrollIndicatorStyleScope> {
    companion object : ScrollIndicatorStyle {
        override fun ScrollIndicatorStyleScope.applyStyle() {}

        val Default = ScrollIndicatorStyle {
            indicatorColor(theme.colors.onSurfaceVariant)
            trackColor(theme.colors.outlineVariant)
            indicatorWidth(4.dp)
            minIndicatorHeight(8.dp)
            gapHeight(3.dp)
            edgePadding(2.dp)
            showSpec(snap())
            hideSpec(tween(250, easing = LinearEasing))
            hideDelay(300.milliseconds)
        }
    }
}

infix fun ScrollIndicatorStyle.then(other: ScrollIndicatorStyle): ScrollIndicatorStyle = when {
    other === ScrollIndicatorStyle -> this

    this === ScrollIndicatorStyle -> other

    else -> ScrollIndicatorStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
class ScrollIndicatorStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<ScrollIndicatorStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var indicatorColor: Color = Color.Unspecified
        private set

    var trackColor: Color = Color.Unspecified
        private set

    var indicatorWidth: Dp = Dp.Unspecified
        private set

    var minIndicatorHeight: Dp = Dp.Unspecified
        private set

    var gapHeight: Dp = Dp.Unspecified
        private set

    var edgePadding: Dp = Dp.Unspecified
        private set

    var showSpec: AnimationSpec<Float> = snap()
        private set

    var hideSpec: AnimationSpec<Float> = snap()
        private set

    var hideDelay: Duration = Duration.ZERO
        private set

    fun indicatorColor(color: Color) {
        indicatorColor = color
    }

    fun trackColor(color: Color) {
        trackColor = color
    }

    fun indicatorWidth(width: Dp) {
        indicatorWidth = width
    }

    fun minIndicatorHeight(height: Dp) {
        minIndicatorHeight = height
    }

    fun gapHeight(height: Dp) {
        gapHeight = height
    }

    fun edgePadding(padding: Dp) {
        edgePadding = padding
    }

    fun showSpec(spec: AnimationSpec<Float>) {
        showSpec = spec
    }

    fun hideSpec(spec: AnimationSpec<Float>) {
        hideSpec = spec
    }

    fun hideDelay(delay: Duration) {
        hideDelay = delay
    }
}
