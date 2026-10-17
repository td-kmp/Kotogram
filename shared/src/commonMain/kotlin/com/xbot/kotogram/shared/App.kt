package com.xbot.kotogram.shared

import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.shared.root.RootNavigation

@Composable
fun App() {
    KotogramTheme {
        RootNavigation()
    }
}
