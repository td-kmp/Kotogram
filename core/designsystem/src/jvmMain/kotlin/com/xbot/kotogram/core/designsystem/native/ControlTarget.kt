package com.xbot.kotogram.core.designsystem.native

import com.sun.jna.Pointer

internal object ControlTarget {
    const val ACTION = "click:"

    val actions = HashMap<Long, () -> Unit>()

    private val imp = ObjC.ActionImp { _, _, sender -> actions[Pointer.nativeValue(sender)]?.invoke() }

    val instance: Pointer by lazy {
        val cls = ObjC.actionClass("KotogramControlTarget", ACTION, imp)
        requireNotNull(ObjC.send(requireNotNull(ObjC.send(cls, "alloc")), "init"))
    }
}
