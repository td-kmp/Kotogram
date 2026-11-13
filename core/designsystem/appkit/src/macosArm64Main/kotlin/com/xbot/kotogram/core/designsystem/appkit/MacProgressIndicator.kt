package com.xbot.kotogram.core.designsystem.appkit

import com.xbot.kotogram.core.designsystem.appkit.extensions.handle
import com.xbot.kotogram.core.designsystem.appkit.extensions.isDark
import platform.AppKit.NSControlSizeRegular
import platform.AppKit.NSControlSizeSmall
import platform.AppKit.NSLayoutConstraint
import platform.AppKit.NSProgressIndicator
import platform.AppKit.NSProgressIndicatorStyleSpinning
import platform.AppKit.NSView
import platform.AppKit.centerXAnchor
import platform.AppKit.centerYAnchor
import platform.AppKit.translatesAutoresizingMaskIntoConstraints

class MacProgressIndicator {
    private val indicator = NSProgressIndicator().apply {
        style = NSProgressIndicatorStyleSpinning
        indeterminate = true
        displayedWhenStopped = false
        controlSize = NSControlSizeSmall
    }

    val viewHandle: Long get() = indicator.handle()

    // Workaround: AppKit draws a light spinner only in its dark appearance.
    var isLight: Boolean
        get() = indicator.isDark
        set(value) {
            indicator.isDark = value
        }

    var isLarge: Boolean
        get() = indicator.controlSize == NSControlSizeRegular
        set(value) {
            indicator.controlSize = if (value) NSControlSizeRegular else NSControlSizeSmall
        }

    var isAnimating: Boolean = false
        set(value) {
            field = value
            if (value) indicator.startAnimation(null) else indicator.stopAnimation(null)
        }

    internal fun centerIn(parent: NSView) {
        indicator.translatesAutoresizingMaskIntoConstraints = false
        parent.addSubview(indicator)
        NSLayoutConstraint.activateConstraints(
            [
                indicator.centerXAnchor.constraintEqualToAnchor(parent.centerXAnchor),
                indicator.centerYAnchor.constraintEqualToAnchor(parent.centerYAnchor),
            ],
        )
    }
}
