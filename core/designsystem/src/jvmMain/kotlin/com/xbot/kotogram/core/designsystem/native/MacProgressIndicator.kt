package com.xbot.kotogram.core.designsystem.native

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.sun.jna.Pointer

internal class MacProgressIndicator {
    val handle: Pointer = requireNotNull(
        ObjC.send(requireNotNull(ObjC.send(requireNotNull(ObjC.cls("NSProgressIndicator")), "alloc")), "init")
    )

    init {
        ObjC.sendVoid(handle, "setStyle:", PROGRESS_STYLE_SPINNING)
        ObjC.sendVoid(handle, "setIndeterminate:", 1.toByte())
        ObjC.sendVoid(handle, "setDisplayedWhenStopped:", 0.toByte())
        ObjC.sendVoid(handle, "setControlSize:", CONTROL_SIZE_SMALL)
    }

    fun setColor(color: Color, background: Color) {
        val light = color.luminance() > minOf(HALF_LUMINANCE, background.luminance())
        ObjC.sendVoid(handle, "setAppearance:", ObjC.appearance(dark = light))
    }

    fun setLarge(large: Boolean) {
        ObjC.sendVoid(handle, "setControlSize:", if (large) CONTROL_SIZE_REGULAR else CONTROL_SIZE_SMALL)
    }

    fun setAnimating(animating: Boolean) {
        ObjC.sendVoid(handle, if (animating) "startAnimation:" else "stopAnimation:", null)
    }

    fun setVisible(visible: Boolean) {
        ObjC.sendVoid(handle, "setHidden:", (if (visible) 0 else 1).toByte())
    }

    fun centerIn(parent: Pointer) {
        ObjC.sendVoid(handle, "setTranslatesAutoresizingMaskIntoConstraints:", 0.toByte())
        ObjC.sendVoid(parent, "addSubview:", handle)
        for (anchor in listOf("centerXAnchor", "centerYAnchor")) {
            ObjC.send(requireNotNull(ObjC.send(handle, anchor)), "constraintEqualToAnchor:", ObjC.send(parent, anchor))
                ?.let { ObjC.sendVoid(it, "setActive:", 1.toByte()) }
        }
    }

    fun release() {
        ObjC.sendVoid(handle, "release")
    }

    companion object {
        val isSupported: Boolean = System.getProperty("os.name").orEmpty().startsWith("Mac")
    }
}

private const val PROGRESS_STYLE_SPINNING = 1L
private const val CONTROL_SIZE_SMALL = 1L
private const val CONTROL_SIZE_REGULAR = 0L
private const val HALF_LUMINANCE = 0.5f
