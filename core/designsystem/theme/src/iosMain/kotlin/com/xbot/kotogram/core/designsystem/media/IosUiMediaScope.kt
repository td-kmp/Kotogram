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

// Adapted from androidx.compose.ui.platform.MediaScope.ios of CMP's jb-main (1.13.0-beta01), without the touch type.
package com.xbot.kotogram.core.designsystem.media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.Dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSValue
import platform.UIKit.CGRectValue
import platform.UIKit.UIKeyboardFrameEndUserInfoKey
import platform.UIKit.UIKeyboardWillChangeFrameNotification
import platform.UIKit.UIKeyboardWillHideNotification
import platform.UIKit.UIKeyboardWillShowNotification
import platform.darwin.NSObjectProtocol

internal class IosUiMediaScope(
    val windowInfo: WindowInfo,
) : UiMediaScope {
    private var isImeShowing by mutableStateOf(false)
    private var keyboardObservers = emptyList<NSObjectProtocol>()

    // Workaround: iOS 27.1's UIHingeInteraction isn't in Kotlin 2.4.20's UIKit, built from the iOS 26.4 SDK.
    override val windowPosture: UiMediaScope.WindowPosture = UiMediaScope.WindowPosture([])

    override val windowWidth: Dp
        get() = windowInfo.containerDpSize.width

    override val windowHeight: Dp
        get() = windowInfo.containerDpSize.height

    override val pointerPrecision: UiMediaScope.PointerPrecision
        get() = UiMediaScope.PointerPrecision.Coarse

    override val keyboardKind: UiMediaScope.KeyboardKind
        get() = when {
            isImeShowing -> UiMediaScope.KeyboardKind.Virtual
            else -> UiMediaScope.KeyboardKind.None
        }

    // Workaround: CMP asks AVCaptureDevice, which would have the app link AVFoundation: the framework is static.
    override val hasMicrophone: Boolean
        get() = true

    override val hasCamera: Boolean
        get() = true

    override val viewingDistance: UiMediaScope.ViewingDistance
        get() = UiMediaScope.ViewingDistance.Near

    fun startObserving() {
        keyboardObservers = [
            observe(UIKeyboardWillShowNotification) { isImeShowing = it.keyboardHeight > 0 },
            observe(UIKeyboardWillChangeFrameNotification) { isImeShowing = it.keyboardHeight > 0 },
            observe(UIKeyboardWillHideNotification) { isImeShowing = false },
        ]
    }

    fun stopObserving() {
        keyboardObservers.forEach(NSNotificationCenter.defaultCenter::removeObserver)
        keyboardObservers = []
    }
}

private fun observe(
    name: String?,
    block: (NSNotification?) -> Unit,
): NSObjectProtocol =
    NSNotificationCenter.defaultCenter.addObserverForName(name, null, NSOperationQueue.mainQueue, block)

@OptIn(ExperimentalForeignApi::class)
private val NSNotification?.keyboardHeight: Double
    get() = (this?.userInfo?.get(UIKeyboardFrameEndUserInfoKey) as? NSValue)
        ?.CGRectValue()
        ?.useContents { size.height }
        ?: 0.0
