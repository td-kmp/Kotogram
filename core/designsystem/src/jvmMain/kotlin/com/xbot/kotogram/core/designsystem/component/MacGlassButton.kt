package com.xbot.kotogram.core.designsystem.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.sun.jna.Pointer

// AppKit's NSButton with macOS 26's glass bezel, driven through the Objective-C runtime.
internal class MacGlassButton(onClick: () -> Unit) {
    val handle: Pointer = requireNotNull(
        ObjC.send(
            requireNotNull(ObjC.cls("NSButton")),
            "buttonWithTitle:target:action:",
            ObjC.string(""),
            GlassButtonTarget.instance,
            ObjC.sel(GlassButtonTarget.ACTION)
        )
    )

    init {
        ObjC.sendVoid(handle, "retain")
        // Compose owns focus; as AppKit's first responder the button would draw its own focus ring.
        ObjC.sendVoid(handle, "setRefusesFirstResponder:", 1.toByte())
        ObjC.sendVoid(handle, "setBezelStyle:", BEZEL_STYLE_GLASS)
        ObjC.sendVoid(handle, "setControlSize:", CONTROL_SIZE_EXTRA_LARGE)
        ObjC.sendVoid(handle, "setTintProminence:", TINT_PROMINENCE_PRIMARY)
        GlassButtonTarget.actions[Pointer.nativeValue(handle)] = onClick
    }

    fun sizeFor(title: String): DpSize {
        ObjC.sendVoid(handle, "setTitle:", ObjC.string(title))
        val size = ObjC.sendSize(handle, "fittingSize")
        return DpSize(size.width.dp, size.height.dp)
    }

    fun update(title: String, enabled: Boolean, tint: Color, dark: Boolean) {
        ObjC.sendVoid(handle, "setTitle:", ObjC.string(title))
        ObjC.sendVoid(handle, "setEnabled:", (if (enabled) 1 else 0).toByte())
        ObjC.sendVoid(handle, "setBezelColor:", nsColor(tint))
        val appearance = if (dark) "NSAppearanceNameDarkAqua" else "NSAppearanceNameAqua"
        ObjC.send(requireNotNull(ObjC.cls("NSAppearance")), "appearanceNamed:", ObjC.string(appearance))
            ?.let { ObjC.sendVoid(handle, "setAppearance:", it) }
    }

    fun release() {
        GlassButtonTarget.actions.remove(Pointer.nativeValue(handle))
        ObjC.sendVoid(handle, "release")
    }

    private fun nsColor(color: Color): Pointer {
        val srgb = color.convert(ColorSpaces.Srgb)
        return requireNotNull(
            ObjC.send(
                requireNotNull(ObjC.cls("NSColor")),
                "colorWithSRGBRed:green:blue:alpha:",
                srgb.red.toDouble(),
                srgb.green.toDouble(),
                srgb.blue.toDouble(),
                srgb.alpha.toDouble()
            )
        )
    }

    companion object {
        val isSupported: Boolean by lazy {
            System.getProperty("os.name").orEmpty().startsWith("Mac") && runCatching {
                val button = ObjC.cls("NSButton") ?: return@runCatching false
                ObjC.sendBoolean(button, "instancesRespondToSelector:", ObjC.sel("setTintProminence:"))
            }.getOrDefault(false)
        }
    }
}

private object GlassButtonTarget {
    const val ACTION = "click:"

    val actions = HashMap<Long, () -> Unit>()

    // Held for the process's lifetime: AppKit calls it through a pointer JNA made from it.
    private val imp = ObjC.ActionImp { _, _, sender -> actions[Pointer.nativeValue(sender)]?.invoke() }

    val instance: Pointer by lazy {
        val cls = ObjC.actionClass("KotogramGlassButtonTarget", ACTION, imp)
        requireNotNull(ObjC.send(requireNotNull(ObjC.send(cls, "alloc")), "init"))
    }
}

// AppKit's values (NSButtonCell.h, NSCell.h, NSTintProminence.h in the macOS 26 SDK).
private const val BEZEL_STYLE_GLASS = 16L
private const val CONTROL_SIZE_EXTRA_LARGE = 4L
private const val TINT_PROMINENCE_PRIMARY = 2L
