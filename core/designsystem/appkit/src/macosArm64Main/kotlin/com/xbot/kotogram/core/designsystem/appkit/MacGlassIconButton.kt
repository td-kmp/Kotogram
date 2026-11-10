package com.xbot.kotogram.core.designsystem.appkit

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import platform.AppKit.NSBezelStyleGlass
import platform.AppKit.NSButton
import platform.AppKit.NSControlBorderShape
import platform.AppKit.NSControlSizeLarge
import platform.AppKit.NSFocusRingTypeNone
import platform.AppKit.NSImage
import platform.AppKit.NSImageOnly
import platform.Foundation.NSData
import platform.Foundation.NSMakeSize
import platform.Foundation.dataWithBytes

class MacGlassIconButton {
    private var clickCount = 0
    private val clickEvents = MutableSharedFlow<Int>(extraBufferCapacity = EVENT_BUFFER)
    private val target = ActionTarget { clickEvents.tryEmit(++clickCount) }
    private val button = NSButton.buttonWithTitle("", target, ActionSelector).apply {
        refusesFirstResponder = true
        focusRingType = NSFocusRingTypeNone
        bezelStyle = NSBezelStyleGlass
        borderShape = NSControlBorderShape.NSControlBorderShapeCircle
        imagePosition = NSImageOnly
        controlSize = NSControlSizeLarge
    }

    val viewHandle: Long get() = button.handle()

    fun clicks(): Flow<Int> = clickEvents

    // Workaround: the PNG is at the screen's scale, and only the image's size in points makes AppKit draw it sharp.
    fun setImage(
        png: ByteArray,
        points: Double,
    ) {
        if (png.isEmpty()) return
        val data = png.usePinned { NSData.dataWithBytes(it.addressOf(0), png.size.toULong()) }
        val image = NSImage(data = data)
        image.size = NSMakeSize(points, points)
        button.image = image
    }

    fun setAccessibilityLabel(label: String?) {
        button.setAccessibilityLabel(label)
    }

    fun setDark(dark: Boolean) {
        button.appearance = appearance(dark)
    }

    fun setEnabled(enabled: Boolean) {
        button.enabled = enabled
    }
}
