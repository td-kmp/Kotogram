package com.xbot.kotogram.core.designsystem.appkit

import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSSelectorFromString
import platform.darwin.NSObject

// Workaround: AppKit holds its targets weakly, so the control's owner keeps this alive.
internal class ActionTarget(
    private val onAction: (sender: NSObject?) -> Unit,
) : NSObject() {
    @ObjCAction
    fun act(sender: NSObject?) {
        onAction(sender)
    }
}

internal val ActionSelector = NSSelectorFromString("act:")

internal const val EVENT_BUFFER = 16
