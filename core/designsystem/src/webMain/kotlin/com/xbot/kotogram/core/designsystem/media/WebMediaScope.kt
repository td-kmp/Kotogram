/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Adapted from androidx.compose.ui.platform.WebMediaScope of CMP's jb-main (1.13.10).
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.xbot.kotogram.core.designsystem.media

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.Dp
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.FoldOrientation
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.FoldState
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.KeyboardKind
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PointerPrecision
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.ViewingDistance
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.WindowFold
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.WindowPosture
import kotlinx.browser.window
import org.w3c.dom.MediaQueryListEvent
import org.w3c.dom.events.EventTarget
import org.w3c.dom.mediacapture.AUDIOINPUT
import org.w3c.dom.mediacapture.MediaDeviceKind
import org.w3c.dom.mediacapture.MediaDevices
import org.w3c.dom.mediacapture.VIDEOINPUT
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.js
import kotlin.js.toList

internal class WebMediaScope(
    private val windowInfo: WindowInfo,
) : UiMediaScope {
    private val finePointer = MediaFeature("(any-pointer: fine)")
    private val coarsePointer = MediaFeature("(any-pointer: coarse)")
    private val hoveringPointer = MediaFeature("(any-hover: hover)")

    private val horizontalSegments = MediaFeature("(horizontal-viewport-segments: 2)")
    private val verticalSegments = MediaFeature("(vertical-viewport-segments: 2)")
    private val folded = MediaFeature("(device-posture: folded)")

    override val windowWidth: Dp
        get() = windowInfo.containerDpSize.width

    override val windowHeight: Dp
        get() = windowInfo.containerDpSize.height

    override val windowPosture: WindowPosture by derivedStateOf {
        val state = if (folded.value) FoldState.HalfOpened else FoldState.Flat
        WindowPosture(
            buildList {
                if (horizontalSegments.value) {
                    add(WindowFold(state, FoldOrientation.Vertical))
                }
                if (verticalSegments.value) {
                    add(WindowFold(state, FoldOrientation.Horizontal))
                }
            },
        )
    }

    override val pointerPrecision: PointerPrecision
        get() = when {
            finePointer.value -> PointerPrecision.Fine
            coarsePointer.value -> PointerPrecision.Coarse
            else -> PointerPrecision.None
        }

    // Workaround: browsers don't report a physical keyboard, so a fine pointer that hovers stands for one.
    private val hasLikelyPhysicalKeyboard: Boolean
        get() = finePointer.value && hoveringPointer.value

    private var virtualKeyboardVisible by mutableStateOf(isVirtualKeyboardVisible())

    override val keyboardKind: KeyboardKind
        get() = when {
            hasLikelyPhysicalKeyboard -> KeyboardKind.Physical
            virtualKeyboardVisible -> KeyboardKind.Virtual
            else -> KeyboardKind.None
        }

    override var hasMicrophone: Boolean by mutableStateOf(false)
        private set

    override var hasCamera: Boolean by mutableStateOf(false)
        private set

    override val viewingDistance: ViewingDistance
        get() = ViewingDistance.Near

    private var disposed = false
    private var enumerationGeneration = 0
    private val mediaDevices = getMediaDevices()

    private val keyboardEvents =
        getVirtualKeyboard()?.let { keyboard ->
            EventTargetListener(keyboard).apply {
                addDisposableEvent("geometrychange") {
                    virtualKeyboardVisible = isVirtualKeyboardVisible()
                }
            }
        }

    private val visualViewportEvents = getVisualViewport()?.let { viewport ->
        EventTargetListener(viewport).apply {
            addDisposableEvent("resize") {
                virtualKeyboardVisible = isVirtualKeyboardVisible()
            }
            addDisposableEvent("scroll") {
                virtualKeyboardVisible = isVirtualKeyboardVisible()
            }
        }
    }

    private val deviceEvents = mediaDevices?.let { devices ->
        EventTargetListener(devices).apply {
            addDisposableEvent("devicechange") { updateMediaDevices() }
        }
    }

    init {
        updateMediaDevices()
    }

    private fun updateMediaDevices() {
        val devices = mediaDevices ?: return
        val generation = ++enumerationGeneration
        enumerateMediaDevices(devices) { microphone, camera ->
            if (!disposed && generation == enumerationGeneration) {
                Snapshot.withMutableSnapshot {
                    hasMicrophone = microphone
                    hasCamera = camera
                }
            }
        }
    }

    fun dispose() {
        disposed = true
        finePointer.dispose()
        coarsePointer.dispose()
        hoveringPointer.dispose()
        horizontalSegments.dispose()
        verticalSegments.dispose()
        folded.dispose()
        keyboardEvents?.dispose()
        visualViewportEvents?.dispose()
        deviceEvents?.dispose()
    }
}

private class MediaFeature(
    query: String,
) {
    private val media = window.matchMedia(query)

    var value: Boolean by mutableStateOf(media.matches)
        private set

    private val events = EventTargetListener(media).apply {
        addDisposableEvent("change") { value = (it as MediaQueryListEvent).matches }
    }

    fun dispose() = events.dispose()
}

// language=js
private fun getVirtualKeyboard(): EventTarget? = js("(navigator.virtualKeyboard || null)")

// language=js
private fun getVisualViewport(): EventTarget? = js("(window.visualViewport || null)")

// language=js
private fun isVirtualKeyboardVisible(): Boolean = js(
    """(function() {
    const keyboard = navigator.virtualKeyboard;
    if (keyboard && keyboard.overlaysContent) return keyboard.boundingRect.height > 0;
    const viewport = window.visualViewport;
    return Boolean(viewport && viewport.scale === 1 &&
        window.innerHeight - viewport.height - viewport.offsetTop > 1);
})()""",
)

// language=js
private fun getMediaDevices(): MediaDevices? = js(
    """(
    navigator.mediaDevices && typeof navigator.mediaDevices.enumerateDevices === 'function'
        ? navigator.mediaDevices : null
)""",
)

private fun enumerateMediaDevices(
    devices: MediaDevices,
    onResult: (Boolean, Boolean) -> Unit,
) {
    devices.enumerateDevices().then<JsAny?>(
        onFulfilled = { result ->
            val inputs = result.toList()
            onResult(
                inputs.any { it.kind == MediaDeviceKind.AUDIOINPUT },
                inputs.any { it.kind == MediaDeviceKind.VIDEOINPUT },
            )
            null
        },
        onRejected = {
            onResult(false, false)
            null
        },
    )
}
