package com.xbot.kotogram.core.designsystem.appkit

import com.xbot.kotogram.core.designsystem.appkit.extensions.handle
import com.xbot.kotogram.core.designsystem.appkit.extensions.isDark
import com.xbot.kotogram.core.designsystem.appkit.extensions.toArgb
import com.xbot.kotogram.core.designsystem.appkit.extensions.toNSColor
import kotlinx.coroutines.flow.Flow
import platform.AppKit.NSBezelStyleGlass
import platform.AppKit.NSButton
import platform.AppKit.NSControlSizeExtraLarge
import platform.AppKit.NSFocusRingTypeNone
import platform.AppKit.NSFont
import platform.AppKit.NSFontAttributeName
import platform.AppKit.NSForegroundColorAttributeName
import platform.AppKit.NSTintProminencePrimary
import platform.Foundation.NSAttributedString
import platform.Foundation.NSSelectorFromString
import platform.Foundation.create

class MacGlassButton {
    private val clickEvents = clickFlow()
    private val button = NSButton.buttonWithTitle("", clickEvents.target, ActionSelector).apply {
        refusesFirstResponder = true
        focusRingType = NSFocusRingTypeNone
        bezelStyle = NSBezelStyleGlass
        controlSize = NSControlSizeExtraLarge
        tintProminence = NSTintProminencePrimary
    }
    private val spinner = MacProgressIndicator().also { it.centerIn(button) }

    val viewHandle: Long get() = button.handle()

    var bezelColor: Int
        get() = button.bezelColor?.toArgb() ?: 0
        set(value) {
            button.bezelColor = value.toNSColor()
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

    fun setTitle(
        text: String,
        fontSize: Double,
        fontWeight: Double,
        argb: Int,
    ) {
        val attributes = mapOf<Any?, Any?>(
            NSFontAttributeName to NSFont.systemFontOfSize(fontSize, fontWeight),
            NSForegroundColorAttributeName to argb.toNSColor(),
        )
        button.attributedTitle = NSAttributedString.create(string = text, attributes = attributes)
    }

    fun setLoading(
        loading: Boolean,
        lightSpinner: Boolean,
    ) {
        spinner.isLight = lightSpinner
        spinner.isAnimating = loading
    }

    companion object {
        val isSupported: Boolean
            get() = NSButton.instancesRespondToSelector(NSSelectorFromString("setTintProminence:"))
    }
}
