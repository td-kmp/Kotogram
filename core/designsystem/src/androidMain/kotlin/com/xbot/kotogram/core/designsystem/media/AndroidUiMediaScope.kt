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

// androidx.compose.ui.adaptive.MediaQuery.android of Compose UI 1.12.1, with platformFeatures from aosp/4304334.
package com.xbot.kotogram.core.designsystem.media

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.input.InputManager
import android.os.Handler
import android.os.Looper
import android.view.InputDevice
import android.view.MotionEvent
import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.KeyboardKind
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PlatformFeature
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PointerPrecision
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.Posture
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.ViewingDistance
import kotlinx.coroutines.flow.collectLatest

@Stable
private class AndroidUiMediaScope(
    context: Context,
    inputManager: InputManager,
    private val windowInfo: WindowInfo,
    imeVisibility: Boolean
) : UiMediaScope {
    var posture by mutableStateOf(Posture.Flat)
    var anyPointer by mutableStateOf(resolvePointerPrecision(inputManager))
    var isDocked by mutableStateOf(false)
    var isImeVisible by mutableStateOf(imeVisibility)
    var hasPhysicalKeyboard by mutableStateOf(hasPhysicalKeyboard(inputManager))

    override val platformFeatures: Set<PlatformFeature> = AndroidPlatformFeatures
        .filter { context.packageManager.hasSystemFeature(it.name) }
        .toSet()

    override val hasMicrophone: Boolean get() = PlatformFeature.Microphone in platformFeatures

    override val hasCamera: Boolean get() = PlatformFeature.Camera in platformFeatures

    override val windowWidth: Dp get() = windowInfo.containerDpSize.width

    override val windowHeight: Dp get() = windowInfo.containerDpSize.height

    override val windowPosture: Posture get() = posture

    override val pointerPrecision: PointerPrecision get() = anyPointer

    override val keyboardKind: KeyboardKind
        get() = when {
            hasPhysicalKeyboard -> KeyboardKind.Physical
            isImeVisible -> KeyboardKind.Virtual
            else -> KeyboardKind.None
        }

    override val viewingDistance: ViewingDistance
        get() = when {
            PlatformFeature.Leanback in platformFeatures -> ViewingDistance.Far
            PlatformFeature.Automotive in platformFeatures || isDocked -> ViewingDistance.Medium
            else -> ViewingDistance.Near
        }
}

@Composable
internal fun rememberAndroidUiMediaScope(): UiMediaScope {
    val context = LocalContext.current
    val view = LocalView.current
    val windowInfo = LocalWindowInfo.current
    val inputManager = remember(context) { context.getSystemService(Context.INPUT_SERVICE) as InputManager }
    val scope = remember(context, windowInfo) {
        AndroidUiMediaScope(context, inputManager, windowInfo, ViewCompat.getRootWindowInsets(view).isImeVisible)
    }
    LaunchedEffect(scope) {
        WindowInfoTracker.getOrCreate(context).windowLayoutInfo(context).collectLatest { layout ->
            scope.posture = resolvePosture(layout)
        }
    }
    DisposableEffect(scope) {
        val listener = object : InputManager.InputDeviceListener {
            override fun onInputDeviceAdded(id: Int) = update()

            override fun onInputDeviceRemoved(id: Int) = update()

            override fun onInputDeviceChanged(id: Int) = update()

            fun update() {
                scope.anyPointer = resolvePointerPrecision(inputManager)
                scope.hasPhysicalKeyboard = hasPhysicalKeyboard(inputManager)
            }
        }
        inputManager.registerInputDeviceListener(listener, Handler(Looper.getMainLooper()))
        listener.update()
        onDispose { inputManager.unregisterInputDeviceListener(listener) }
    }
    DisposableEffect(scope, view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            scope.isImeVisible = ViewCompat.getRootWindowInsets(view).isImeVisible
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }
    DisposableEffect(scope) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                scope.isDocked = isDocked(intent)
            }
        }
        val sticky = ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_DOCK_EVENT),
            ContextCompat.RECEIVER_EXPORTED
        )
        scope.isDocked = isDocked(sticky)
        onDispose { context.unregisterReceiver(receiver) }
    }
    return scope
}

private val AndroidPlatformFeatures = listOf(
    PlatformFeature.Leanback,
    PlatformFeature.Automotive,
    PlatformFeature.Pc,
    PlatformFeature.Watch,
    PlatformFeature.Camera,
    PlatformFeature.Microphone
)

private fun resolvePosture(layoutInfo: WindowLayoutInfo): Posture {
    val fold = layoutInfo.displayFeatures.filterIsInstance<FoldingFeature>()
        .firstOrNull { it.state == FoldingFeature.State.HALF_OPENED }
        ?: return Posture.Flat
    return if (fold.orientation == FoldingFeature.Orientation.HORIZONTAL) Posture.Tabletop else Posture.Book
}

private fun hasPhysicalKeyboard(inputManager: InputManager): Boolean = inputManager.inputDeviceIds.any { id ->
    val device = inputManager.getInputDevice(id) ?: return@any false
    device.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC && !device.isVirtual
}

@Suppress("CyclomaticComplexMethod")
private fun resolvePointerPrecision(inputManager: InputManager): PointerPrecision {
    var precision = PointerPrecision.None
    var fallback = PointerPrecision.None
    for (id in inputManager.inputDeviceIds) {
        val device = inputManager.getInputDevice(id) ?: continue
        if (
            device.hasValidPointerSource(InputDevice.SOURCE_MOUSE) ||
            device.hasValidPointerSource(InputDevice.SOURCE_STYLUS) ||
            device.hasValidPointerSource(InputDevice.SOURCE_TOUCHPAD)
        ) {
            return PointerPrecision.Fine
        }
        if (device.hasValidPointerSource(InputDevice.SOURCE_TOUCHSCREEN)) {
            precision = PointerPrecision.Coarse
        } else if (
            precision == PointerPrecision.None &&
            (
                device.hasValidPointerSource(InputDevice.SOURCE_JOYSTICK) ||
                    device.hasValidPointerSource(InputDevice.SOURCE_GAMEPAD)
                )
        ) {
            precision = PointerPrecision.Blunt
        }
        // Emulators report their pointers this way; kept apart so a real device's source wins.
        if (device.hasFallbackFinePointer()) {
            fallback = PointerPrecision.Fine
        } else if (fallback != PointerPrecision.Fine && device.hasFallbackCoarsePointer()) {
            fallback = PointerPrecision.Coarse
        }
    }
    return if (precision != PointerPrecision.None) precision else fallback
}

private fun InputDevice.hasValidPointerSource(source: Int): Boolean =
    sources and source == source && getMotionRange(MotionEvent.AXIS_X, source) != null

private fun InputDevice.hasFallbackFinePointer(): Boolean =
    getMotionRange(MotionEvent.AXIS_X)?.isFromSource(InputDevice.SOURCE_MOUSE) == true &&
        getMotionRange(MotionEvent.AXIS_VSCROLL) != null

private fun InputDevice.hasFallbackCoarsePointer(): Boolean =
    getMotionRange(MotionEvent.AXIS_X)?.isFromSource(InputDevice.SOURCE_TOUCHSCREEN) == true &&
        (
            getMotionRange(MotionEvent.AXIS_TOUCH_MAJOR) != null ||
                getMotionRange(MotionEvent.AXIS_TOUCH_MINOR) != null
            )

private val WindowInsetsCompat?.isImeVisible: Boolean
    get() = this?.isVisible(WindowInsetsCompat.Type.ime()) == true

private fun isDocked(intent: Intent?): Boolean {
    val state = intent?.getIntExtra(Intent.EXTRA_DOCK_STATE, Intent.EXTRA_DOCK_STATE_UNDOCKED)
    return state != null && state != Intent.EXTRA_DOCK_STATE_UNDOCKED
}
