package com.xbot.kotogram.core.designsystem.appkit

import com.xbot.kotogram.core.designsystem.appkit.extensions.handle
import com.xbot.kotogram.core.designsystem.appkit.extensions.isDark
import kotlinx.cinterop.useContents
import kotlinx.coroutines.flow.Flow
import platform.AppKit.NSButton
import platform.AppKit.NSControl
import platform.AppKit.NSControlStateValue
import platform.AppKit.NSControlStateValueOff
import platform.AppKit.NSControlStateValueOn
import platform.AppKit.NSFocusRingTypeNone
import platform.AppKit.NSSwitch
import platform.AppKit.fittingSize

class MacToggle(
    kind: MacToggleKind,
) {
    private val stateChanges = ActionFlow { isOn }
    private val toggle: ToggleControl = when (kind) {
        MacToggleKind.Switch -> SwitchControl(stateChanges.target)
        MacToggleKind.Checkbox -> CheckboxControl(stateChanges.target)
    }

    val viewHandle: Long get() = toggle.control.handle()

    val isOn: Boolean get() = toggle.state != NSControlStateValueOff

    val fittingWidth: Double get() = toggle.control.fittingSize.useContents { width }

    val fittingHeight: Double get() = toggle.control.fittingSize.useContents { height }

    var isEnabled: Boolean
        get() = toggle.control.enabled
        set(value) {
            toggle.control.enabled = value
        }

    var isDark: Boolean
        get() = toggle.control.isDark
        set(value) {
            toggle.control.isDark = value
        }

    fun changes(): Flow<Boolean> = stateChanges.flow

    fun setOn(
        on: Boolean,
        animated: Boolean,
    ) {
        toggle.setState(if (on) NSControlStateValueOn else NSControlStateValueOff, animated)
    }
}

private interface ToggleControl {
    val control: NSControl
    val state: NSControlStateValue

    fun setState(
        state: NSControlStateValue,
        animated: Boolean,
    )
}

private class SwitchControl(
    target: ActionTarget,
) : ToggleControl {
    override val control = NSSwitch().apply {
        this.target = target
        action = ActionSelector
        focusRingType = NSFocusRingTypeNone
    }

    override val state: NSControlStateValue get() = control.state

    // Workaround: only a switch set through its animator slides to the new state.
    override fun setState(
        state: NSControlStateValue,
        animated: Boolean,
    ) {
        val receiver = if (animated) control.animator() else control
        receiver.state = state
    }
}

private class CheckboxControl(
    target: ActionTarget,
) : ToggleControl {
    override val control = NSButton.checkboxWithTitle("", target, ActionSelector).apply {
        focusRingType = NSFocusRingTypeNone
    }

    override val state: NSControlStateValue get() = control.state

    override fun setState(
        state: NSControlStateValue,
        animated: Boolean,
    ) {
        control.state = state
    }
}
