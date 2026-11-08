package com.xbot.kotogram.core.designsystem.theme

import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.xbot.kotogram.core.designsystem.media.UiMediaScope
import com.xbot.kotogram.core.designsystem.media.rememberUiMediaScope
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo

internal actual val currentPlatform: PlatformInfo = PlatformInfo(isApple = false, isDesktop = false)

@Composable
internal actual fun platformComponentOverrides(): Array<ProvidedValue<*>> = emptyArray()

@Composable
internal actual fun platformUiMediaScope(): UiMediaScope = rememberUiMediaScope()

// Material 3's mediaQueryInfo (ComponentStyles.android.kt of androidx-main).
@Composable
internal actual fun platformMediaQueryInfo(): MediaQueryInfo {
    val packageManager = LocalContext.current.packageManager
    return remember(packageManager) {
        MediaQueryInfo(
            isLaptop = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && packageManager.isLaptop(),
            isTv = packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK),
            isAuto = packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O_MR1)
private fun PackageManager.isLaptop() = hasSystemFeature(PackageManager.FEATURE_PC)

@Composable
internal actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val context = LocalContext.current
    return remember(context, darkTheme) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
}
