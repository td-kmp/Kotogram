package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import platform.UIKit.UIColor

internal fun Color.toUIColor(): UIColor {
    val srgb = convert(ColorSpaces.Srgb)
    return UIColor.colorWithRed(
        red = srgb.red.toDouble(),
        green = srgb.green.toDouble(),
        blue = srgb.blue.toDouble(),
        alpha = srgb.alpha.toDouble(),
    )
}
