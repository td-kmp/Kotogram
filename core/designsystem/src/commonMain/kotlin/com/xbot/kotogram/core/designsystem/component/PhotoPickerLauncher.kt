package com.xbot.kotogram.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.xbot.kotogram.core.designsystem.component.platform.platformPhotoPickerLauncher

@Stable
fun interface PhotoPickerLauncher {
    fun launch()
}

@Composable
fun rememberPhotoPickerLauncher(onPicked: (jpeg: ByteArray) -> Unit): PhotoPickerLauncher? =
    platformPhotoPickerLauncher(onPicked)
