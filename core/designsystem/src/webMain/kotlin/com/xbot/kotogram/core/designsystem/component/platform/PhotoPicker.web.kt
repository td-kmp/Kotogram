package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.xbot.kotogram.core.designsystem.component.PhotoPickerLauncher
import kotlinx.browser.document
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.coroutines.resume

// Adapted from Calf's FilePickerLauncher.wasmJs.kt (Mohamed Rejeb, Apache-2.0).
@Composable
internal actual fun platformPhotoPickerLauncher(onPicked: (ByteArray) -> Unit): PhotoPickerLauncher? {
    val scope = rememberCoroutineScope()
    val currentOnPicked by rememberUpdatedState(onPicked)
    return remember {
        PhotoPickerLauncher {
            val input = document.createElement("input") as HTMLInputElement
            input.style.display = "none"
            input.type = "file"
            input.accept = "image/*"
            input.onchange = {
                val file = input.files?.get(0)
                input.remove()
                if (file != null) scope.launch { file.readBytes().toJpeg()?.let(currentOnPicked) }
            }
            input.oncancel = { input.remove() }
            // Workaround: some browsers fire no events for an input outside the document.
            document.body?.appendChild(input)
            input.click()
        }
    }
}

private suspend fun File.readBytes(): ByteArray = suspendCancellableCoroutine { continuation ->
    val reader = FileReader()
    reader.onloadend = { event ->
        val bytes = if (readyState(event) == FileReader.DONE) {
            val array = Uint8Array(result(event))
            ByteArray(array.length) { array[it] }
        } else {
            ByteArray(0)
        }
        continuation.resume(bytes)
    }
    reader.readAsArrayBuffer(this)
}

@Suppress("UnusedParameter")
private fun readyState(event: Event): Short = js("event.target.readyState")

@Suppress("UnusedParameter")
private fun result(event: Event): ArrayBuffer = js("event.target.result")
