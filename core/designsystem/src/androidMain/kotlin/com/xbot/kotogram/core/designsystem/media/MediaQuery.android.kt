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

// androidx.compose.ui.adaptive.MediaQuery.android of androidx-main; rememberUiMediaScope attaches the listeners that
// AndroidComposeView attaches to it there.
package com.xbot.kotogram.core.designsystem.media

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import androidx.compose.runtime.annotation.FrequentlyChangingValue
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
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.FoldOrientation
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.FoldState
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.KeyboardKind
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PointerPrecision
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.ViewingDistance
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.WindowFold
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.WindowPosture
import kotlinx.coroutines.flow.collectLatest

@Stable
internal class UiMediaScopeImpl(
    context: Context,
    val inputManager: InputManager,
    private val windowInfo: WindowInfo,
    imeVisibility: Boolean
) : UiMediaScope {
    private val packageManager = context.packageManager
    var posture by mutableStateOf(WindowPosture(emptyList()))
    var anyPointer by mutableStateOf(resolvePointerPrecision(inputManager))
    var isDocked by mutableStateOf(false)
    var isImeVisible by mutableStateOf(imeVisibility)
    var hasPhysicalKeyboard by mutableStateOf(hasPhysicalKeyboard(inputManager))

    override val hasMicrophone: Boolean
        get() = packageManager.isMicAvailable()

    override val hasCamera: Boolean
        get() = packageManager.isCameraAvailable()

    @get:FrequentlyChangingValue
    override val windowWidth: Dp
        get() = windowInfo.containerDpSize.width

    @get:FrequentlyChangingValue
    override val windowHeight: Dp
        get() = windowInfo.containerDpSize.height

    override val windowPosture: WindowPosture
        get() = posture

    override val pointerPrecision: PointerPrecision
        get() = anyPointer

    override val keyboardKind: KeyboardKind
        get() = when {
            hasPhysicalKeyboard -> KeyboardKind.Physical
            isImeVisible -> KeyboardKind.Virtual
            else -> KeyboardKind.None
        }

    override val viewingDistance: ViewingDistance
        get() = when {
            packageManager.isTvDevice() -> ViewingDistance.Far
            packageManager.isAutomotiveDevice() || isDocked -> ViewingDistance.Medium
            else -> ViewingDistance.Near
        }
}

@Composable
internal fun rememberUiMediaScope(): UiMediaScope {
    val context = LocalContext.current
    val view = LocalView.current
    val windowInfo = LocalWindowInfo.current
    val scope = remember(context, windowInfo) {
        val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager
        UiMediaScopeImpl(context, inputManager, windowInfo, ViewCompat.getRootWindowInsets(view).isImeVisible)
    }
    LaunchedEffect(scope) {
        WindowInfoTracker.getOrCreate(context).windowLayoutInfo(context).collectLatest { layout ->
            scope.posture = resolvePosture(layout)
        }
    }
    DisposableEffect(scope) {
        val inputManager = scope.inputManager
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
        val stickyIntent = ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_DOCK_EVENT),
            ContextCompat.RECEIVER_EXPORTED
        )
        scope.isDocked = isDocked(stickyIntent)
        onDispose { context.unregisterReceiver(receiver) }
    }
    return scope
}

internal fun resolvePosture(layoutInfo: WindowLayoutInfo?): WindowPosture {
    if (layoutInfo == null || layoutInfo.displayFeatures.isEmpty()) return WindowPosture(emptyList())

    val features = layoutInfo.displayFeatures
    val folds = ArrayList<WindowFold>(features.size)
    for (i in features.indices) {
        val feature = features[i]
        if (feature is FoldingFeature) {
            val foldState = when (feature.state) {
                FoldingFeature.State.HALF_OPENED -> FoldState.HalfOpened
                else -> FoldState.Flat
            }
            val orientation = when (feature.orientation) {
                FoldingFeature.Orientation.HORIZONTAL -> FoldOrientation.Horizontal
                else -> FoldOrientation.Vertical
            }
            folds.add(WindowFold(foldState, orientation))
        }
    }
    return WindowPosture(folds)
}

internal fun hasPhysicalKeyboard(inputManager: InputManager?): Boolean {
    if (inputManager == null) return false

    return inputManager.inputDeviceIds?.any { id ->
        val device = inputManager.getInputDevice(id) ?: return@any false
        device.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC && !device.isVirtual
    } ?: false
}

@Suppress("CyclomaticComplexMethod")
internal fun resolvePointerPrecision(inputManager: InputManager?): PointerPrecision {
    if (inputManager == null) return PointerPrecision.None

    var pointerPrecision = PointerPrecision.None
    var fallbackPrecision = PointerPrecision.None

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
            pointerPrecision = PointerPrecision.Coarse
        } else if (
            pointerPrecision == PointerPrecision.None &&
            (
                device.hasValidPointerSource(InputDevice.SOURCE_JOYSTICK) ||
                    device.hasValidPointerSource(InputDevice.SOURCE_GAMEPAD)
                )
        ) {
            pointerPrecision = PointerPrecision.Blunt
        }

        // Emulators report their pointers this way; kept apart so a real device's source wins.
        if (device.hasFallbackFinePointer()) {
            fallbackPrecision = PointerPrecision.Fine
        } else if (fallbackPrecision != PointerPrecision.Fine && device.hasFallbackCoarsePointer()) {
            fallbackPrecision = PointerPrecision.Coarse
        }
    }

    if (pointerPrecision != PointerPrecision.None) return pointerPrecision

    return fallbackPrecision
}

private fun InputDevice.hasValidPointerSource(source: Int, axis: Int = MotionEvent.AXIS_X): Boolean =
    (sources and source == source) && getMotionRange(axis, source) != null

private fun InputDevice.hasFallbackFinePointer(): Boolean =
    getMotionRange(MotionEvent.AXIS_X)?.isFromSource(InputDevice.SOURCE_MOUSE) == true &&
        getMotionRange(MotionEvent.AXIS_VSCROLL) != null

private fun InputDevice.hasFallbackCoarsePointer(): Boolean =
    getMotionRange(MotionEvent.AXIS_X)?.isFromSource(InputDevice.SOURCE_TOUCHSCREEN) == true &&
        (
            getMotionRange(MotionEvent.AXIS_TOUCH_MAJOR) != null ||
                getMotionRange(MotionEvent.AXIS_TOUCH_MINOR) != null
            )

internal val WindowInsetsCompat?.isImeVisible: Boolean
    get() = this?.isVisible(WindowInsetsCompat.Type.ime()) == true

internal fun isDocked(intent: Intent?): Boolean {
    if (intent == null) return false
    val dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, Intent.EXTRA_DOCK_STATE_UNDOCKED)
    return dockState != Intent.EXTRA_DOCK_STATE_UNDOCKED
}

private fun PackageManager.isCameraAvailable(): Boolean = hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

private fun PackageManager.isMicAvailable(): Boolean = hasSystemFeature(PackageManager.FEATURE_MICROPHONE)

private fun PackageManager.isAutomotiveDevice(): Boolean = hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)

private fun PackageManager.isTvDevice(): Boolean = hasSystemFeature(PackageManager.FEATURE_LEANBACK)
