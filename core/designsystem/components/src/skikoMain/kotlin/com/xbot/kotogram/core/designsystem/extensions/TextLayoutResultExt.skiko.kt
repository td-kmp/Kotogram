package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextLayoutResult

// Workaround: reshaping on Skia is slower than the raster read back (9.7 against 0.1 ms on the JVM).
internal actual fun TextLayoutResult.glyphPathBounds(): Rect? = null
