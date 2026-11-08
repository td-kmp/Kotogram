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

// androidx.compose.ui.MediaQuery of androidx-main (Compose UI 1.13), which CMP 1.12.1 provides only on Android, without
// computedMediaQuery: its computedStateOf is 1.13's runtime. Moving to the official one is a change of imports.
package com.xbot.kotogram.core.designsystem.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalAccessorScope
import androidx.compose.runtime.Immutable
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
    val windowPosture: WindowPosture

    @get:FrequentlyChangingValue
    val windowWidth: Dp

    @get:FrequentlyChangingValue
    val windowHeight: Dp

    val pointerPrecision: PointerPrecision

    val keyboardKind: KeyboardKind

    val hasMicrophone: Boolean

    val hasCamera: Boolean

    val viewingDistance: ViewingDistance

    @Immutable
    class WindowFold(val state: FoldState, val orientation: FoldOrientation) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is WindowFold) return false
            return state == other.state && orientation == other.orientation
        }

        override fun hashCode(): Int = state.value * 31 + orientation.value

        override fun toString(): String = "WindowFold(state=$state, orientation=$orientation)"
    }

    @Immutable
    class WindowPosture(val folds: List<WindowFold>) {
        val isFlat: Boolean
            get() = folds.none { it.state == FoldState.HalfOpened }

        val isTabletop: Boolean
            get() = folds.any { it.state == FoldState.HalfOpened && it.orientation == FoldOrientation.Horizontal }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is WindowPosture) return false
            return folds == other.folds
        }

        override fun hashCode(): Int = folds.hashCode()

        override fun toString(): String = "WindowPosture(folds=$folds)"
    }

    @JvmInline
    value class FoldState internal constructor(internal val value: Int) {
        override fun toString(): String = when (this) {
            Flat -> "Flat"
            HalfOpened -> "HalfOpened"
            else -> "Unknown"
        }

        companion object {
            val Flat: FoldState = FoldState(0)
            val HalfOpened: FoldState = FoldState(1)
        }
    }

    @JvmInline
    value class FoldOrientation internal constructor(internal val value: Int) {
        override fun toString(): String = when (this) {
            Horizontal -> "Horizontal"
            Vertical -> "Vertical"
            else -> "Unknown"
        }

        companion object {
            val Horizontal: FoldOrientation = FoldOrientation(0)
            val Vertical: FoldOrientation = FoldOrientation(1)
        }
    }

    @JvmInline
    value class PointerPrecision private constructor(private val value: Int) {
        override fun toString(): String = when (this) {
            Fine -> "Fine"
            Coarse -> "Coarse"
            Blunt -> "Blunt"
            None -> "None"
            else -> "Unknown"
        }

        companion object {
            val Fine: PointerPrecision get() = PointerPrecision(0)
            val Coarse: PointerPrecision get() = PointerPrecision(1)
            val Blunt: PointerPrecision get() = PointerPrecision(2)
            val None: PointerPrecision get() = PointerPrecision(3)
        }
    }

    @JvmInline
    value class KeyboardKind private constructor(private val value: Int) {
        override fun toString(): String = when (this) {
            Physical -> "Physical"
            Virtual -> "Virtual"
            None -> "None"
            else -> "Unknown"
        }

        companion object {
            val Physical: KeyboardKind get() = KeyboardKind(0)
            val Virtual: KeyboardKind get() = KeyboardKind(1)
            val None: KeyboardKind get() = KeyboardKind(2)
        }
    }

    @JvmInline
    value class ViewingDistance private constructor(private val value: Int) {
        override fun toString(): String = when (this) {
            Near -> "Near"
            Medium -> "Medium"
            Far -> "Far"
            else -> "Unknown"
        }

        companion object {
            val Near: ViewingDistance get() = ViewingDistance(0)
            val Medium: ViewingDistance get() = ViewingDistance(1)
            val Far: ViewingDistance get() = ViewingDistance(2)
        }
    }
}

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
