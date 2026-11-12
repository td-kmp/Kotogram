package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil3.Bitmap

internal actual fun Bitmap.asComposeBitmap(): ImageBitmap = asImageBitmap()

internal actual fun ImageBitmap.asCoilBitmap(): Bitmap = asAndroidBitmap()
