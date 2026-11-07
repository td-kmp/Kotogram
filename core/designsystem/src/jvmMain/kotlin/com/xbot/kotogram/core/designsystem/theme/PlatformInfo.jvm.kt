package com.xbot.kotogram.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import com.xbot.kotogram.core.designsystem.component.LocalMenuOverride
import com.xbot.kotogram.core.designsystem.component.LocalProgressIndicatorOverride
import com.xbot.kotogram.core.designsystem.component.LocalProminentButtonOverride
import com.xbot.kotogram.core.designsystem.component.platform.MacGlassProminentButtonOverride
import com.xbot.kotogram.core.designsystem.component.platform.MacPopUpMenuOverride
import com.xbot.kotogram.core.designsystem.component.platform.MacSpinnerOverride
import com.xbot.kotogram.core.designsystem.media.Pc
import com.xbot.kotogram.core.designsystem.media.PlatformUiMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.KeyboardKind
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PlatformFeature
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PointerPrecision
import com.xbot.kotogram.core.designsystem.native.MacGlassButton
import dev.nucleusframework.window.tao.LocalTaoWindow

internal actual val currentPlatform: PlatformInfo =
    PlatformInfo(isApple = System.getProperty("os.name").orEmpty().startsWith("Mac"), isDesktop = true)

@Composable
internal actual fun platformComponentOverrides(): Array<ProvidedValue<*>> {
    if (!currentPlatform.isApple || LocalTaoWindow.current == null) return emptyArray()
    return listOfNotNull(
        LocalProgressIndicatorOverride provides MacSpinnerOverride,
        LocalMenuOverride provides MacPopUpMenuOverride,
        if (MacGlassButton.isSupported) LocalProminentButtonOverride provides MacGlassProminentButtonOverride else null
    ).toTypedArray()
}

// A desktop is taken to have a mouse and a keyboard; Compose for desktop doesn't report its devices.
@Composable
internal actual fun platformUiMediaScope(): UiMediaScope {
    val windowInfo = LocalWindowInfo.current
    return remember(windowInfo) {
        PlatformUiMediaScope(windowInfo, PointerPrecision.Fine, KeyboardKind.Physical, setOf(PlatformFeature.Pc))
    }
}

@Composable
internal actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? = null
