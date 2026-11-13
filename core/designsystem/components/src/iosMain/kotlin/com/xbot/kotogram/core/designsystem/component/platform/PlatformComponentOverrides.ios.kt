package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import com.xbot.kotogram.core.designsystem.component.AppleTopBarOverride
import com.xbot.kotogram.core.designsystem.component.LocalBottomSheetOverride
import com.xbot.kotogram.core.designsystem.component.LocalFastScrollerOverride
import com.xbot.kotogram.core.designsystem.component.LocalGlassButtonOverride
import com.xbot.kotogram.core.designsystem.component.LocalGlassIconButtonOverride
import com.xbot.kotogram.core.designsystem.component.LocalProgressIndicatorOverride
import com.xbot.kotogram.core.designsystem.component.LocalScrollIndicatorOverride
import com.xbot.kotogram.core.designsystem.component.LocalSearchFieldOverride
import com.xbot.kotogram.core.designsystem.component.LocalSwitchOverride
import com.xbot.kotogram.core.designsystem.component.LocalTopBarOverride

@Composable
internal actual fun platformComponentOverrides(): Array<ProvidedValue<*>> = listOfNotNull(
    LocalProgressIndicatorOverride provides ActivityIndicatorOverride,
    LocalBottomSheetOverride provides PageSheetOverride,
    LocalFastScrollerOverride provides TableIndexFastScrollerOverride,
    LocalScrollIndicatorOverride provides IosScrollIndicatorOverride,
    LocalSwitchOverride provides IosSwitchOverride,
    LocalTopBarOverride provides AppleTopBarOverride,
    if (hasLiquidGlass) LocalGlassButtonOverride provides IosGlassButtonOverride else null,
    if (hasLiquidGlass) LocalGlassIconButtonOverride provides IosGlassIconButtonOverride else null,
    if (hasLiquidGlass) LocalSearchFieldOverride provides GlassSearchFieldOverride else null,
).toTypedArray()
