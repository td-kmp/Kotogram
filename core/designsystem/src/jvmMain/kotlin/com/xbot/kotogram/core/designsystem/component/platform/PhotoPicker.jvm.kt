package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.xbot.kotogram.core.designsystem.component.PhotoPickerLauncher
import com.xbot.kotogram.core.designsystem.native.MacOpenPanel
import com.xbot.kotogram.core.designsystem.theme.currentPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
internal actual fun platformPhotoPickerLauncher(onPicked: (ByteArray) -> Unit): PhotoPickerLauncher? {
    if (!currentPlatform.isApple) return null
    val scope = rememberCoroutineScope()
    val currentOnPicked by rememberUpdatedState(onPicked)
    return remember {
        PhotoPickerLauncher {
            val path = MacOpenPanel.chooseImage() ?: return@PhotoPickerLauncher
            scope.launch {
                withContext(Dispatchers.IO) {
                    File(path).readBytes().toJpeg()
                }?.let(currentOnPicked)
            }
        }
    }
}
