package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramLaptopPreview
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.designsystem.theme.currentPlatform

@Composable
fun TopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigation: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    style: TopBarStyle = TopBarStyle
) {
    @Suppress("NAME_SHADOWING")
    val style = rememberStyle(::TopBarStyleScope, TopBarStyle.Adaptive then style)
    val measurePolicy = remember(style) { TopBarMeasurePolicy(style) }
    Layout(
        content = {
            Box(Modifier.layoutId(TopBarSlot.Navigation)) { navigation?.invoke() }
            Box(Modifier.layoutId(TopBarSlot.Title)) {
                CompositionLocalProvider(LocalTextStyle provides style.titleTextStyle, content = title)
            }
            Row(
                modifier = Modifier.layoutId(TopBarSlot.Actions),
                horizontalArrangement = Arrangement.spacedBy(style.actionGap),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        },
        modifier = modifier,
        measurePolicy = measurePolicy
    )
}

private enum class TopBarSlot {
    Navigation,
    Title,
    Actions
}

// As wide as the incoming maximum. A centred title keeps clear of the wider side, so it stays in the middle.
private class TopBarMeasurePolicy(private val style: TopBarStyleScope) : MeasurePolicy {
    override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
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
            maxOf(style.minHeight.roundToPx(), navigation.height, actions.height, title.height)
        )
        return layout(width, height) {
            navigation.placeRelative(padding, (height - navigation.height) / 2)
            actions.placeRelative(width - padding - actions.width, (height - actions.height) / 2)
            val titleStart = if (centered) (width - title.width) / 2 else navigationEnd
            title.placeRelative(titleStart, (height - title.height) / 2)
        }
    }
}

@KotogramPreview
@Composable
private fun TopBarPreview() {
    TopBar(
        title = { Text("Country", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigation = { IconButton(KotogramIcons.Close, contentDescription = null, onClick = {}) },
        actions = { IconButton(KotogramIcons.Search, contentDescription = null, onClick = {}) }
    )
}

@KotogramLaptopPreview
@Composable
private fun TopBarLaptopPreview() = TopBarPreview()

// UIKit's navigation bar in a sheet on touch screens (17 semibold, 16 from the edges); our own on a laptop. Apple's
// platforms centre the title, Material starts it after the navigation button.
@Suppress("MagicNumber")
fun interface TopBarStyle : ComponentStyle<TopBarStyleScope> {
    companion object : TopBarStyle {
        override fun TopBarStyleScope.applyStyle() {}

        val Default = TopBarStyle {
            centeredTitle(currentPlatform.isApple)
            minHeight(56.dp)
            horizontalPadding(16.dp)
            titleGap(8.dp)
            actionGap(8.dp)
            titleTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.colors.onSurface
                )
            )
        }

        val Adaptive = Default then TopBarStyle {
            laptop {
                minHeight(44.dp)
                horizontalPadding(10.dp)
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

class TopBarStyleScope(val theme: KotogramTheme.Values, override val mediaQueryInfo: MediaQueryInfo) :
    AdaptiveStyleScope<TopBarStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var centeredTitle: Boolean = false
        private set

    var minHeight: Dp = Dp.Unspecified
        private set

    var horizontalPadding: Dp = Dp.Unspecified
        private set

    var titleGap: Dp = Dp.Unspecified
        private set

    var actionGap: Dp = Dp.Unspecified
        private set

    var titleTextStyle: TextStyle = TextStyle.Default
        private set

    fun centeredTitle(centered: Boolean) {
        centeredTitle = centered
    }

    fun minHeight(height: Dp) {
        minHeight = height
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

    fun titleTextStyle(style: TextStyle) {
        titleTextStyle = style
    }
}
