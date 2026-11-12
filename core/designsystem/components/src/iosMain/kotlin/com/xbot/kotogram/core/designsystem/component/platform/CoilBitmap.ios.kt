package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import coil3.Bitmap

internal actual fun Bitmap.asComposeBitmap(): ImageBitmap = asComposeImageBitmap()

internal actual fun ImageBitmap.asCoilBitmap(): Bitmap = asSkiaBitmap()
