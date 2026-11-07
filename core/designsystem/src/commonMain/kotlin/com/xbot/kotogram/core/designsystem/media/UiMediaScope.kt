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

// androidx.compose.ui.MediaQuery of Compose UI 1.12.1, whose UiMediaScope CMP provides only on Android, plus
// platformFeatures from aosp/4304334. Moving to the official one is a change of imports.
package com.xbot.kotogram.core.designsystem.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalAccessorScope
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.annotation.FrequentlyChangingValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.unit.Dp
import kotlin.jvm.JvmInline

interface UiMediaScope {
    val windowPosture: Posture

    @get:FrequentlyChangingValue
    val windowWidth: Dp

    @get:FrequentlyChangingValue
    val windowHeight: Dp

    val pointerPrecision: PointerPrecision

    val keyboardKind: KeyboardKind

    val platformFeatures: Set<PlatformFeature>

    val hasMicrophone: Boolean

    val hasCamera: Boolean

    val viewingDistance: ViewingDistance

    @JvmInline
    value class Posture private constructor(private val description: String) {
        override fun toString(): String = description

        companion object {
            val Flat = Posture("Flat")
            val Tabletop = Posture("Tabletop")
            val Book = Posture("Book")
        }
    }

    @JvmInline
    value class PointerPrecision private constructor(private val description: String) {
        override fun toString(): String = description

        companion object {
            val Fine = PointerPrecision("Fine")
            val Coarse = PointerPrecision("Coarse")
            val Blunt = PointerPrecision("Blunt")
            val None = PointerPrecision("None")
        }
    }

    @JvmInline
    value class KeyboardKind private constructor(private val description: String) {
        override fun toString(): String = description

        companion object {
            val Physical = KeyboardKind("Physical")
            val Virtual = KeyboardKind("Virtual")
            val None = KeyboardKind("None")
        }
    }

    @JvmInline
    value class PlatformFeature internal constructor(internal val name: String) {
        override fun toString(): String = name

        companion object
    }

    @JvmInline
    value class ViewingDistance private constructor(private val description: String) {
        override fun toString(): String = description

        companion object {
            val Near = ViewingDistance("Near")
            val Medium = ViewingDistance("Medium")
            val Far = ViewingDistance("Far")
        }
    }
}

// Android's feature names, as aosp/4304334 declares them in androidMain; common here for every platform to report.
val UiMediaScope.PlatformFeature.Companion.Leanback
    get() = UiMediaScope.PlatformFeature("android.software.leanback")
val UiMediaScope.PlatformFeature.Companion.Automotive
    get() = UiMediaScope.PlatformFeature("android.hardware.type.automotive")
val UiMediaScope.PlatformFeature.Companion.Pc
    get() = UiMediaScope.PlatformFeature("android.hardware.type.pc")
val UiMediaScope.PlatformFeature.Companion.Watch
    get() = UiMediaScope.PlatformFeature("android.hardware.type.watch")
val UiMediaScope.PlatformFeature.Companion.Camera
    get() = UiMediaScope.PlatformFeature("android.hardware.camera.any")
val UiMediaScope.PlatformFeature.Companion.Microphone
    get() = UiMediaScope.PlatformFeature("android.hardware.microphone")

val LocalUiMediaScope =
    staticCompositionLocalOf<UiMediaScope> {
        error("CompositionLocal LocalUiMediaScope not present")
    }

@Composable
@ReadOnlyComposable
inline fun <T> mediaQuery(query: UiMediaScope.() -> T): T = LocalUiMediaScope.current.query()

@Composable
fun <T> derivedMediaQuery(query: UiMediaScope.() -> T): State<T> {
    val mediaScope = LocalUiMediaScope.current
    val currentQuery by rememberUpdatedState(query)
    return remember(mediaScope) { derivedStateOf { mediaScope.currentQuery() } }
}

inline fun <T> CompositionLocalAccessorScope.mediaQuery(query: UiMediaScope.() -> T): T =
    LocalUiMediaScope.currentValue.query()

inline fun <T> CompositionLocalConsumerModifierNode.mediaQuery(query: UiMediaScope.() -> T): T =
    currentValueOf(LocalUiMediaScope).query()
