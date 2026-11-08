package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.designsystem.component.CheckboxOverride
import com.xbot.kotogram.core.designsystem.component.CheckboxOverrideScope
import com.xbot.kotogram.core.designsystem.native.MacToggle

internal object MacCheckboxOverride : CheckboxOverride {
    @Composable
    override fun CheckboxOverrideScope.Checkbox() {
        MacToggleView(MacToggle::checkbox, checked, onCheckedChange, enabled, modifier)
    }
}
