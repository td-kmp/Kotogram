package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.designsystem.component.SwitchOverride
import com.xbot.kotogram.core.designsystem.component.SwitchOverrideScope
import com.xbot.kotogram.core.designsystem.native.MacToggle

internal object MacSwitchOverride : SwitchOverride {
    @Composable
    override fun SwitchOverrideScope.Switch() {
        MacToggleView(MacToggle::switch, checked, onCheckedChange, enabled, modifier)
    }
}
