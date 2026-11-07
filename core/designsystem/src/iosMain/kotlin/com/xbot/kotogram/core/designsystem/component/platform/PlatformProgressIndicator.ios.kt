package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.xbot.kotogram.core.designsystem.extensions.toUIColor
import platform.UIKit.UIActivityIndicatorView
import platform.UIKit.UIActivityIndicatorViewStyleLarge
import platform.UIKit.UIActivityIndicatorViewStyleMedium

@Composable
internal actual fun PlatformProgressIndicator(modifier: Modifier, color: Color) {
    val density = LocalDensity.current
    var large by remember { mutableStateOf(false) }
    val inFront = isSettledInFront()
    val uiColor = color.toUIColor()
    UIKitView(
        factory = {
            UIActivityIndicatorView(activityIndicatorStyle = UIActivityIndicatorViewStyleMedium).apply {
                hidesWhenStopped = false
                startAnimating()
            }
        },
        modifier = modifier.onSizeChanged { large = with(density) { it.height.toDp() } >= LargeSize },
        update = { indicator ->
            indicator.activityIndicatorViewStyle =
                if (large) UIActivityIndicatorViewStyleLarge else UIActivityIndicatorViewStyleMedium
            indicator.color = uiColor
            indicator.hidden = !inFront
        },
        properties = SpinnerProperties
    )
}

@OptIn(ExperimentalComposeUiApi::class)
private val SpinnerProperties = UIKitInteropProperties(interactionMode = null, placedAsOverlay = true)

private val LargeSize = 32.dp
