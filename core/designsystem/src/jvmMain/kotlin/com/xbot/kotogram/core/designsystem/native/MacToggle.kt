package com.xbot.kotogram.core.designsystem.native

import com.sun.jna.Pointer

// AppKit's switch or checkbox: a control with an on and an off state, at its own size.
internal class MacToggle private constructor(val handle: Pointer, onToggle: (Boolean) -> Unit) {
    init {
        ObjC.sendVoid(handle, "setFocusRingType:", FOCUS_RING_TYPE_NONE)
        ControlTarget.actions[Pointer.nativeValue(handle)] = { onToggle(isOn) }
    }

    val isOn: Boolean get() = ObjC.sendLong(handle, "state") != STATE_OFF

    val fittingSize: ObjC.NSSize get() = ObjC.sendSize(handle, "fittingSize")

    // A switch set through its animator slides to the new state.
    fun setOn(on: Boolean, animated: Boolean) {
        val receiver = if (animated) ObjC.send(handle, "animator") ?: handle else handle
        ObjC.sendVoid(receiver, "setState:", if (on) STATE_ON else STATE_OFF)
    }

    fun setEnabled(enabled: Boolean) {
        ObjC.sendVoid(handle, "setEnabled:", (if (enabled) 1 else 0).toByte())
    }

    fun setDark(dark: Boolean) {
        ObjC.sendVoid(handle, "setAppearance:", ObjC.appearance(dark))
    }

    fun setVisible(visible: Boolean) {
        ObjC.sendVoid(handle, "setHidden:", (if (visible) 0 else 1).toByte())
    }

    fun release() {
        ControlTarget.actions.remove(Pointer.nativeValue(handle))
        ObjC.sendVoid(handle, "release")
    }

    companion object {
        fun switch(onToggle: (Boolean) -> Unit): MacToggle {
            val switch = requireNotNull(
                ObjC.send(requireNotNull(ObjC.send(requireNotNull(ObjC.cls("NSSwitch")), "alloc")), "init")
            )
            ObjC.sendVoid(switch, "setTarget:", ControlTarget.instance)
            ObjC.sendVoid(switch, "setAction:", ObjC.sel(ControlTarget.ACTION))
            return MacToggle(switch, onToggle)
        }

        fun checkbox(onToggle: (Boolean) -> Unit): MacToggle {
            val checkbox = requireNotNull(
                ObjC.send(
                    requireNotNull(ObjC.cls("NSButton")),
                    "checkboxWithTitle:target:action:",
                    ObjC.string(""),
                    ControlTarget.instance,
                    ObjC.sel(ControlTarget.ACTION)
                )
            )
            ObjC.sendVoid(checkbox, "retain")
            return MacToggle(checkbox, onToggle)
        }
    }
}

private const val STATE_OFF = 0L
private const val STATE_ON = 1L
private const val FOCUS_RING_TYPE_NONE = 1L
