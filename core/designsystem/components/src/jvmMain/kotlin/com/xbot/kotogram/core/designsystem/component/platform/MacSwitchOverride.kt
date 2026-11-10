package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.designsystem.appkit.MacToggleKind
import com.xbot.kotogram.core.designsystem.component.SwitchOverride
import com.xbot.kotogram.core.designsystem.component.SwitchOverrideScope

internal object MacSwitchOverride : SwitchOverride {
    @Composable
    override fun SwitchOverrideScope.Switch() {
        MacToggleView(MacToggleKind.Switch, checked, onCheckedChange, enabled, modifier)
    }
}
