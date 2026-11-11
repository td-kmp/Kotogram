package com.xbot.kotogram.feature.auth.impl.screen.registration

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
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
internal fun interface RegistrationScreenStyle : ComponentStyle<RegistrationScreenStyleScope> {
    companion object : RegistrationScreenStyle {
        override fun RegistrationScreenStyleScope.applyStyle() {}

        val Default = RegistrationScreenStyle {
            horizontalPadding(24.dp)
            contentMaxWidth(430.dp)
            avatarSize(110.dp)
            beforeTitle(18.dp)
            beforeInfo(20.dp)
            beforeField(32.dp)
            fieldGap(12.dp)
            beforeError(10.dp)
            afterField(48.dp)
            beforeLinks(0.dp)
            bottomActionInset(24.dp)
            infoWidth(Dp.Infinity)
            avatarColor(theme.colors.surfaceContainerHighest)
            avatarIconColor(theme.colors.onSurfaceVariant)
            linkColor(theme.colors.primary)
            titleTextStyle(
                theme.typography.headlineLarge.merge(
                    color = theme.colors.onSurface,
                    textAlign = TextAlign.Center,
                ),
            )
            infoTextStyle(
                theme.typography.bodyLarge.merge(
                    color = theme.colors.onSurface,
                    textAlign = TextAlign.Center,
                ),
            )
            termsTextStyle(
                theme.typography.bodySmall.merge(
                    color = theme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                ),
            )
            errorTextStyle(
                theme.typography.bodySmall.merge(
                    color = theme.colors.error,
                    textAlign = TextAlign.Center,
                ),
            )
            errorFadeSpec(tween(200))
            landscapeHeight(480.dp)
        }

        val Adaptive = Default then RegistrationScreenStyle {
            laptop {
                infoWidth(380.dp)
                contentMaxWidth(Dp.Unspecified)
                avatarSize(140.dp)
                beforeTitle(20.dp)
                beforeInfo(10.dp)
                beforeField(20.dp)
                fieldGap(10.dp)
                afterField(40.dp)
                beforeLinks(20.dp)
                infoTextStyle(
                    theme.typography.bodyLarge.merge(
                        color = theme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    ),
                )
                termsTextStyle(
                    theme.typography.bodyMedium.merge(
                        color = theme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        }
    }
}

internal infix fun RegistrationScreenStyle.then(other: RegistrationScreenStyle): RegistrationScreenStyle = when {
    other === RegistrationScreenStyle -> this

    this === RegistrationScreenStyle -> other

    else -> RegistrationScreenStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
internal class RegistrationScreenStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<RegistrationScreenStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var horizontalPadding: Dp = Dp.Unspecified
        private set

    var contentMaxWidth: Dp = Dp.Unspecified
        private set

    var avatarSize: Dp = Dp.Unspecified
        private set

    var beforeTitle: Dp = Dp.Unspecified
        private set

    var beforeInfo: Dp = Dp.Unspecified
        private set

    var beforeField: Dp = Dp.Unspecified
        private set

    var fieldGap: Dp = Dp.Unspecified
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

    var avatarColor: Color = Color.Unspecified
        private set

    var avatarIconColor: Color = Color.Unspecified
        private set

    var linkColor: Color = Color.Unspecified
        private set

    var titleTextStyle: TextStyle = TextStyle.Default
        private set

    var infoTextStyle: TextStyle = TextStyle.Default
        private set

    var termsTextStyle: TextStyle = TextStyle.Default
        private set

    var errorTextStyle: TextStyle = TextStyle.Default
        private set

    var errorFadeSpec: FiniteAnimationSpec<Float> = snap()
        private set

    var landscapeHeight: Dp = Dp.Unspecified
        private set

    fun horizontalPadding(space: Dp) {
        horizontalPadding = space
    }

    fun contentMaxWidth(space: Dp) {
        contentMaxWidth = space
    }

    fun avatarSize(space: Dp) {
        avatarSize = space
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

    fun fieldGap(space: Dp) {
        fieldGap = space
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

    fun bottomActionInset(space: Dp) {
        bottomActionInset = space
    }

    fun infoWidth(space: Dp) {
        infoWidth = space
    }

    fun avatarColor(color: Color) {
        avatarColor = color
    }

    fun avatarIconColor(color: Color) {
        avatarIconColor = color
    }

    fun linkColor(color: Color) {
        linkColor = color
    }

    fun titleTextStyle(style: TextStyle) {
        titleTextStyle = style
    }

    fun infoTextStyle(style: TextStyle) {
        infoTextStyle = style
    }

    fun termsTextStyle(style: TextStyle) {
        termsTextStyle = style
    }

    fun errorTextStyle(style: TextStyle) {
        errorTextStyle = style
    }

    fun errorFadeSpec(spec: FiniteAnimationSpec<Float>) {
        errorFadeSpec = spec
    }

    fun landscapeHeight(space: Dp) {
        landscapeHeight = space
    }
}
