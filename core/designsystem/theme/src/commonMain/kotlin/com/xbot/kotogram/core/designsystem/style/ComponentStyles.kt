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

// Adapted from material3's ComponentStyles.kt of androidx-main, the stand-in for the Style API's own properties.
package com.xbot.kotogram.core.designsystem.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.xbot.kotogram.core.designsystem.KotogramTheme

interface StyleResolver {
    fun resolve(block: () -> Unit)
}

class DefaultStyleResolver : StyleResolver {
    private val pendingBlocks: MutableList<() -> Unit> = mutableListOf()
    private var resolvingIndex = -1

    override fun resolve(block: () -> Unit) {
        pendingBlocks.add(block)
        if (resolvingIndex == -1) {
            resolvingIndex = 0
            while (resolvingIndex < pendingBlocks.size) {
                pendingBlocks[resolvingIndex++]()
            }
            pendingBlocks.clear()
            resolvingIndex = -1
        }
    }
}

class MediaQueryInfo(
    val isLaptop: Boolean,
    val isTv: Boolean,
    val isAuto: Boolean,
)

val LocalMediaQueryInfo = staticCompositionLocalOf { MediaQueryInfo(isLaptop = false, isTv = false, isAuto = false) }

@Composable
@ReadOnlyComposable
fun mediaQueryInfo(): MediaQueryInfo = LocalMediaQueryInfo.current

@Suppress("UNCHECKED_CAST")
interface AdaptiveStyleScope<T : AdaptiveStyleScope<T>> {
    val mediaQueryInfo: MediaQueryInfo

    fun laptop(style: T.() -> Unit) {
        if (mediaQueryInfo.isLaptop) {
            (this as T).style()
        }
    }

    fun tv(style: T.() -> Unit) {
        if (mediaQueryInfo.isTv) {
            (this as T).style()
        }
    }

    fun auto(style: T.() -> Unit) {
        if (mediaQueryInfo.isAuto) {
            (this as T).style()
        }
    }
}

fun <T : StyleResolver> T.resolve(style: ComponentStyle<T>): T {
    resolve { with(style) { applyStyle() } }
    return this
}

fun interface ComponentStyle<T : StyleResolver> {
    fun T.applyStyle()
}

@Composable
fun <T : StyleResolver> rememberStyle(
    scope: (theme: KotogramTheme.Values, mediaQueryInfo: MediaQueryInfo) -> T,
    style: ComponentStyle<T>,
): T {
    val theme = KotogramTheme.values
    val mediaQueryInfo = mediaQueryInfo()
    return remember(theme, mediaQueryInfo, style) { scope(theme, mediaQueryInfo).resolve(style) }
}
