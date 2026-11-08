package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle

// The caller's modifier sets the size; the style's is only for a modifier without one.
@Composable
fun ProgressIndicator(modifier: Modifier = Modifier, style: ProgressIndicatorStyle = ProgressIndicatorStyle) {
    @Suppress("NAME_SHADOWING")
    val style = rememberStyle(::ProgressIndicatorStyleScope, ProgressIndicatorStyle.Default then style)
    with(LocalProgressIndicatorOverride.current) {
        ProgressIndicatorOverrideScope(modifier.size(style.size), style).ProgressIndicator()
    }
}

internal interface ProgressIndicatorOverride {
    @Composable
    fun ProgressIndicatorOverrideScope.ProgressIndicator()
}

internal class ProgressIndicatorOverrideScope(val modifier: Modifier, val style: ProgressIndicatorStyleScope)

internal object DefaultProgressIndicatorOverride : ProgressIndicatorOverride {
    @Composable
    override fun ProgressIndicatorOverrideScope.ProgressIndicator() {
        CircularProgressIndicator(modifier = modifier, color = style.color, strokeWidth = style.strokeWidth)
    }
}

internal val LocalProgressIndicatorOverride: ProvidableCompositionLocal<ProgressIndicatorOverride> =
    staticCompositionLocalOf { DefaultProgressIndicatorOverride }

// The same on every form factor. UIKit's and AppKit's spinners draw their own stroke.
@Suppress("MagicNumber")
fun interface ProgressIndicatorStyle : ComponentStyle<ProgressIndicatorStyleScope> {
    companion object : ProgressIndicatorStyle {
        override fun ProgressIndicatorStyleScope.applyStyle() {}

        val Default = ProgressIndicatorStyle {
            size(20.dp)
            strokeWidth(2.dp)
            color(theme.colors.primary)
        }
    }
}

infix fun ProgressIndicatorStyle.then(other: ProgressIndicatorStyle): ProgressIndicatorStyle = when {
    other === ProgressIndicatorStyle -> this
    this === ProgressIndicatorStyle -> other
    else -> ProgressIndicatorStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

class ProgressIndicatorStyleScope(val theme: KotogramTheme.Values, override val mediaQueryInfo: MediaQueryInfo) :
    AdaptiveStyleScope<ProgressIndicatorStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var size: Dp = Dp.Unspecified
        private set

    var strokeWidth: Dp = Dp.Unspecified
        private set

    var color: Color = Color.Unspecified
        private set

    fun size(size: Dp) {
        this.size = size
    }

    fun strokeWidth(width: Dp) {
        strokeWidth = width
    }

    fun color(color: Color) {
        this.color = color
    }
}
