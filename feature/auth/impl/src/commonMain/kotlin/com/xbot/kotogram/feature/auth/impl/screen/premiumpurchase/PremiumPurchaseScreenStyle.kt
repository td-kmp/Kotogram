package com.xbot.kotogram.feature.auth.impl.screen.premiumpurchase

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
internal fun interface PremiumPurchaseScreenStyle :
    ComponentStyle<PremiumPurchaseScreenStyleScope> {
    companion object : PremiumPurchaseScreenStyle {
        override fun PremiumPurchaseScreenStyleScope.applyStyle() {}

        val Default = PremiumPurchaseScreenStyle {
            horizontalPadding(24.dp)
            contentMaxWidth(430.dp)
            animationSize(100.dp)
            beforeTitle(18.dp)
            beforeInfo(18.dp)
            bottomActionInset(24.dp)
            infoWidth(Dp.Infinity)
            titleTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.colors.onSurface,
                    textAlign = TextAlign.Center
                )
            )
            infoTextStyle(
                theme.typography.bodyLarge.merge(
                    fontSize = 16.sp,
                    color = theme.colors.onSurface,
                    textAlign = TextAlign.Center
                )
            )
            landscapeHeight(480.dp)
            beforeAction(40.dp)
        }

        val Adaptive = Default then PremiumPurchaseScreenStyle {
            laptop {
                infoWidth(380.dp)
                contentMaxWidth(Dp.Unspecified)
                animationSize(140.dp)
                beforeTitle(20.dp)
                beforeInfo(10.dp)
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

internal infix fun PremiumPurchaseScreenStyle.then(
    other: PremiumPurchaseScreenStyle
): PremiumPurchaseScreenStyle = when {
    other === PremiumPurchaseScreenStyle -> this

    this === PremiumPurchaseScreenStyle -> other

    else -> PremiumPurchaseScreenStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

@Suppress("TooManyFunctions")
internal class PremiumPurchaseScreenStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo
) : AdaptiveStyleScope<PremiumPurchaseScreenStyleScope>,
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

    var bottomActionInset: Dp = Dp.Unspecified
        private set

    var infoWidth: Dp = Dp.Unspecified
        private set

    var titleTextStyle: TextStyle = TextStyle.Default
        private set

    var infoTextStyle: TextStyle = TextStyle.Default
        private set

    var landscapeHeight: Dp = Dp.Unspecified
        private set

    var beforeAction: Dp = Dp.Unspecified
        private set

    fun horizontalPadding(space: Dp) {
        horizontalPadding = space
    }

    fun contentMaxWidth(space: Dp) {
        contentMaxWidth = space
    }

    fun animationSize(space: Dp) {
        animationSize = space
    }

    fun beforeTitle(space: Dp) {
        beforeTitle = space
    }

    fun beforeInfo(space: Dp) {
        beforeInfo = space
    }

    fun bottomActionInset(space: Dp) {
        bottomActionInset = space
    }

    fun infoWidth(space: Dp) {
        infoWidth = space
    }

    fun titleTextStyle(style: TextStyle) {
        titleTextStyle = style
    }

    fun infoTextStyle(style: TextStyle) {
        infoTextStyle = style
    }

    fun landscapeHeight(space: Dp) {
        landscapeHeight = space
    }

    fun beforeAction(space: Dp) {
        beforeAction = space
    }
}
