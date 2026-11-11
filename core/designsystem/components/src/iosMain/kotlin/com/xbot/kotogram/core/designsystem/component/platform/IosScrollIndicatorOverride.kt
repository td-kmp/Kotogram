package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.component.ScrollIndicatorOverride
import com.xbot.kotogram.core.designsystem.component.ScrollIndicatorOverrideScope
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIColor
import platform.UIKit.UIScrollView
import platform.UIKit.UIScrollViewContentInsetAdjustmentBehavior
import platform.UIKit.UIScrollViewDelegateProtocol
import platform.darwin.NSObject
import kotlin.math.abs

// Hack: UIKit draws its scroll indicator only for a scroll view, so an empty one as wide as the indicator carries it.
internal object IosScrollIndicatorOverride : ScrollIndicatorOverride {
    @OptIn(ExperimentalForeignApi::class)
    @Composable
    override fun ScrollIndicatorOverrideScope.ScrollIndicator() {
        val scope = rememberCoroutineScope()
        val currentState by rememberUpdatedState(state)
        val mirror = remember { ScrollMirror { scope.launch { currentState.scrollToFraction(it) } } }
        LaunchedEffect(mirror, state) {
            snapshotFlow { state.positionFraction to state.sizeFraction }.collect { (position, size) ->
                mirror.update(position, size)
            }
        }
        OverlayView(
            factory = {
                IndicatorScrollView(mirror).apply {
                    backgroundColor = UIColor.clearColor
                    showsVerticalScrollIndicator = true
                    showsHorizontalScrollIndicator = false
                    bounces = false
                    contentInsetAdjustmentBehavior =
                        UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentNever
                    delegate = mirror
                }
            },
            properties = GlassProperties,
            modifier = modifier.width(INDICATOR_WIDTH),
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IndicatorScrollView(
    private val mirror: ScrollMirror,
) : UIScrollView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    private var laidOutHeight = 0.0

    override fun layoutSubviews() {
        super.layoutSubviews()
        val height = bounds.useContents { size.height }
        if (height != laidOutHeight) {
            laidOutHeight = height
            mirror.attach(this)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class ScrollMirror(
    private val onScroll: (Float) -> Unit,
) : NSObject(),
    UIScrollViewDelegateProtocol {
    private var view: UIScrollView? = null
    private var position = 0f
    private var proportion = 1f
    private var applying = false

    fun attach(view: UIScrollView) {
        this.view = view
        sync(flash = false)
    }

    fun update(
        position: Float,
        proportion: Float,
    ) {
        this.position = position
        this.proportion = proportion
        sync(flash = true)
    }

    override fun scrollViewDidScroll(scrollView: UIScrollView) {
        if (applying || !(scrollView.tracking || scrollView.decelerating)) return
        val range = range(scrollView)
        if (range <= 0.0) return
        position = (scrollView.contentOffset.useContents { y } / range).coerceIn(0.0, 1.0).toFloat()
        onScroll(position)
    }

    private fun sync(flash: Boolean) {
        val view = view?.takeUnless { it.tracking || it.decelerating } ?: return
        val width = view.bounds.useContents { size.width }
        val height = view.bounds.useContents { size.height }
        if (height > 0.0) {
            val content = if (proportion > 0f && proportion < 1f) height / proportion else height
            val y = (content - height) * position.coerceIn(0f, 1f)
            val currentY = view.contentOffset.useContents { this.y }
            applying = true
            view.setContentSize(CGSizeMake(width, content))
            view.setContentOffset(CGPointMake(0.0, y), animated = false)
            applying = false
            if (flash && abs(currentY - y) >= EPSILON) view.flashScrollIndicators()
        }
    }

    private fun range(view: UIScrollView): Double =
        view.contentSize.useContents { height } - view.bounds.useContents { size.height }
}

private val INDICATOR_WIDTH = 12.dp

private const val EPSILON = 0.5
