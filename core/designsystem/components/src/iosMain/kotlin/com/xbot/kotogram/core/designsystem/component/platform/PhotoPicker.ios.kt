package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import com.xbot.kotogram.core.designsystem.component.PhotoPickerLauncher
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSItemProvider
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerConfigurationAssetRepresentationModeCompatible
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.coroutines.resume

// Adapted from Calf's FilePickerLauncher.ios.kt (Mohamed Rejeb, Apache-2.0).
@Composable
internal actual fun platformPhotoPickerLauncher(onPick: (ByteArray) -> Unit): PhotoPickerLauncher? {
    val scope = rememberCoroutineScope()
    val viewController = LocalUIViewController.current
    val currentOnPick by rememberUpdatedState(onPick)
    val delegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(
                picker: PHPickerViewController,
                didFinishPicking: List<*>,
            ) {
                picker.dismissViewControllerAnimated(true, null)
                val provider =
                    (didFinishPicking.firstOrNull() as? PHPickerResult)?.itemProvider ?: return
                scope.launch {
                    provider.loadImageBytes()?.let(currentOnPick)
                }
            }
        }
    }
    return remember(viewController) {
        PhotoPickerLauncher {
            val configuration = PHPickerConfiguration().apply {
                filter = PHPickerFilter.imagesFilter()
                selectionLimit = 1
                preferredAssetRepresentationMode =
                    PHPickerConfigurationAssetRepresentationModeCompatible
            }
            val picker = PHPickerViewController(configuration)
            picker.delegate = delegate
            viewController.presentViewController(picker, animated = true, completion = null)
        }
    }
}

// Workaround: the file is deleted once the handler returns, so it is read inside it.
private suspend fun NSItemProvider.loadImageBytes(): ByteArray? = withContext(Dispatchers.IO) {
    suspendCancellableCoroutine { continuation ->
        val progress = loadFileRepresentationForTypeIdentifier(UTTypeImage.identifier) { url, _ ->
            val image = url?.path?.let { UIImage.imageWithContentsOfFile(it) }
            continuation.resume(image?.toJpeg()?.toByteArray())
        }
        continuation.invokeOnCancellation { progress.cancel() }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toJpeg(): NSData {
    val (width, height) = size.useContents { width to height }
    val scale = minOf(1.0, PHOTO_MAX_SIDE / maxOf(width, height))
    val format = UIGraphicsImageRendererFormat.defaultFormat().apply { this.scale = 1.0 }
    val renderer =
        UIGraphicsImageRenderer(size = CGSizeMake(width * scale, height * scale), format = format)
    return renderer.JPEGDataWithCompressionQuality(PHOTO_JPEG_QUALITY / PERCENT) {
        drawInRect(CGRectMake(0.0, 0.0, width * scale, height * scale))
    }
}

private const val PERCENT = 100.0

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
    if (isNotEmpty()) usePinned { memcpy(it.addressOf(0), bytes, length) }
}
