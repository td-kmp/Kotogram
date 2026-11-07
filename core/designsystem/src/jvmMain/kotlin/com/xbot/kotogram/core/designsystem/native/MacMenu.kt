package com.xbot.kotogram.core.designsystem.native

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.sun.jna.Pointer

// Opens as Telegram for macOS's menus do: top left corner at the click, or under the anchor when opened by keyboard.
internal object MacMenu {
    private const val ACTION = "select:"
    private var chosen: Int? = null

    private val imp = ObjC.ActionImp { _, _, sender -> chosen = ObjC.sendLong(sender, "tag").toInt() }

    private val target: Pointer by lazy {
        val cls = ObjC.actionClass("KotogramMenuTarget", ACTION, imp)
        requireNotNull(ObjC.send(requireNotNull(ObjC.send(cls, "alloc")), "init"))
    }

    fun show(window: Pointer, titles: List<String>, selectedIndex: Int, anchor: Rect, height: Float): Int? {
        chosen = null
        val allocated = requireNotNull(ObjC.send(requireNotNull(ObjC.cls("NSMenu")), "alloc"))
        val menu = requireNotNull(ObjC.send(allocated, "initWithTitle:", ObjC.string("")))
        titles.forEachIndexed { index, title ->
            val item = requireNotNull(
                ObjC.send(
                    menu,
                    "addItemWithTitle:action:keyEquivalent:",
                    ObjC.string(title),
                    ObjC.sel(ACTION),
                    ObjC.string("")
                )
            )
            ObjC.sendVoid(item, "setTarget:", target)
            ObjC.sendVoid(item, "setTag:", index.toLong())
            if (index == selectedIndex) ObjC.sendVoid(item, "setState:", STATE_ON)
        }
        val view = requireNotNull(ObjC.send(window, "contentView"))
        val flipped = ObjC.sendBoolean(view, "isFlipped")
        val inWindow = ObjC.sendPoint(window, "mouseLocationOutsideOfEventStream")
        val mouse = ObjC.sendPoint(view, "convertPoint:fromView:", inWindow, null)
        val pointer = Offset(mouse.x.toFloat(), if (flipped) mouse.y.toFloat() else height - mouse.y.toFloat())
        val corner = if (pointer in anchor) pointer else anchor.bottomLeft
        val location = ObjC.NSPoint(corner.x.toDouble(), (if (flipped) corner.y else height - corner.y).toDouble())
        ObjC.sendBoolean(menu, "popUpMenuPositioningItem:atLocation:inView:", null, location, view)
        ObjC.sendVoid(menu, "release")
        return chosen
    }
}

private const val STATE_ON = 1L
