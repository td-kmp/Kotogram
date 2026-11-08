package com.xbot.kotogram.feature.auth.impl.screen.auth

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
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
            fadeSpec(tween(200))
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
    override val mediaQueryInfo: MediaQueryInfo
) : AdaptiveStyleScope<LanguageSuggestionStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var fadeSpec: FiniteAnimationSpec<Float> = snap()
        private set

    fun fadeSpec(spec: FiniteAnimationSpec<Float>) {
        fadeSpec = spec
    }
}
