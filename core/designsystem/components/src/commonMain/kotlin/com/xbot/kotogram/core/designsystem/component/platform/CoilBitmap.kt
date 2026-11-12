package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.ui.graphics.ImageBitmap
import coil3.Bitmap

internal expect fun Bitmap.asComposeBitmap(): ImageBitmap

internal expect fun ImageBitmap.asCoilBitmap(): Bitmap
