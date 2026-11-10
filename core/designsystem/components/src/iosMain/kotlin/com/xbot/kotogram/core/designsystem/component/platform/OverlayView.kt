package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.roundToIntRect
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSClassFromString
import platform.UIKit.UIView

@Composable
internal fun <T : UIView> OverlayView(
    factory: () -> T,
    properties: UIKitInteropProperties,
    modifier: Modifier = Modifier,
    update: (T) -> Unit = {},
) {
    val clip = remember { OverlayClip() }
    UIKitView(
        factory = { factory().also { clip.view = it } },
        modifier = modifier.onGloballyPositioned(clip::sync),
        update = update,
        properties = properties,
    )
}

// Workaround: Compose frames its wrapper to the part of an overlay its ancestors leave, but doesn't clip to it.
private class OverlayClip {
    var view: UIView? = null

    @OptIn(BetaInteropApi::class)
    fun sync(coordinates: LayoutCoordinates) {
        val root = coordinates.findRootCoordinates()
        val frame = root.localBoundingBoxOf(coordinates, clipBounds = false).roundToIntRect()
        val visible = root.localBoundingBoxOf(coordinates, clipBounds = true).roundToIntRect()
        val wrapper = generateSequence(view?.superview) { it.superview }.firstOrNull { it.isKindOfClass(WrapperClass) }
        wrapper?.clipsToBounds = visible != frame
    }
}

// Workaround: the wrapper's class is in the cinterop of Compose's ui-uikit, which a shared source set doesn't see.
@OptIn(BetaInteropApi::class)
private val WrapperClass = NSClassFromString("CMPInteropWrappingView")
