package com.xbot.kotogram.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.xbot.kotogram.core.designsystem.component.platform.platformPhotoPickerLauncher

@Stable
fun interface PhotoPickerLauncher {
    fun launch()
}

// The system's own picker of one image, which comes as a JPEG no larger than PHOTO_MAX_SIDE, as TDLib's
// inputChatPhotoStatic takes. Null where the platform has none, as a desktop other than macOS.
@Composable
fun rememberPhotoPickerLauncher(onPicked: (jpeg: ByteArray) -> Unit): PhotoPickerLauncher? =
    platformPhotoPickerLauncher(onPicked)
