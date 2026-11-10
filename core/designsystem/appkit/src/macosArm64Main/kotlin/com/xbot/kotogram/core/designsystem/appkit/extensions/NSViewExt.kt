package com.xbot.kotogram.core.designsystem.appkit.extensions

import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.toLong
import platform.AppKit.NSAppearance
import platform.AppKit.NSAppearanceNameAqua
import platform.AppKit.NSAppearanceNameDarkAqua
import platform.AppKit.NSView

internal fun NSView.handle(): Long = objcPtr().toLong()

internal var NSView.isDark: Boolean
    get() = appearance?.name == NSAppearanceNameDarkAqua
    set(value) {
        appearance = NSAppearance.appearanceNamed(if (value) NSAppearanceNameDarkAqua else NSAppearanceNameAqua)
    }
