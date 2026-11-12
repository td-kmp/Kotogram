package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint

internal actual fun ImageBitmap.toSpritePaint(): Paint = Paint().also {
    it.shader = ImageShader(this)
    it.filterQuality = FilterQuality.Low
}
