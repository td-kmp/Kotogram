package com.xbot.kotogram.core.designsystem.appkit

import com.xbot.kotogram.core.designsystem.appkit.extensions.handle
import com.xbot.kotogram.core.designsystem.appkit.extensions.isDark
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import platform.AppKit.NSControlSizeRegular
import platform.AppKit.NSNoBorder
import platform.AppKit.NSScrollElasticityNone
import platform.AppKit.NSScrollView
import platform.AppKit.NSScroller
import platform.AppKit.NSView
import platform.AppKit.NSViewBoundsDidChangeNotification
import platform.AppKit.NSViewFrameDidChangeNotification
import platform.Foundation.NSMakePoint
import platform.Foundation.NSMakeRect
import platform.Foundation.NSMakeSize
import platform.Foundation.NSNotificationCenter
import kotlin.math.abs

// Hack: AppKit draws its scroller only inside a scroll view, so an empty one as wide as its scroller carries it.
class MacScroller {
    private var applying = false
    private var position = 0.0
    private var proportion = 1.0
    private val positionChanges = MutableSharedFlow<Double>(extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST)
    private val boundsTarget = ActionTarget { onBoundsChanged() }
    private val frameTarget = ActionTarget { sync(position, proportion, flash = false) }
    private val document = NSView(NSMakeRect(0.0, 0.0, 0.0, 0.0))
    private val scrollView = NSScrollView().apply {
        hasVerticalScroller = true
        hasHorizontalScroller = false
        autohidesScrollers = true
        drawsBackground = false
        borderType = NSNoBorder
        verticalScrollElasticity = NSScrollElasticityNone
        documentView = document
        contentView().postsBoundsChangedNotifications = true
        postsFrameChangedNotifications = true
    }

    init {
        val center = NSNotificationCenter.defaultCenter
        center.addObserver(boundsTarget, ActionSelector, NSViewBoundsDidChangeNotification, scrollView.contentView())
        center.addObserver(frameTarget, ActionSelector, NSViewFrameDidChangeNotification, scrollView)
    }

    val viewHandle: Long get() = scrollView.handle()

    val width: Double
        get() = NSScroller.scrollerWidthForControlSize(NSControlSizeRegular, scrollView.scrollerStyle)

    var isDark: Boolean
        get() = scrollView.isDark
        set(value) {
            scrollView.isDark = value
        }

    fun positions(): Flow<Double> = positionChanges

    fun update(
        position: Double,
        proportion: Double,
    ) {
        sync(position, proportion, flash = true)
    }

    private fun sync(
        position: Double,
        proportion: Double,
        flash: Boolean,
    ) {
        this.position = position
        this.proportion = proportion
        val clipWidth = scrollView.contentSize.useContents { width }
        val clipHeight = scrollView.contentSize.useContents { height }
        if (clipHeight <= 0.0) return
        val documentHeight = if (proportion > 0.0 && proportion < 1.0) clipHeight / proportion else clipHeight
        val range = documentHeight - clipHeight
        val y = range - (position.coerceIn(0.0, 1.0) * range)
        val currentY = scrollView.contentView().bounds.useContents { origin.y }
        val currentHeight = document.frame.useContents { size.height }
        if (abs(currentHeight - documentHeight) < EPSILON && abs(currentY - y) < EPSILON) return
        applying = true
        document.setFrameSize(NSMakeSize(clipWidth, documentHeight))
        scrollView.contentView().scrollToPoint(NSMakePoint(0.0, y))
        scrollView.reflectScrolledClipView(scrollView.contentView())
        applying = false
        if (flash && abs(currentY - y) >= EPSILON) scrollView.flashScrollers()
    }

    private fun onBoundsChanged() {
        if (applying) return
        val clipHeight = scrollView.contentSize.useContents { height }
        val range = document.frame.useContents { size.height } - clipHeight
        if (range <= 0.0) return
        val y = scrollView.contentView().bounds.useContents { origin.y }
        position = ((range - y) / range).coerceIn(0.0, 1.0)
        positionChanges.tryEmit(position)
    }
}

private const val EPSILON = 0.5
