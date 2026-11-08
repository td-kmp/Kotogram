package com.xbot.kotogram.feature.auth.impl.screen.qr

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
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

// Our own on touch screens, in Telegram for iOS's type, as it has no such screen; Telegram for macOS's QR login screen
// on a laptop, in its points.
@Suppress("MagicNumber")
internal fun interface QrScreenStyle : ComponentStyle<QrScreenStyleScope> {
    companion object : QrScreenStyle {
        override fun QrScreenStyleScope.applyStyle() {}

        val Default = QrScreenStyle {
            horizontalPadding(16.dp)
            touchPadding(24.dp)
            contentWidth(380.dp)
            stepTextWidth(350.dp)
            beforeTitle(30.dp)
            beforeSteps(24.dp)
            betweenSteps(14.dp)
            beforeButton(12.dp)
            stepNumberSize(26.dp)
            stepNumberColor(theme.colors.primary)
            stepTextGap(14.dp)
            stepTextTop(2.dp)
            stepTextBottom(0.dp)
            titleTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.onSurface
                )
            )
            stepTextStyle(theme.typography.bodyLarge.merge(fontSize = 17.sp, color = theme.colors.onSurface))
            stepNumberTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    color = theme.colors.onPrimary
                )
            )
            errorTextStyle(theme.typography.bodySmall.merge(color = theme.colors.error, textAlign = TextAlign.Center))
            // WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND.
            landscapeHeight(480.dp)
        }

        val Adaptive = Default then QrScreenStyle {
            laptop {
                beforeTitle(20.dp)
                beforeSteps(20.dp)
                betweenSteps(10.dp)
                beforeButton(10.dp)
                stepNumberSize(20.dp)
                stepTextGap(10.dp)
                stepTextBottom(6.dp)
                titleTextStyle(
                    theme.typography.bodyLarge.merge(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.colors.onSurface
                    )
                )
                stepTextStyle(theme.typography.bodyLarge.merge(fontSize = 13.sp, color = theme.colors.onSurface))
                stepNumberTextStyle(
                    theme.typography.bodyLarge.merge(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = theme.colors.onPrimary
                    )
                )
            }
        }
    }
}

internal infix fun QrScreenStyle.then(other: QrScreenStyle): QrScreenStyle = when {
    other === QrScreenStyle -> this
    this === QrScreenStyle -> other
    else -> QrScreenStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

internal class QrScreenStyleScope(val theme: KotogramTheme.Values, override val mediaQueryInfo: MediaQueryInfo) :
    AdaptiveStyleScope<QrScreenStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var horizontalPadding: Dp = Dp.Unspecified
        private set

    var touchPadding: Dp = Dp.Unspecified
        private set

    var contentWidth: Dp = Dp.Unspecified
        private set

    var stepTextWidth: Dp = Dp.Unspecified
        private set

    var beforeTitle: Dp = Dp.Unspecified
        private set

    var beforeSteps: Dp = Dp.Unspecified
        private set

    var betweenSteps: Dp = Dp.Unspecified
        private set

    var beforeButton: Dp = Dp.Unspecified
        private set

    var stepNumberSize: Dp = Dp.Unspecified
        private set

    var stepNumberColor: Color = Color.Unspecified
        private set

    var stepTextGap: Dp = Dp.Unspecified
        private set

    var stepTextTop: Dp = Dp.Unspecified
        private set

    var stepTextBottom: Dp = Dp.Unspecified
        private set

    var titleTextStyle: TextStyle = TextStyle.Default
        private set

    var stepTextStyle: TextStyle = TextStyle.Default
        private set

    var stepNumberTextStyle: TextStyle = TextStyle.Default
        private set

    var errorTextStyle: TextStyle = TextStyle.Default
        private set

    var landscapeHeight: Dp = Dp.Unspecified
        private set

    fun horizontalPadding(padding: Dp) {
        horizontalPadding = padding
    }

    fun touchPadding(padding: Dp) {
        touchPadding = padding
    }

    fun contentWidth(width: Dp) {
        contentWidth = width
    }

    fun stepTextWidth(width: Dp) {
        stepTextWidth = width
    }

    fun beforeTitle(space: Dp) {
        beforeTitle = space
    }

    fun beforeSteps(space: Dp) {
        beforeSteps = space
    }

    fun betweenSteps(space: Dp) {
        betweenSteps = space
    }

    fun beforeButton(space: Dp) {
        beforeButton = space
    }

    fun stepNumberSize(size: Dp) {
        stepNumberSize = size
    }

    fun stepNumberColor(color: Color) {
        stepNumberColor = color
    }

    fun stepTextGap(space: Dp) {
        stepTextGap = space
    }

    fun stepTextTop(space: Dp) {
        stepTextTop = space
    }

    fun stepTextBottom(space: Dp) {
        stepTextBottom = space
    }

    fun titleTextStyle(style: TextStyle) {
        titleTextStyle = style
    }

    fun stepTextStyle(style: TextStyle) {
        stepTextStyle = style
    }

    fun stepNumberTextStyle(style: TextStyle) {
        stepNumberTextStyle = style
    }

    fun errorTextStyle(style: TextStyle) {
        errorTextStyle = style
    }

    fun landscapeHeight(height: Dp) {
        landscapeHeight = height
    }
}
