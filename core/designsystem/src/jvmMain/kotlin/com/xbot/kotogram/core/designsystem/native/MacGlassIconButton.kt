package com.xbot.kotogram.core.designsystem.native

import com.sun.jna.Memory
import com.sun.jna.Pointer

// AppKit's glass button in a circle, with the icon as an image AppKit draws in the button.
internal class MacGlassIconButton(onClick: () -> Unit) {
    val handle: Pointer = requireNotNull(
        ObjC.send(
            requireNotNull(ObjC.cls("NSButton")),
            "buttonWithTitle:target:action:",
            ObjC.string(""),
            ControlTarget.instance,
            ObjC.sel(ControlTarget.ACTION)
        )
    )

    private var png: ByteArray? = null

    init {
        ObjC.sendVoid(handle, "retain")
        ObjC.sendVoid(handle, "setRefusesFirstResponder:", 1.toByte())
        ObjC.sendVoid(handle, "setFocusRingType:", FOCUS_RING_TYPE_NONE)
        ObjC.sendVoid(handle, "setBezelStyle:", BEZEL_STYLE_GLASS)
        ObjC.sendVoid(handle, "setBorderShape:", BORDER_SHAPE_CIRCLE)
        ObjC.sendVoid(handle, "setImagePosition:", IMAGE_ONLY)
        ObjC.sendVoid(handle, "setControlSize:", CONTROL_SIZE_LARGE)
        ControlTarget.actions[Pointer.nativeValue(handle)] = onClick
    }

    // The PNG is drawn at the screen's scale; the image's size in points makes AppKit draw it sharp.
    fun setImage(png: ByteArray, points: Double) {
        if (png === this.png) return
        this.png = png
        val bytes = Memory(png.size.toLong()).apply { write(0, png, 0, png.size) }
        val data = ObjC.send(requireNotNull(ObjC.cls("NSData")), "dataWithBytes:length:", bytes, png.size.toLong())
        val allocated = requireNotNull(ObjC.send(requireNotNull(ObjC.cls("NSImage")), "alloc"))
        val image = ObjC.send(allocated, "initWithData:", data) ?: return
        ObjC.sendVoid(image, "setSize:", ObjC.NSSize(points, points))
        ObjC.sendVoid(handle, "setImage:", image)
        ObjC.sendVoid(image, "release")
    }

    fun setAccessibilityLabel(label: String?) {
        ObjC.sendVoid(handle, "setAccessibilityLabel:", label?.let(ObjC::string))
    }

    fun setDark(dark: Boolean) {
        ObjC.sendVoid(handle, "setAppearance:", ObjC.appearance(dark))
    }

    fun setEnabled(enabled: Boolean) {
        ObjC.sendVoid(handle, "setEnabled:", (if (enabled) 1 else 0).toByte())
    }

    fun setVisible(visible: Boolean) {
        ObjC.sendVoid(handle, "setHidden:", (if (visible) 0 else 1).toByte())
    }

    fun release() {
        ControlTarget.actions.remove(Pointer.nativeValue(handle))
        ObjC.sendVoid(handle, "release")
    }
}

private const val BEZEL_STYLE_GLASS = 16L
private const val BORDER_SHAPE_CIRCLE = 3L
private const val IMAGE_ONLY = 1L
private const val CONTROL_SIZE_LARGE = 3L
private const val FOCUS_RING_TYPE_NONE = 1L
