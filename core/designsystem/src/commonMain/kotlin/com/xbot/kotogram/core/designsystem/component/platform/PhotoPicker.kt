package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.designsystem.component.PhotoPickerLauncher

@Composable
internal expect fun platformPhotoPickerLauncher(onPicked: (ByteArray) -> Unit): PhotoPickerLauncher?

// As Telegram for iOS signs up: TGPhotoEditorResultImageAvatarMaxSize, compressImageToJPEG(quality: 0.7).
internal const val PHOTO_MAX_SIDE = 2048
internal const val PHOTO_JPEG_QUALITY = 70
