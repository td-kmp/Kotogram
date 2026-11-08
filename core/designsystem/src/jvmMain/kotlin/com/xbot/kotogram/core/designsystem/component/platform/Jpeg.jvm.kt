package com.xbot.kotogram.core.designsystem.component.platform

import kotlin.math.max
import kotlin.math.roundToInt
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.FilterMipmap
import org.jetbrains.skia.FilterMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.MipmapMode
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface

internal fun ByteArray.toJpeg(): ByteArray? {
    val image = runCatching { Image.makeFromEncoded(this) }.getOrNull() ?: return null
    val scale = minOf(1f, PHOTO_MAX_SIDE.toFloat() / max(image.width, image.height))
    val width = (image.width * scale).roundToInt()
    val height = (image.height * scale).roundToInt()
    val surface = Surface.makeRasterN32Premul(width, height)
    surface.canvas.drawImageRect(
        image,
        Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
        Rect.makeWH(width.toFloat(), height.toFloat()),
        FilterMipmap(FilterMode.LINEAR, MipmapMode.LINEAR),
        null,
        true
    )
    return surface.makeImageSnapshot().encodeToData(
        EncodedImageFormat.JPEG,
        PHOTO_JPEG_QUALITY
    )?.bytes
}
