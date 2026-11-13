package com.xbot.kotogram.feature.auth.impl.screen.premiumpurchase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.isSpecified
import com.xbot.kotogram.core.designsystem.PreviewKotogram
import com.xbot.kotogram.core.designsystem.PreviewKotogramLaptop
import com.xbot.kotogram.core.designsystem.component.GlassButton
import com.xbot.kotogram.core.designsystem.media.derivedMediaQuery
import com.xbot.kotogram.core.designsystem.media.mediaQuery
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.navigation_back
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.core.resources.unsupported_subtitle
import com.xbot.kotogram.core.resources.unsupported_title
import com.xbot.kotogram.feature.auth.impl.extensions.isBook

@Composable
internal fun PremiumPurchaseScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style =
        rememberStyle(::PremiumPurchaseScreenStyleScope, PremiumPurchaseScreenStyle.Adaptive)
    val posture = mediaQuery { windowPosture }
    val landscape by derivedMediaQuery { windowHeight < style.landscapeHeight }
    val actionAtBottom = !style.mediaQueryInfo.isLaptop && !landscape
    context(style) {
        Scaffold(
            modifier = modifier,
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { innerPadding ->
            Column(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (posture.isBook) 0.5f else 1f)
                    .padding(innerPadding),
            ) {
                BoxWithConstraints(Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .heightIn(min = maxHeight)
                            .padding(horizontal = style.horizontalPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (!landscape && !posture.isTabletop) {
                            Box(Modifier.size(style.animationSize).background(Color.White))
                            Spacer(Modifier.height(style.beforeTitle))
                        }
                        Text(
                            text = stringResource(Res.string.unsupported_title),
                            style = style.titleTextStyle,
                        )
                        Spacer(Modifier.height(style.beforeInfo))
                        Text(
                            text = stringResource(Res.string.unsupported_subtitle),
                            style = style.infoTextStyle,
                            modifier = Modifier.widthIn(max = style.infoWidth),
                        )
                        if (!actionAtBottom) {
                            Spacer(Modifier.height(style.beforeAction))
                            BackAction(onBackClick)
                        }
                    }
                }
                if (posture.isTabletop) Spacer(Modifier.weight(1f))
                if (actionAtBottom) {
                    BackAction(
                        onBackClick = onBackClick,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(style.bottomActionInset),
                    )
                }
            }
        }
    }
}

@Composable
context(style: PremiumPurchaseScreenStyleScope)
private fun BackAction(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassButton(
        text = stringResource(Res.string.navigation_back),
        onClick = onBackClick,
        modifier = if (style.contentMaxWidth.isSpecified) {
            modifier.widthIn(max = style.contentMaxWidth).fillMaxWidth()
        } else {
            modifier
        },
    )
}

@PreviewKotogram
@Composable
private fun PremiumPurchaseScreenPreview() {
    PremiumPurchaseScreen(onBackClick = {})
}

@PreviewKotogramLaptop
@Composable
private fun PremiumPurchaseScreenLaptopPreview() = PremiumPurchaseScreenPreview()
