package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import com.xbot.kotogram.core.designsystem.component.ProgressIndicatorOverride
import com.xbot.kotogram.core.designsystem.component.ProgressIndicatorOverrideScope
import com.xbot.kotogram.core.designsystem.extensions.toUIColor
import platform.UIKit.UIActivityIndicatorView
import platform.UIKit.UIActivityIndicatorViewStyleLarge
import platform.UIKit.UIActivityIndicatorViewStyleMedium

internal object ActivityIndicatorOverride : ProgressIndicatorOverride {
    @Composable
    override fun ProgressIndicatorOverrideScope.ProgressIndicator() {
        val density = LocalDensity.current
        var large by remember { mutableStateOf(false) }
        val uiColor = style.color.toUIColor()
        OverlayView(
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
            },
            properties = SpinnerProperties,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private val SpinnerProperties = UIKitInteropProperties(interactionMode = null, placedAsOverlay = true)

private val LargeSize = 32.dp
