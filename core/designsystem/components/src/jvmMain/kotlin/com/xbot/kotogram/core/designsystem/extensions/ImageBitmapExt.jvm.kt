package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

internal fun ImageBitmap.toPng(): ByteArray? =
    Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes
