package com.xbot.kotogram.core.designsystem.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.xbot.kotogram.core.designsystem.media.UiMediaScope
import com.xbot.kotogram.core.designsystem.media.rememberAndroidUiMediaScope

internal actual val currentPlatform: PlatformInfo = PlatformInfo(isApple = false, isDesktop = false)

@Composable
internal actual fun platformComponentOverrides(): Array<ProvidedValue<*>> = emptyArray()

@Composable
internal actual fun platformUiMediaScope(): UiMediaScope = rememberAndroidUiMediaScope()

@Composable
internal actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val context = LocalContext.current
    return remember(context, darkTheme) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
}
