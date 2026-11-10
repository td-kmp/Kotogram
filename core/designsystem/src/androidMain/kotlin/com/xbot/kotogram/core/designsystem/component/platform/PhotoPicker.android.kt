package com.xbot.kotogram.core.designsystem.component.platform

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.xbot.kotogram.core.designsystem.component.PhotoPickerLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

// Adapted from Calf's FilePickerLauncher.android.kt (Mohamed Rejeb, Apache-2.0).
@Composable
internal actual fun platformPhotoPickerLauncher(onPicked: (ByteArray) -> Unit): PhotoPickerLauncher? {
    val resolver = LocalContext.current.contentResolver
    val scope = rememberCoroutineScope()
    val currentOnPicked by rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            withContext(Dispatchers.IO) { resolver.readJpeg(uri) }?.let(currentOnPicked)
        }
    }
    return remember(launcher) {
        PhotoPickerLauncher { launcher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) }
    }
}

private fun ContentResolver.readJpeg(uri: Uri): ByteArray? {
    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(this, uri)) { decoder, info, _ ->
            val scale = scale(info.size.width, info.size.height)
            if (scale < 1f) {
                decoder.setTargetSize(
                    (info.size.width * scale).roundToInt(),
                    (
                        info.size.height *
                            scale
                        ).roundToInt(),
                )
            }
        }
    } else {
        openInputStream(uri)?.use(BitmapFactory::decodeStream)?.let { decoded ->
            val scale = scale(decoded.width, decoded.height)
            if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    decoded,
                    (decoded.width * scale).roundToInt(),
                    (decoded.height * scale).roundToInt(),
                    true,
                )
            } else {
                decoded
            }
        }
    } ?: return null
    return ByteArrayOutputStream().use { stream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, PHOTO_JPEG_QUALITY, stream)
        stream.toByteArray()
    }
}

private fun scale(
    width: Int,
    height: Int,
): Float = minOf(
    1f,
    PHOTO_MAX_SIDE.toFloat() / max(width, height),
)
