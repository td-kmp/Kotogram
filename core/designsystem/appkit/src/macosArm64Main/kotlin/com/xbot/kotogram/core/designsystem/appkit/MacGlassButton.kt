package com.xbot.kotogram.core.designsystem.appkit

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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
    private var clickCount = 0
    private val clickEvents = MutableSharedFlow<Int>(extraBufferCapacity = EVENT_BUFFER)
    private val target = ActionTarget { clickEvents.tryEmit(++clickCount) }
    private val button = NSButton.buttonWithTitle("", target, ActionSelector).apply {
        refusesFirstResponder = true
        focusRingType = NSFocusRingTypeNone
        bezelStyle = NSBezelStyleGlass
        controlSize = NSControlSizeExtraLarge
        tintProminence = NSTintProminencePrimary
    }
    private val spinner = MacProgressIndicator().also { it.centerIn(button) }

    val viewHandle: Long get() = button.handle()

    fun clicks(): Flow<Int> = clickEvents

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

    fun setBezelColor(argb: Int) {
        button.bezelColor = argb.toNSColor()
    }

    fun setDark(dark: Boolean) {
        button.appearance = appearance(dark)
    }

    fun setLoading(
        loading: Boolean,
        lightSpinner: Boolean,
    ) {
        spinner.setLight(lightSpinner)
        spinner.setAnimating(loading)
    }

    fun setEnabled(enabled: Boolean) {
        button.enabled = enabled
    }

    companion object {
        val isSupported: Boolean
            get() = NSButton.instancesRespondToSelector(NSSelectorFromString("setTintProminence:"))
    }
}
