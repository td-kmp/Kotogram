package com.xbot.kotogram.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.xbot.kotogram.core.designsystem.component.platform.platformComponentOverrides

@Composable
fun ProvidePlatformComponents(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        *platformComponentOverrides(),
        LocalImageLoader provides rememberImageLoader(),
        content = content,
    )
}
