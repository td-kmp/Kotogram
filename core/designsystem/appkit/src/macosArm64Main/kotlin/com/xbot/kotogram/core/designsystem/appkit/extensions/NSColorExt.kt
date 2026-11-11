package com.xbot.kotogram.core.designsystem.appkit.extensions

import platform.AppKit.NSColor
import platform.AppKit.NSColorSpace
import kotlin.math.roundToInt

@Suppress("MagicNumber")
internal fun NSColor.toArgb(): Int {
    val color = colorUsingColorSpace(NSColorSpace.sRGBColorSpace) ?: return 0
    val alpha = (color.alphaComponent * 255).roundToInt()
    val red = (color.redComponent * 255).roundToInt()
    val green = (color.greenComponent * 255).roundToInt()
    val blue = (color.blueComponent * 255).roundToInt()
    return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
}
