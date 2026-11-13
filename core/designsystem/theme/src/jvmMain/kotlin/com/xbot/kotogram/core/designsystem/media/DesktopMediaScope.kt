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

// Adapted from androidx.compose.ui.platform.MediaScope.desktop of CMP's jb-main (1.13.0-beta01).
package com.xbot.kotogram.core.designsystem.media

import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.Dp

internal class DesktopMediaScope(
    val windowInfo: WindowInfo,
) : UiMediaScope {

    override val windowPosture: UiMediaScope.WindowPosture = UiMediaScope.WindowPosture([])

    override val windowWidth: Dp
        get() = windowInfo.containerDpSize.width

    override val windowHeight: Dp
        get() = windowInfo.containerDpSize.height

    override val pointerPrecision: UiMediaScope.PointerPrecision
        get() = UiMediaScope.PointerPrecision.Fine

    override val keyboardKind: UiMediaScope.KeyboardKind
        get() = UiMediaScope.KeyboardKind.Physical

    override val hasMicrophone: Boolean
        get() = true

    override val hasCamera: Boolean
        get() = false

    override val viewingDistance: UiMediaScope.ViewingDistance
        get() = UiMediaScope.ViewingDistance.Near
}
