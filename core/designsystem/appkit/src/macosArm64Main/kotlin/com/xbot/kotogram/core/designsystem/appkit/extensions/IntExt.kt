package com.xbot.kotogram.core.designsystem.appkit.extensions

import platform.AppKit.NSColor

@Suppress("MagicNumber")
internal fun Int.toNSColor(): NSColor = NSColor.colorWithSRGBRed(
    red = ((this shr 16) and 0xFF) / 255.0,
    green = ((this shr 8) and 0xFF) / 255.0,
    blue = (this and 0xFF) / 255.0,
    alpha = ((this ushr 24) and 0xFF) / 255.0,
)
