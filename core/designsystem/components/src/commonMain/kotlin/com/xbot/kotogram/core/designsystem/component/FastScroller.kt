package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class FastScrollerSection(
    val title: String,
    val index: Int,
)

@Composable
fun FastScroller(
    state: LazyListState,
    sections: List<FastScrollerSection>,
    modifier: Modifier = Modifier,
    style: FastScrollerStyle = FastScrollerStyle,
) {
    val scrollerStyle = rememberStyle(::FastScrollerStyleScope, FastScrollerStyle.Default then style)
    with(LocalFastScrollerOverride.current) {
        FastScrollerOverrideScope(state, sections, modifier, scrollerStyle).FastScroller()
    }
}

internal interface FastScrollerOverride {
    @Composable
    fun FastScrollerOverrideScope.FastScroller()
}

internal class FastScrollerOverrideScope(
    val state: LazyListState,
    val sections: List<FastScrollerSection>,
    val modifier: Modifier,
    val style: FastScrollerStyleScope,
)

internal object DefaultFastScrollerOverride : FastScrollerOverride {
    @Composable
    override fun FastScrollerOverrideScope.FastScroller() {
        context(style) { ScrollThumb(state, sections, modifier) }
    }
}

internal val LocalFastScrollerOverride: ProvidableCompositionLocal<FastScrollerOverride> =
    staticCompositionLocalOf { DefaultFastScrollerOverride }

// Adapted from AOSP's AccelerateDecelerateInterpolator, the default of the platform's animators.
private val AccelerateDecelerate = Easing { ((cos((it + 1) * PI) / 2) + 0.5).toFloat() }

@Suppress("MagicNumber")
fun interface FastScrollerStyle : ComponentStyle<FastScrollerStyleScope> {
    companion object : FastScrollerStyle {
        override fun FastScrollerStyleScope.applyStyle() {}

        val Default = FastScrollerStyle {
            indexColor(theme.colors.primary)
            touchTarget(48.dp)
            thumbWidth(8.dp)
            minThumbHeight(48.dp)
            activeThumbColor(theme.colors.primary)
            bubbleMinSize(88.dp)
            bubblePadding(22.dp)
            bubbleShape(RoundedCornerShape(topStart = 44.dp, topEnd = 44.dp, bottomEnd = 0.dp, bottomStart = 44.dp))
            bubbleColor(theme.colors.primary)
            bubbleTextStyle(theme.typography.displayMedium.merge(color = theme.colors.onPrimary))
            bubbleGap(16.dp)
            showSpec(tween(150, easing = AccelerateDecelerate))
            hideSpec(tween(300, easing = AccelerateDecelerate))
            sectionSpec(tween(50, easing = AccelerateDecelerate))
            hideDelay(1500.milliseconds)
            minPages(4)
        }
    }
}

infix fun FastScrollerStyle.then(other: FastScrollerStyle): FastScrollerStyle = when {
    other === FastScrollerStyle -> this

    this === FastScrollerStyle -> other

    else -> FastScrollerStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
class FastScrollerStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<FastScrollerStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var indexColor: Color = Color.Unspecified
        private set

    var touchTarget: Dp = Dp.Unspecified
        private set

    var thumbWidth: Dp = Dp.Unspecified
        private set

    var minThumbHeight: Dp = Dp.Unspecified
        private set

    var activeThumbColor: Color = Color.Unspecified
        private set

    var bubbleMinSize: Dp = Dp.Unspecified
        private set

    var bubblePadding: Dp = Dp.Unspecified
        private set

    var bubbleShape: Shape = RectangleShape
        private set

    var bubbleColor: Color = Color.Unspecified
        private set

    var bubbleTextStyle: TextStyle = TextStyle.Default
        private set

    var bubbleGap: Dp = Dp.Unspecified
        private set

    var showSpec: AnimationSpec<Float> = snap()
        private set

    var hideSpec: AnimationSpec<Float> = snap()
        private set

    var sectionSpec: FiniteAnimationSpec<Float> = snap()
        private set

    var hideDelay: Duration = Duration.ZERO
        private set

    var minPages: Int = 0
        private set

    fun indexColor(color: Color) {
        indexColor = color
    }

    fun touchTarget(size: Dp) {
        touchTarget = size
    }

    fun thumbWidth(width: Dp) {
        thumbWidth = width
    }

    fun minThumbHeight(height: Dp) {
        minThumbHeight = height
    }

    fun activeThumbColor(color: Color) {
        activeThumbColor = color
    }

    fun bubbleMinSize(size: Dp) {
        bubbleMinSize = size
    }

    fun bubblePadding(padding: Dp) {
        bubblePadding = padding
    }

    fun bubbleShape(shape: Shape) {
        bubbleShape = shape
    }

    fun bubbleColor(color: Color) {
        bubbleColor = color
    }

    fun bubbleTextStyle(style: TextStyle) {
        bubbleTextStyle = style
    }

    fun bubbleGap(gap: Dp) {
        bubbleGap = gap
    }

    fun showSpec(spec: AnimationSpec<Float>) {
        showSpec = spec
    }

    fun hideSpec(spec: AnimationSpec<Float>) {
        hideSpec = spec
    }

    fun sectionSpec(spec: FiniteAnimationSpec<Float>) {
        sectionSpec = spec
    }

    fun hideDelay(delay: Duration) {
        hideDelay = delay
    }

    fun minPages(pages: Int) {
        minPages = pages
    }
}
