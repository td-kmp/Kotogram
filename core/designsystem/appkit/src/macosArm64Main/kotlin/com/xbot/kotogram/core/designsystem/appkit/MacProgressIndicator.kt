package com.xbot.kotogram.core.designsystem.appkit

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
    fun setLight(light: Boolean) {
        indicator.appearance = appearance(dark = light)
    }

    fun setLarge(large: Boolean) {
        indicator.controlSize = if (large) NSControlSizeRegular else NSControlSizeSmall
    }

    fun setAnimating(animating: Boolean) {
        if (animating) indicator.startAnimation(null) else indicator.stopAnimation(null)
    }

    internal fun centerIn(parent: NSView) {
        indicator.translatesAutoresizingMaskIntoConstraints = false
        parent.addSubview(indicator)
        NSLayoutConstraint.activateConstraints(
            listOf(
                indicator.centerXAnchor.constraintEqualToAnchor(parent.centerXAnchor),
                indicator.centerYAnchor.constraintEqualToAnchor(parent.centerYAnchor),
            ),
        )
    }
}
