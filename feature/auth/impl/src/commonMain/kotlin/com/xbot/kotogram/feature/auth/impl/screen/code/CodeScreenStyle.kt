package com.xbot.kotogram.feature.auth.impl.screen.code

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

// Telegram for iOS's code entry (AuthorizationSequenceCodeEntryControllerNode) on touch screens, and Telegram for
// macOS's (Auth_CodeEntry, Auth_Insets) on a laptop, in their points.
@Suppress("MagicNumber")
internal fun interface CodeScreenStyle : ComponentStyle<CodeScreenStyleScope> {
    companion object : CodeScreenStyle {
        override fun CodeScreenStyleScope.applyStyle() {}

        val Default = CodeScreenStyle {
            horizontalPadding(24.dp)
            animationSize(100.dp)
            beforeTitle(18.dp)
            beforeInfo(10.dp)
            beforeField(30.dp)
            beforeError(10.dp)
            afterField(50.dp)
            beforeLinks(0.dp)
            infoWidth(Dp.Infinity)
            linkColor(theme.colors.primary)
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
            // WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND.
            landscapeHeight(480.dp)
        }

        val Adaptive = Default then CodeScreenStyle {
            laptop {
                animationSize(140.dp)
                beforeTitle(20.dp)
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

internal infix fun CodeScreenStyle.then(other: CodeScreenStyle): CodeScreenStyle = when {
    other === CodeScreenStyle -> this

    this === CodeScreenStyle -> other

    else -> CodeScreenStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
internal class CodeScreenStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo
) : AdaptiveStyleScope<CodeScreenStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var horizontalPadding: Dp = Dp.Unspecified
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

    // Under the field, with the error in it: down to the links on touch screens, to Next on a laptop.
    var afterField: Dp = Dp.Unspecified
        private set

    var beforeLinks: Dp = Dp.Unspecified
        private set

    var infoWidth: Dp = Dp.Unspecified
        private set

    var linkColor: Color = Color.Unspecified
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

    fun infoWidth(width: Dp) {
        infoWidth = width
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
