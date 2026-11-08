package com.xbot.kotogram.feature.auth.impl.screen.password

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver

@Suppress("MagicNumber")
internal fun interface PasswordScreenStyle : ComponentStyle<PasswordScreenStyleScope> {
    companion object : PasswordScreenStyle {
        override fun PasswordScreenStyleScope.applyStyle() {}

        val Default = PasswordScreenStyle {
            horizontalPadding(24.dp)
            contentMaxWidth(430.dp)
            animationSize(100.dp)
            beforeTitle(18.dp)
            beforeInfo(18.dp)
            beforeField(32.dp)
            beforeError(10.dp)
            afterField(48.dp)
            beforeLinks(0.dp)
            bottomActionInset(24.dp)
            infoWidth(Dp.Infinity)
            titleTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.colors.onSurface,
                    textAlign = TextAlign.Center
                )
            )
            infoTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 17.sp,
                    color = theme.colors.onSurface,
                    textAlign = TextAlign.Center
                )
            )
            errorTextStyle(
                theme.typography.bodyMedium.merge(
                    color = theme.colors.error,
                    textAlign = TextAlign.Center
                )
            )
            errorFadeSpec(tween(200))
            landscapeHeight(480.dp)
        }

        val Adaptive = Default then PasswordScreenStyle {
            laptop {
                infoWidth(380.dp)
                contentMaxWidth(Dp.Unspecified)
                animationSize(140.dp)
                beforeTitle(20.dp)
                beforeInfo(10.dp)
                beforeField(20.dp)
                afterField(40.dp)
                beforeLinks(20.dp)
                titleTextStyle(
                    theme.typography.bodyLarge.merge(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.colors.onSurface,
                        textAlign = TextAlign.Center
                    )
                )
                infoTextStyle(
                    theme.typography.bodyLarge.merge(
                        fontSize = 13.5.sp,
                        color = theme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

internal infix fun PasswordScreenStyle.then(other: PasswordScreenStyle): PasswordScreenStyle =
    when {
        other === PasswordScreenStyle -> this

        this === PasswordScreenStyle -> other

        else -> PasswordScreenStyle {
            with(this@then) { applyStyle() }
            with(other) { applyStyle() }
        }
    }

@Suppress("TooManyFunctions")
internal class PasswordScreenStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo
) : AdaptiveStyleScope<PasswordScreenStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var horizontalPadding: Dp = Dp.Unspecified
        private set

    var contentMaxWidth: Dp = Dp.Unspecified
        private set

    var animationSize: Dp = Dp.Unspecified
        private set

    var beforeTitle: Dp = Dp.Unspecified
        private set

    var beforeInfo: Dp = Dp.Unspecified
        private set

    var beforeField: Dp = Dp.Unspecified
        private set

    var beforeError: Dp = Dp.Unspecified
        private set

    var afterField: Dp = Dp.Unspecified
        private set

    var beforeLinks: Dp = Dp.Unspecified
        private set

    var bottomActionInset: Dp = Dp.Unspecified
        private set

    var infoWidth: Dp = Dp.Unspecified
        private set

    var titleTextStyle: TextStyle = TextStyle.Default
        private set

    var infoTextStyle: TextStyle = TextStyle.Default
        private set

    var errorTextStyle: TextStyle = TextStyle.Default
        private set

    var errorFadeSpec: FiniteAnimationSpec<Float> = snap()
        private set

    var landscapeHeight: Dp = Dp.Unspecified
        private set

    fun horizontalPadding(padding: Dp) {
        horizontalPadding = padding
    }

    fun contentMaxWidth(width: Dp) {
        contentMaxWidth = width
    }

    fun animationSize(size: Dp) {
        animationSize = size
    }

    fun beforeTitle(space: Dp) {
        beforeTitle = space
    }

    fun beforeInfo(space: Dp) {
        beforeInfo = space
    }

    fun beforeField(space: Dp) {
        beforeField = space
    }

    fun beforeError(space: Dp) {
        beforeError = space
    }

    fun afterField(space: Dp) {
        afterField = space
    }

    fun beforeLinks(space: Dp) {
        beforeLinks = space
    }

    fun bottomActionInset(inset: Dp) {
        bottomActionInset = inset
    }

    fun infoWidth(width: Dp) {
        infoWidth = width
    }

    fun titleTextStyle(style: TextStyle) {
        titleTextStyle = style
    }

    fun infoTextStyle(style: TextStyle) {
        infoTextStyle = style
    }

    fun errorTextStyle(style: TextStyle) {
        errorTextStyle = style
    }

    fun errorFadeSpec(spec: FiniteAnimationSpec<Float>) {
        errorFadeSpec = spec
    }

    fun landscapeHeight(height: Dp) {
        landscapeHeight = height
    }
}
