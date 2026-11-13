package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import com.xbot.kotogram.core.designsystem.appkit.MacGlassButton
import com.xbot.kotogram.core.designsystem.component.AppleTopBarOverride
import com.xbot.kotogram.core.designsystem.component.LocalCheckboxOverride
import com.xbot.kotogram.core.designsystem.component.LocalGlassButtonOverride
import com.xbot.kotogram.core.designsystem.component.LocalGlassIconButtonOverride
import com.xbot.kotogram.core.designsystem.component.LocalMenuOverride
import com.xbot.kotogram.core.designsystem.component.LocalProgressIndicatorOverride
import com.xbot.kotogram.core.designsystem.component.LocalScrollIndicatorOverride
import com.xbot.kotogram.core.designsystem.component.LocalSwitchOverride
import com.xbot.kotogram.core.designsystem.component.LocalTopBarOverride
import com.xbot.kotogram.core.designsystem.theme.currentPlatform
import dev.nucleusframework.window.tao.LocalTaoWindow

@Composable
internal actual fun platformComponentOverrides(): Array<ProvidedValue<*>> {
    if (!currentPlatform.isApple || LocalTaoWindow.current == null) return []
    return listOfNotNull(
        LocalProgressIndicatorOverride provides MacSpinnerOverride,
        LocalMenuOverride provides MacPopUpMenuOverride,
        LocalSwitchOverride provides MacSwitchOverride,
        LocalCheckboxOverride provides MacCheckboxOverride,
        LocalScrollIndicatorOverride provides MacScrollIndicatorOverride,
        LocalTopBarOverride provides AppleTopBarOverride,
        if (hasGlassButtons) LocalGlassButtonOverride provides MacGlassButtonOverride else null,
        if (hasGlassButtons) LocalGlassIconButtonOverride provides MacGlassIconButtonOverride else null,
    ).toTypedArray()
}

private val hasGlassButtons: Boolean by lazy { MacGlassButton.isSupported }
