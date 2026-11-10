package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import com.xbot.kotogram.core.designsystem.component.IndexFastScrollerOverride
import com.xbot.kotogram.core.designsystem.component.LocalBottomSheetOverride
import com.xbot.kotogram.core.designsystem.component.LocalFastScrollerOverride
import com.xbot.kotogram.core.designsystem.component.LocalGlassIconButtonOverride
import com.xbot.kotogram.core.designsystem.component.LocalProgressIndicatorOverride
import com.xbot.kotogram.core.designsystem.component.LocalProminentButtonOverride
import com.xbot.kotogram.core.designsystem.component.LocalSearchFieldOverride
import com.xbot.kotogram.core.designsystem.component.LocalSwitchOverride

@Composable
internal actual fun platformComponentOverrides(): Array<ProvidedValue<*>> = listOfNotNull(
    LocalProgressIndicatorOverride provides ActivityIndicatorOverride,
    LocalBottomSheetOverride provides PageSheetOverride,
    LocalFastScrollerOverride provides IndexFastScrollerOverride,
    LocalSwitchOverride provides IosSwitchOverride,
    if (hasLiquidGlass) LocalProminentButtonOverride provides GlassProminentButtonOverride else null,
    if (hasLiquidGlass) LocalGlassIconButtonOverride provides IosGlassIconButtonOverride else null,
    if (hasLiquidGlass) LocalSearchFieldOverride provides GlassSearchFieldOverride else null,
).toTypedArray()
