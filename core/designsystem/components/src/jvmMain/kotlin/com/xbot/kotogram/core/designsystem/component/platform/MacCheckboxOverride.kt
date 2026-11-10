package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.designsystem.appkit.MacToggleKind
import com.xbot.kotogram.core.designsystem.component.CheckboxOverride
import com.xbot.kotogram.core.designsystem.component.CheckboxOverrideScope

internal object MacCheckboxOverride : CheckboxOverride {
    @Composable
    override fun CheckboxOverrideScope.Checkbox() {
        MacToggleView(MacToggleKind.Checkbox, checked, onCheckedChange, enabled, modifier)
    }
}
