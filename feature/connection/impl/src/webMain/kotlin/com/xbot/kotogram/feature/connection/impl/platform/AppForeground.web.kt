package com.xbot.kotogram.feature.connection.impl.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event

@Composable
internal actual fun rememberAppForeground(): State<Boolean> {
    val focused = remember { mutableStateOf(document.hasFocus()) }
    DisposableEffect(Unit) {
        val onFocus: (Event) -> Unit = { focused.value = true }
        val onBlur: (Event) -> Unit = { focused.value = false }
        window.addEventListener(FOCUS, onFocus)
        window.addEventListener(BLUR, onBlur)
        onDispose {
            window.removeEventListener(FOCUS, onFocus)
            window.removeEventListener(BLUR, onBlur)
        }
    }
    return focused
}

private const val FOCUS = "focus"
private const val BLUR = "blur"
