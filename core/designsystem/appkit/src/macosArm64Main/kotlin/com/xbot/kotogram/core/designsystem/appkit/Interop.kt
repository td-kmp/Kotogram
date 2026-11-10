package com.xbot.kotogram.core.designsystem.appkit

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.interpretObjCPointer
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.toLong
import platform.AppKit.NSAppearance
import platform.AppKit.NSAppearanceNameAqua
import platform.AppKit.NSAppearanceNameDarkAqua
import platform.AppKit.NSColor
import platform.AppKit.NSView
import platform.AppKit.NSWindow

internal fun NSView.handle(): Long = objcPtr().toLong()

internal fun windowFrom(handle: Long): NSWindow =
    interpretObjCPointer(requireNotNull(handle.toCPointer<CPointed>()).rawValue)

internal fun appearance(dark: Boolean): NSAppearance? =
    NSAppearance.appearanceNamed(if (dark) NSAppearanceNameDarkAqua else NSAppearanceNameAqua)

@Suppress("MagicNumber")
internal fun Int.toNSColor(): NSColor = NSColor.colorWithSRGBRed(
    red = ((this shr 16) and 0xFF) / 255.0,
    green = ((this shr 8) and 0xFF) / 255.0,
    blue = (this and 0xFF) / 255.0,
    alpha = ((this ushr 24) and 0xFF) / 255.0,
)
