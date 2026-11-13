package com.xbot.kotogram.core.designsystem.window

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.kotogramColorScheme
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.NucleusApplicationScope
import dev.nucleusframework.application.NucleusDecoratedWindowScope
import dev.nucleusframework.window.NucleusDecoratedWindowTheme
import dev.nucleusframework.window.material.rememberMaterialTitleBarStyle
import dev.nucleusframework.window.material.rememberMaterialWindowStyle

@Composable
fun NucleusApplicationScope.KotogramWindow(
    onCloseRequest: () -> Unit,
    state: WindowState = rememberWindowState(),
    title: String = "",
    minimumSize: DpSize? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable NucleusDecoratedWindowScope.() -> Unit,
) {
    val colorScheme = kotogramColorScheme(darkTheme, dynamicColor)
    NucleusDecoratedWindowTheme(
        isDark = darkTheme,
        windowStyle = rememberMaterialWindowStyle(colorScheme),
        titleBarStyle = rememberMaterialTitleBarStyle(colorScheme.copy(surface = colorScheme.background)),
    ) {
        DecoratedWindow(
            onCloseRequest = onCloseRequest,
            state = state,
            title = title,
            minimumSize = minimumSize,
        ) {
            val window = this
            val base = LocalWindowInfo.current
            val windowInfo = remember(base, window) { KotogramWindowInfo(base, window) }
            CompositionLocalProvider(LocalWindowInfo provides windowInfo) {
                KotogramTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
                    window.content()
                }
            }
        }
    }
}
