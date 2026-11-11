package com.xbot.kotogram.feature.auth.impl.screen.phone

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
internal fun interface PhoneScreenStyle : ComponentStyle<PhoneScreenStyleScope> {
    companion object : PhoneScreenStyle {
        override fun PhoneScreenStyleScope.applyStyle() {}

        val Default = PhoneScreenStyle {
            horizontalPadding(16.dp)
            contentMaxWidth(430.dp)
            logoSize(100.dp)
            beforeTitle(18.dp)
            beforeInfo(18.dp)
            beforeField(30.dp)
            beforeError(10.dp)
            beforeAction(40.dp)
            bottomActionInset(24.dp)
            infoWidth(306.dp)
            titleTextStyle(
                theme.typography.headlineLargeEmphasized.merge(
                    color = theme.colors.onSurface,
                ),
            )
            infoTextStyle(
                theme.typography.bodyLarge.merge(
                    color = theme.colors.onSurface,
                    textAlign = TextAlign.Center,
                ),
            )
            errorTextStyle(
                theme.typography.bodySmall.merge(
                    color = theme.colors.error,
                    textAlign = TextAlign.Center,
                ),
            )
            qrLoginTextStyle(
                theme.typography.titleSmall,
            )
            errorFadeSpec(tween(200))
            landscapeHeight(480.dp)
        }

        val Adaptive = Default then PhoneScreenStyle {
            laptop {
                contentMaxWidth(Dp.Unspecified)
                logoSize(140.dp)
                beforeTitle(20.dp)
                beforeInfo(10.dp)
                beforeField(20.dp)
                infoWidth(Dp.Infinity)
                infoTextStyle(
                    theme.typography.bodyLarge.merge(
                        color = theme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        }
    }
}

internal infix fun PhoneScreenStyle.then(other: PhoneScreenStyle): PhoneScreenStyle = when {
    other === PhoneScreenStyle -> this

    this === PhoneScreenStyle -> other

    else -> PhoneScreenStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
internal class PhoneScreenStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<PhoneScreenStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var horizontalPadding: Dp = Dp.Unspecified
        private set

    var contentMaxWidth: Dp = Dp.Unspecified
        private set

    var logoSize: Dp = Dp.Unspecified
        private set

    var beforeTitle: Dp = Dp.Unspecified
        private set

    var beforeInfo: Dp = Dp.Unspecified
        private set

    var beforeField: Dp = Dp.Unspecified
        private set

    var beforeError: Dp = Dp.Unspecified
        private set

    var beforeAction: Dp = Dp.Unspecified
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

    var qrLoginTextStyle: TextStyle = TextStyle.Default
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

    fun logoSize(size: Dp) {
        logoSize = size
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

    fun beforeAction(space: Dp) {
        beforeAction = space
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

    fun qrLoginTextStyle(style: TextStyle) {
        qrLoginTextStyle = style
    }

    fun errorFadeSpec(spec: FiniteAnimationSpec<Float>) {
        errorFadeSpec = spec
    }

    fun landscapeHeight(height: Dp) {
        landscapeHeight = height
    }
}
