package com.xbot.kotogram.core.designsystem.appkit

import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.AppKit.NSControlStateValueOn
import platform.AppKit.NSMenu
import platform.AppKit.NSMenuItem
import platform.AppKit.NSModalResponseOK
import platform.AppKit.NSOpenPanel
import platform.Foundation.NSMakePoint
import platform.UniformTypeIdentifiers.UTTypeImage
import kotlin.coroutines.resume

class MacWindow(
    windowHandle: Long,
) {
    private val window = windowFrom(windowHandle)

    @Suppress("LongParameterList")
    fun popUpMenu(
        titles: List<String>,
        selectedIndex: Int,
        anchorLeft: Double,
        anchorTop: Double,
        anchorRight: Double,
        anchorBottom: Double,
        height: Double,
    ): Int? {
        val view = window.contentView ?: return null
        var chosen: Int? = null
        val target = ActionTarget { sender -> chosen = (sender as? NSMenuItem)?.tag?.toInt() }
        val menu = NSMenu(title = "")
        titles.forEachIndexed { index, title ->
            val item = menu.addItemWithTitle(title, action = ActionSelector, keyEquivalent = "")
            item.target = target
            item.setTag(index.toLong())
            if (index == selectedIndex) item.state = NSControlStateValueOn
        }
        val flipped = view.flipped
        val (mouseX, mouseY) = view.convertPoint(window.mouseLocationOutsideOfEventStream, fromView = null)
            .useContents { x to y }
        val pointerY = if (flipped) mouseY else height - mouseY
        val inAnchor = mouseX >= anchorLeft && mouseX < anchorRight && pointerY >= anchorTop && pointerY < anchorBottom
        val cornerX = if (inAnchor) mouseX else anchorLeft
        val cornerY = if (inAnchor) pointerY else anchorBottom
        menu.popUpMenuPositioningItem(null, NSMakePoint(cornerX, if (flipped) cornerY else height - cornerY), view)
        return chosen
    }

    suspend fun chooseImage(): String? = withContext(Dispatchers.Main) {
        val panel = NSOpenPanel.openPanel().apply {
            canChooseFiles = true
            canChooseDirectories = false
            allowsMultipleSelection = false
            allowedContentTypes = listOf(UTTypeImage)
        }
        suspendCancellableCoroutine { continuation ->
            panel.beginSheetModalForWindow(window) { response ->
                continuation.resume(if (response == NSModalResponseOK) panel.URL?.path else null)
            }
        }
    }
}
