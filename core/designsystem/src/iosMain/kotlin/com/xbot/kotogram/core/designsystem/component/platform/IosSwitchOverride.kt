package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.SwitchOverride
import com.xbot.kotogram.core.designsystem.component.SwitchOverrideScope
import com.xbot.kotogram.core.designsystem.extensions.isDark
import com.xbot.kotogram.core.designsystem.extensions.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIAction
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UISwitch

internal object IosSwitchOverride : SwitchOverride {
    @OptIn(ExperimentalForeignApi::class)
    @Composable
    override fun SwitchOverrideScope.Switch() {
        val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)
        var toggles by remember { mutableIntStateOf(0) }
        val switch = remember {
            UISwitch().also { switch ->
                switch.on = checked
                switch.addAction(
                    UIAction.actionWithHandler {
                        currentOnCheckedChange?.invoke(switch.on)
                        toggles++
                    },
                    forControlEvents = UIControlEventValueChanged
                )
            }
        }
        val size = remember(switch) { switch.intrinsicContentSize.useContents { DpSize(width.dp, height.dp) } }
        val trackColor = style.checkedTrackColor.toUIColor()
        val interactive = onCheckedChange != null
        val dark = KotogramTheme.colors.background.isDark()
        val inFront = isSettledInFront()
        UIKitView(
            factory = { switch },
            modifier = modifier.size(size),
            update = {
                it.onTintColor = trackColor
                it.enabled = enabled
                it.userInteractionEnabled = interactive && inFront
                it.overrideUserInterfaceStyle = interfaceStyle(dark)
                it.hidden = !inFront
            },
            properties = GlassProperties
        )
        // Every toggle recomposes this, so a change the caller doesn't take goes back.
        LaunchedEffect(checked, toggles) {
            if (switch.on != checked) switch.setOn(checked, animated = true)
        }
    }
}
