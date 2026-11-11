package com.xbot.kotogram.core.designsystem.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.PreviewKotogram
import com.xbot.kotogram.core.designsystem.PreviewKotogramLaptop
import com.xbot.kotogram.core.designsystem.extensions.isDark
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.designsystem.theme.currentPlatform
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun TopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigation: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    scrollBehavior: TopBarScrollBehavior? = null,
    style: TopBarStyle = TopBarStyle,
) {
    val barStyle = rememberStyle(::TopBarStyleScope, TopBarStyle.Adaptive then style)
    with(LocalTopBarOverride.current) {
        TopBarOverrideScope(title, modifier, navigation, actions, windowInsets, scrollBehavior, barStyle).TopBar()
    }
}

@Stable
class TopBarScrollBehavior internal constructor(
    internal val haze: HazeState,
    private val content: ScrollableState,
) {
    internal val isContentUnderBar: Boolean
        get() = content.canScrollBackward
}

@Composable
fun rememberTopBarScrollBehavior(content: ScrollableState): TopBarScrollBehavior {
    val haze = rememberHazeState()
    return remember(haze, content) { TopBarScrollBehavior(haze, content) }
}

fun Modifier.topBarScrollSource(behavior: TopBarScrollBehavior): Modifier = hazeSource(behavior.haze)

internal interface TopBarOverride {
    @Composable
    fun TopBarOverrideScope.TopBar()
}

@Suppress("LongParameterList")
internal class TopBarOverrideScope(
    val title: @Composable () -> Unit,
    val modifier: Modifier,
    val navigation: @Composable (() -> Unit)?,
    val actions: @Composable RowScope.() -> Unit,
    val windowInsets: WindowInsets,
    val scrollBehavior: TopBarScrollBehavior?,
    val style: TopBarStyleScope,
)

internal object DefaultTopBarOverride : TopBarOverride {
    @Composable
    override fun TopBarOverrideScope.TopBar() {
        val containerColor = if (scrollBehavior == null) style.containerColor else Color.Transparent
        val colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = containerColor,
            navigationIconContentColor = style.contentColor,
            titleContentColor = style.contentColor,
            actionIconContentColor = style.contentColor,
        )
        val barTitle: @Composable () -> Unit = { ProvideTextStyle(style.titleTextStyle, title) }
        val barNavigation: @Composable () -> Unit = { navigation?.invoke() }
        val barActions: @Composable RowScope.() -> Unit = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(style.actionGap),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
        val insetTop = windowInsets.getTop(LocalDensity.current)
        Box(modifier) {
            scrollBehavior?.let { ScrollEdgeEffect(it, style, insetTop, Modifier.matchParentSize()) }
            if (style.centeredTitle) {
                CenterAlignedTopAppBar(
                    title = barTitle,
                    navigationIcon = barNavigation,
                    actions = barActions,
                    expandedHeight = style.minHeight,
                    windowInsets = windowInsets,
                    colors = colors,
                )
            } else {
                TopAppBar(
                    title = barTitle,
                    navigationIcon = barNavigation,
                    actions = barActions,
                    expandedHeight = style.minHeight,
                    windowInsets = windowInsets,
                    colors = colors,
                )
            }
        }
    }
}

internal object AppleTopBarOverride : TopBarOverride {
    @Composable
    override fun TopBarOverrideScope.TopBar() {
        val measurePolicy = remember(style) { TopBarMeasurePolicy(style) }
        val insetTop = windowInsets.getTop(LocalDensity.current)
        Box(modifier) {
            scrollBehavior?.let { ScrollEdgeEffect(it, style, insetTop, Modifier.matchParentSize()) }
            val touchTarget = if (style.contentHeight.isSpecified) {
                style.contentHeight
            } else {
                LocalMinimumInteractiveComponentSize.current
            }
            CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides touchTarget,
                LocalTextStyle provides style.titleTextStyle,
            ) {
                Layout(
                    content = {
                        Box(Modifier.layoutId(TopBarSlot.Navigation)) { navigation?.invoke() }
                        Box(Modifier.layoutId(TopBarSlot.Title)) { title() }
                        Row(
                            modifier = Modifier.layoutId(TopBarSlot.Actions),
                            horizontalArrangement = Arrangement.spacedBy(style.actionGap),
                            verticalAlignment = Alignment.CenterVertically,
                            content = actions,
                        )
                    },
                    modifier = Modifier.windowInsetsPadding(windowInsets),
                    measurePolicy = measurePolicy,
                )
            }
        }
    }
}

// Adapted from UIKit's scroll edge effect under a bar, read in iOS 27.2's layers: its blur, mask, tint and colour matrix.
@Composable
private fun ScrollEdgeEffect(
    behavior: TopBarScrollBehavior,
    style: TopBarStyleScope,
    insetTop: Int,
    modifier: Modifier = Modifier,
) {
    val overlapped by remember(behavior) { derivedStateOf { behavior.isContentUnderBar } }
    val alpha = animateFloatAsState(if (overlapped) 1f else 0f, style.edgeFadeSpec)
    if (!overlapped && alpha.value == 0f) return
    val falloffStart = insetTop - with(LocalDensity.current) { style.edgeBlurLead.roundToPx() }
    val blur = remember(style, falloffStart) {
        HazeBlurStyle {
            blurEnabled(true)
            blurRadius(style.edgeBlurRadius)
            noiseFactor(0f)
            backgroundColor(style.containerColor)
            progressive(
                HazeProgressive.verticalGradient(
                    easing = EdgeBlurFalloff,
                    startY = falloffStart.coerceAtLeast(0).toFloat(),
                    startIntensity = 1f,
                    endIntensity = EDGE_BLUR_END,
                ),
            )
        }
    }
    Box(
        modifier
            .graphicsLayer { this.alpha = alpha.value }
            .drawWithContent {
                drawContent()
                drawRect(style.edgeHairlineColor, Offset(0f, size.height - 1f), Size(size.width, 1f))
            },
    ) {
        // Hack: Haze's progressive mask would fade a tint of its own with the blur, and UIKit's tint is even.
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { colorFilter = EdgeColorFilter }
                .hazeBlur(HazeInput.Sources(behavior.haze), blur)
                .drawWithContent {
                    drawContent()
                    drawRect(style.containerColor, alpha = style.edgeTintAlpha)
                },
        )
    }
}

private enum class TopBarSlot {
    Navigation,
    Title,
    Actions,
}

private class TopBarMeasurePolicy(
    private val style: TopBarStyleScope,
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val centered = style.centeredTitle
        val width = constraints.maxWidth
        val padding = style.horizontalPadding.roundToPx()
        val gap = style.titleGap.roundToPx()
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val navigation = measurables.first { it.layoutId == TopBarSlot.Navigation }.measure(loose)
        val actions = measurables.first { it.layoutId == TopBarSlot.Actions }.measure(loose)
        val navigationEnd = if (navigation.width > 0) padding + navigation.width + gap else padding
        val actionsStart = if (actions.width > 0) width - padding - actions.width - gap else width - padding
        val titleWidth = if (centered) {
            width - 2 * maxOf(navigationEnd, width - actionsStart)
        } else {
            actionsStart - navigationEnd
        }
        val title = measurables.first { it.layoutId == TopBarSlot.Title }
            .measure(loose.copy(maxWidth = titleWidth.coerceAtLeast(0)))
        val height = constraints.constrainHeight(
            maxOf(style.minHeight.roundToPx(), navigation.height, actions.height, title.height),
        )
        val band = if (style.contentHeight.isSpecified) minOf(style.contentHeight.roundToPx(), height) else height
        return layout(width, height) {
            navigation.placeRelative(padding, (band - navigation.height) / 2)
            actions.placeRelative(width - padding - actions.width, (band - actions.height) / 2)
            val titleStart = if (centered) (width - title.width) / 2 else navigationEnd
            title.placeRelative(titleStart, (band - title.height) / 2)
        }
    }
}

internal val LocalTopBarOverride: ProvidableCompositionLocal<TopBarOverride> =
    staticCompositionLocalOf { DefaultTopBarOverride }

@PreviewKotogram
@Composable
private fun TopBarPreview() {
    TopBar(
        title = { Text("Country", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigation = { IconButton(KotogramIcons.Close, contentDescription = null, onClick = {}) },
        actions = { IconButton(KotogramIcons.Search, contentDescription = null, onClick = {}) },
    )
}

@PreviewKotogramLaptop
@Composable
private fun TopBarLaptopPreview() = TopBarPreview()

private const val EDGE_BLUR_END = 0.57f

@Suppress("MagicNumber")
private val EdgeColorFilter = ColorFilter.colorMatrix(
    ColorMatrix(
        floatArrayOf(
            1.1969f, -0.1789f, -0.0180f, 0f, 7.65f,
            -0.0531f, 1.0712f, -0.0181f, 0f, 7.65f,
            -0.0532f, -0.1787f, 1.2320f, 0f, 7.65f,
            0f, 0f, 0f, 1f, 0f,
        ),
    ),
)

@Suppress("MagicNumber")
private val EdgeBlurMask = floatArrayOf(1f, 0.87f, 0.75f, 0.69f, 0.68f, 0.66f, 0.63f, 0.59f, 0.57f, 0.57f, 0.57f)

private val EdgeBlurFalloff = Easing { fraction ->
    val position = fraction.coerceIn(0f, 1f) * EdgeBlurMask.lastIndex
    val index = position.toInt().coerceAtMost(EdgeBlurMask.lastIndex - 1)
    val value = EdgeBlurMask[index] + (EdgeBlurMask[index + 1] - EdgeBlurMask[index]) * (position - index)
    (EdgeBlurMask.first() - value) / (EdgeBlurMask.first() - EdgeBlurMask.last())
}

@Suppress("MagicNumber")
fun interface TopBarStyle : ComponentStyle<TopBarStyleScope> {
    companion object : TopBarStyle {
        override fun TopBarStyleScope.applyStyle() {}

        val Default = TopBarStyle {
            val apple = currentPlatform.isApple
            centeredTitle(apple)
            minHeight(if (apple) 54.dp else 64.dp)
            if (apple) contentHeight(44.dp)
            horizontalPadding(if (apple) 20.dp else 4.dp)
            titleGap(8.dp)
            actionGap(8.dp)
            containerColor(theme.colors.surface)
            contentColor(theme.colors.onSurface)
            titleTextStyle(
                if (apple) {
                    theme.typography.bodyLarge.merge(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.43).sp,
                        color = theme.colors.onSurface,
                    )
                } else {
                    theme.typography.titleLarge.merge(color = theme.colors.onSurface)
                },
            )
            edgeBlurRadius(15.dp)
            edgeBlurLead(16.dp)
            edgeTintAlpha(if (theme.colors.surface.isDark()) 0.6f else 0.5f)
            edgeHairlineColor(theme.colors.onSurface.copy(alpha = 0.1f))
            edgeFadeSpec(tween(200))
        }

        val Adaptive = Default then TopBarStyle {
            laptop {
                minHeight(44.dp)
                contentHeight(Dp.Unspecified)
                if (currentPlatform.isApple) horizontalPadding(10.dp)
                actionGap(4.dp)
                titleTextStyle(theme.typography.titleMedium.merge(color = theme.colors.onSurface))
            }
        }
    }
}

infix fun TopBarStyle.then(other: TopBarStyle): TopBarStyle = when {
    other === TopBarStyle -> this

    this === TopBarStyle -> other

    else -> TopBarStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
class TopBarStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<TopBarStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var centeredTitle: Boolean = false
        private set

    var minHeight: Dp = Dp.Unspecified
        private set

    var contentHeight: Dp = Dp.Unspecified
        private set

    var horizontalPadding: Dp = Dp.Unspecified
        private set

    var titleGap: Dp = Dp.Unspecified
        private set

    var actionGap: Dp = Dp.Unspecified
        private set

    var containerColor: Color = Color.Unspecified
        private set

    var contentColor: Color = Color.Unspecified
        private set

    var titleTextStyle: TextStyle = TextStyle.Default
        private set

    var edgeBlurRadius: Dp = Dp.Unspecified
        private set

    var edgeBlurLead: Dp = Dp.Unspecified
        private set

    var edgeTintAlpha: Float = 0f
        private set

    var edgeHairlineColor: Color = Color.Unspecified
        private set

    var edgeFadeSpec: AnimationSpec<Float> = snap()
        private set

    fun centeredTitle(centered: Boolean) {
        centeredTitle = centered
    }

    fun minHeight(height: Dp) {
        minHeight = height
    }

    fun contentHeight(height: Dp) {
        contentHeight = height
    }

    fun horizontalPadding(padding: Dp) {
        horizontalPadding = padding
    }

    fun titleGap(gap: Dp) {
        titleGap = gap
    }

    fun actionGap(gap: Dp) {
        actionGap = gap
    }

    fun containerColor(color: Color) {
        containerColor = color
    }

    fun contentColor(color: Color) {
        contentColor = color
    }

    fun titleTextStyle(style: TextStyle) {
        titleTextStyle = style
    }

    fun edgeBlurRadius(radius: Dp) {
        edgeBlurRadius = radius
    }

    fun edgeBlurLead(lead: Dp) {
        edgeBlurLead = lead
    }

    fun edgeTintAlpha(alpha: Float) {
        edgeTintAlpha = alpha
    }

    fun edgeHairlineColor(color: Color) {
        edgeHairlineColor = color
    }

    fun edgeFadeSpec(spec: AnimationSpec<Float>) {
        edgeFadeSpec = spec
    }
}
