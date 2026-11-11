package com.xbot.kotogram.feature.auth.impl.screen.auth

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver

@Suppress("MagicNumber")
internal fun interface LanguageSuggestionStyle : ComponentStyle<LanguageSuggestionStyleScope> {
    companion object : LanguageSuggestionStyle {
        override fun LanguageSuggestionStyleScope.applyStyle() {}

        val Default = LanguageSuggestionStyle {
            bottomPadding(8.dp)
            fadeSpec(tween(200))
            resizeSpec(tween(250))
            ripple(false)
        }

        val Adaptive = Default then LanguageSuggestionStyle {
            laptop {
                bottomPadding(theme.spacing.medium)
            }
        }
    }
}

internal infix fun LanguageSuggestionStyle.then(other: LanguageSuggestionStyle): LanguageSuggestionStyle = when {
    other === LanguageSuggestionStyle -> this

    this === LanguageSuggestionStyle -> other

    else -> LanguageSuggestionStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

internal class LanguageSuggestionStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<LanguageSuggestionStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var bottomPadding: Dp = Dp.Unspecified
        private set

    var fadeSpec: FiniteAnimationSpec<Float> = snap()
        private set

    var resizeSpec: FiniteAnimationSpec<IntSize> = snap()
        private set

    var ripple: Boolean = true
        private set

    fun bottomPadding(padding: Dp) {
        bottomPadding = padding
    }

    fun fadeSpec(spec: FiniteAnimationSpec<Float>) {
        fadeSpec = spec
    }

    fun resizeSpec(spec: FiniteAnimationSpec<IntSize>) {
        resizeSpec = spec
    }

    fun ripple(enabled: Boolean) {
        ripple = enabled
    }
}
