package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asComposeShader
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.SamplingMode

// Workaround: Compose's ImageShader samples the nearest pixel on Skia; a sprite between pixels needs linear.
internal actual fun ImageBitmap.toSpritePaint(): Paint = Paint().also {
    it.shader = asSkiaBitmap()
        .makeShader(FilterTileMode.CLAMP, FilterTileMode.CLAMP, SamplingMode.LINEAR)
        .asComposeShader()
}
