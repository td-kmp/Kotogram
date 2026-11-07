package com.xbot.kotogram.core.designsystem.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer

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

    private val spinner: Pointer = requireNotNull(
        ObjC.send(requireNotNull(ObjC.send(requireNotNull(ObjC.cls("NSProgressIndicator")), "alloc")), "init")
    )

    init {
        ObjC.sendVoid(handle, "retain")
        ObjC.sendVoid(handle, "setRefusesFirstResponder:", 1.toByte())
        ObjC.sendVoid(handle, "setFocusRingType:", FOCUS_RING_TYPE_NONE)
        ObjC.sendVoid(handle, "setBezelStyle:", BEZEL_STYLE_GLASS)
        ObjC.sendVoid(handle, "setControlSize:", CONTROL_SIZE_EXTRA_LARGE)
        ObjC.sendVoid(handle, "setTintProminence:", TINT_PROMINENCE_PRIMARY)
        GlassButtonTarget.actions[Pointer.nativeValue(handle)] = onClick
        ObjC.sendVoid(spinner, "setStyle:", PROGRESS_STYLE_SPINNING)
        ObjC.sendVoid(spinner, "setControlSize:", CONTROL_SIZE_SMALL)
        ObjC.sendVoid(spinner, "setDisplayedWhenStopped:", 0.toByte())
        ObjC.sendVoid(spinner, "setTranslatesAutoresizingMaskIntoConstraints:", 0.toByte())
        ObjC.sendVoid(handle, "addSubview:", spinner)
        for (anchor in listOf("centerXAnchor", "centerYAnchor")) {
            val constraint = ObjC.send(
                requireNotNull(ObjC.send(spinner, anchor)),
                "constraintEqualToAnchor:",
                ObjC.send(handle, anchor)
            )
            constraint?.let { ObjC.sendVoid(it, "setActive:", 1.toByte()) }
        }
    }

    fun setTitle(text: String, fontSize: Double, fontWeight: Double, color: Color) {
        ObjC.sendVoid(handle, "setAttributedTitle:", attributedTitle(text, fontSize, fontWeight, color))
    }

    fun setBezelColor(color: Color) {
        ObjC.sendVoid(handle, "setBezelColor:", nsColor(color))
    }

    fun setDark(dark: Boolean) {
        appearance(if (dark) "NSAppearanceNameDarkAqua" else "NSAppearanceNameAqua")
            ?.let { ObjC.sendVoid(handle, "setAppearance:", it) }
        appearance("NSAppearanceNameDarkAqua")?.let { ObjC.sendVoid(spinner, "setAppearance:", it) }
    }

    fun setLoading(loading: Boolean) {
        ObjC.sendVoid(spinner, if (loading) "startAnimation:" else "stopAnimation:", null)
    }

    fun setEnabled(enabled: Boolean) {
        ObjC.sendVoid(handle, "setEnabled:", (if (enabled) 1 else 0).toByte())
    }

    fun setVisible(visible: Boolean) {
        ObjC.sendVoid(handle, "setHidden:", (if (visible) 0 else 1).toByte())
    }

    private fun appearance(name: String): Pointer? =
        ObjC.send(requireNotNull(ObjC.cls("NSAppearance")), "appearanceNamed:", ObjC.string(name))

    fun release() {
        GlassButtonTarget.actions.remove(Pointer.nativeValue(handle))
        ObjC.sendVoid(spinner, "release")
        ObjC.sendVoid(handle, "release")
    }

    private fun attributedTitle(text: String, fontSize: Double, fontWeight: Double, color: Color): Pointer {
        val nsFont = requireNotNull(
            ObjC.send(requireNotNull(ObjC.cls("NSFont")), "systemFontOfSize:weight:", fontSize, fontWeight)
        )
        val keys = pointers(appKitString("NSFontAttributeName"), appKitString("NSForegroundColorAttributeName"))
        val values = pointers(nsFont, nsColor(color))
        val dictionary = requireNotNull(ObjC.cls("NSDictionary"))
        val attributes = requireNotNull(ObjC.send(dictionary, "dictionaryWithObjects:forKeys:count:", values, keys, 2L))
        val title = requireNotNull(ObjC.send(requireNotNull(ObjC.cls("NSAttributedString")), "alloc"))
        return requireNotNull(ObjC.send(title, "initWithString:attributes:", ObjC.string(text), attributes))
            .also { ObjC.send(it, "autorelease") }
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

    private val imp = ObjC.ActionImp { _, _, sender -> actions[Pointer.nativeValue(sender)]?.invoke() }

    val instance: Pointer by lazy {
        val cls = ObjC.actionClass("KotogramGlassButtonTarget", ACTION, imp)
        requireNotNull(ObjC.send(requireNotNull(ObjC.send(cls, "alloc")), "init"))
    }
}

private val appKit by lazy { NativeLibrary.getInstance("AppKit") }

private fun appKitString(symbol: String): Pointer = appKit.getGlobalVariableAddress(symbol).getPointer(0)

private fun pointers(vararg values: Pointer): Memory = Memory(Native.POINTER_SIZE.toLong() * values.size).apply {
    values.forEachIndexed { index, value -> setPointer(Native.POINTER_SIZE.toLong() * index, value) }
}

private const val BEZEL_STYLE_GLASS = 16L
private const val CONTROL_SIZE_EXTRA_LARGE = 4L
private const val TINT_PROMINENCE_PRIMARY = 2L
private const val FOCUS_RING_TYPE_NONE = 1L
private const val PROGRESS_STYLE_SPINNING = 1L
private const val CONTROL_SIZE_SMALL = 1L
