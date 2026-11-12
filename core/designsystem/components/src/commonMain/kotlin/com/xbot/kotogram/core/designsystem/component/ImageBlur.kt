package com.xbot.kotogram.core.designsystem.component

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntSize
import coil3.Bitmap
import coil3.size.Size
import coil3.transform.Transformation
import com.xbot.kotogram.core.designsystem.component.platform.asCoilBitmap
import com.xbot.kotogram.core.designsystem.component.platform.asComposeBitmap

@Immutable
class ImageBlur(
    val resolution: Int,
    val saturation: Float = 1f,
) {
    internal val cacheKey: String get() = "blur:$resolution:$saturation"

    internal companion object {
        val Placeholder = ImageBlur(resolution = 11)
        val Spoiler = ImageBlur(resolution = 11, saturation = 1.7f)
        val Backdrop = ImageBlur(resolution = 3)
    }
}

internal class BlurTransformation(
    private val blur: ImageBlur,
) : Transformation() {
    override val cacheKey: String get() = blur.cacheKey

    override suspend fun transform(
        input: Bitmap,
        size: Size,
    ): Bitmap = input.asComposeBitmap().blurred(blur).asCoilBitmap()
}

internal fun ImageBitmap.blurred(blur: ImageBlur): ImageBitmap {
    val short = minOf(width, height).coerceAtLeast(1)
    val coarseWidth = (width * blur.resolution / short).coerceAtLeast(1)
    val coarseHeight = (height * blur.resolution / short).coerceAtLeast(1)
    val paint = Paint().apply { filterQuality = FilterQuality.Low }
    var current = this
    while (current.width >= coarseWidth * 2 && current.height >= coarseHeight * 2) {
        current = current.scaled((current.width + 1) / 2, (current.height + 1) / 2, paint)
    }
    current = current.scaled(coarseWidth, coarseHeight, paint)
    while (minOf(current.width, current.height) < SMOOTH_SIDE) current = current.chaikinSubdivided(paint)
    if (blur.saturation != 1f) {
        paint.colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(blur.saturation) })
        current = current.scaled(current.width, current.height, paint)
    }
    return current
}

private fun ImageBitmap.chaikinSubdivided(bilinear: Paint): ImageBitmap = scaled(width * 2, height * 2, bilinear)

private fun ImageBitmap.scaled(
    width: Int,
    height: Int,
    paint: Paint,
): ImageBitmap {
    val target = ImageBitmap(width, height)
    Canvas(target).drawImageRect(image = this, dstSize = IntSize(width, height), paint = paint)
    return target
}

private const val SMOOTH_SIDE = 64
