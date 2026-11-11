package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.xbot.kotogram.core.designsystem.appkit.MacWindow
import com.xbot.kotogram.core.designsystem.component.PhotoPickerLauncher
import com.xbot.kotogram.core.designsystem.theme.currentPlatform
import dev.nucleusframework.window.tao.LocalTaoWindow
import kotlinx.coroutines.launch

@Composable
internal actual fun platformPhotoPickerLauncher(onPick: (ByteArray) -> Unit): PhotoPickerLauncher? {
    if (!currentPlatform.isApple) return null
    val window = LocalTaoWindow.current?.nsWindowHandle
    val scope = rememberCoroutineScope()
    val currentOnPick by rememberUpdatedState(onPick)
    return remember(window) {
        PhotoPickerLauncher {
            val handle = window ?: return@PhotoPickerLauncher
            scope.launch {
                MacWindow(handle)
                    .use { it.chooseImage(PHOTO_MAX_SIDE, PHOTO_JPEG_QUALITY / PERCENT) }
                    ?.let(currentOnPick)
            }
        }
    }
}

private const val PERCENT = 100.0
