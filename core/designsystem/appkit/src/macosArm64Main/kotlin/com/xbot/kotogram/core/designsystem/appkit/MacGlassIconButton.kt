package com.xbot.kotogram.core.designsystem.appkit

import com.xbot.kotogram.core.designsystem.appkit.extensions.handle
import com.xbot.kotogram.core.designsystem.appkit.extensions.isDark
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.flow.Flow
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
    private val clickEvents = clickFlow()
    private val button = NSButton.buttonWithTitle("", clickEvents.target, ActionSelector).apply {
        refusesFirstResponder = true
        focusRingType = NSFocusRingTypeNone
        bezelStyle = NSBezelStyleGlass
        borderShape = NSControlBorderShape.NSControlBorderShapeCircle
        imagePosition = NSImageOnly
        controlSize = NSControlSizeLarge
    }

    val viewHandle: Long get() = button.handle()

    var accessibilityLabel: String?
        get() = button.accessibilityLabel()
        set(value) {
            button.setAccessibilityLabel(value)
        }

    var isDark: Boolean
        get() = button.isDark
        set(value) {
            button.isDark = value
        }

    var isEnabled: Boolean
        get() = button.enabled
        set(value) {
            button.enabled = value
        }

    fun clicks(): Flow<Int> = clickEvents.flow

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
}
