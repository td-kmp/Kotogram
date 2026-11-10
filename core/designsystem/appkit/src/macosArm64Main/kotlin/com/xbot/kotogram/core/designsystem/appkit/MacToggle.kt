package com.xbot.kotogram.core.designsystem.appkit

import kotlinx.cinterop.useContents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import platform.AppKit.NSButton
import platform.AppKit.NSControl
import platform.AppKit.NSControlStateValueOff
import platform.AppKit.NSControlStateValueOn
import platform.AppKit.NSFocusRingTypeNone
import platform.AppKit.NSSwitch
import platform.AppKit.fittingSize

class MacToggle(
    kind: MacToggleKind,
) {
    private val stateChanges = MutableSharedFlow<Boolean>(extraBufferCapacity = EVENT_BUFFER)
    private val target = ActionTarget { stateChanges.tryEmit(isOn) }
    private val control: NSControl = when (kind) {
        MacToggleKind.Switch -> NSSwitch().also {
            it.target = target
            it.action = ActionSelector
        }

        MacToggleKind.Checkbox -> NSButton.checkboxWithTitle("", target, ActionSelector)
    }.apply { focusRingType = NSFocusRingTypeNone }

    val viewHandle: Long get() = control.handle()

    val isOn: Boolean
        get() = when (val control = control) {
            is NSSwitch -> control.state != NSControlStateValueOff
            is NSButton -> control.state != NSControlStateValueOff
            else -> false
        }

    val fittingWidth: Double get() = control.fittingSize.useContents { width }

    val fittingHeight: Double get() = control.fittingSize.useContents { height }

    fun changes(): Flow<Boolean> = stateChanges

    // Workaround: only a switch set through its animator slides to the new state.
    fun setOn(
        on: Boolean,
        animated: Boolean,
    ) {
        val state = if (on) NSControlStateValueOn else NSControlStateValueOff
        when (val control = control) {
            is NSSwitch -> {
                val receiver = if (animated) control.animator() else control
                receiver.state = state
            }

            is NSButton -> control.state = state
        }
    }

    fun setEnabled(enabled: Boolean) {
        control.enabled = enabled
    }

    fun setDark(dark: Boolean) {
        control.appearance = appearance(dark)
    }
}
