package com.xbot.kotogram.feature.connection.impl.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event

@Composable
actual fun isAppInForeground(): Boolean {
    var focused by remember { mutableStateOf(document.hasFocus()) }
    DisposableEffect(Unit) {
        val onFocus: (Event) -> Unit = { focused = true }
        val onBlur: (Event) -> Unit = { focused = false }
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
