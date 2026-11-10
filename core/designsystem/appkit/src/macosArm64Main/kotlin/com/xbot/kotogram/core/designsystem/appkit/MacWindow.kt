package com.xbot.kotogram.core.designsystem.appkit

import com.xbot.kotogram.core.designsystem.appkit.extensions.readJpeg
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.interpretObjCPointer
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.AppKit.NSControlStateValueOn
import platform.AppKit.NSMenu
import platform.AppKit.NSMenuItem
import platform.AppKit.NSModalResponseOK
import platform.AppKit.NSOpenPanel
import platform.AppKit.NSWindow
import platform.Foundation.NSMakePoint
import platform.Foundation.NSURL
import platform.UniformTypeIdentifiers.UTTypeImage
import kotlin.coroutines.resume

class MacWindow(
    windowHandle: Long,
) {
    private val window: NSWindow = interpretObjCPointer(requireNotNull(windowHandle.toCPointer<CPointed>()).rawValue)

    fun popUpMenu(
        titles: List<String>,
        selectedIndex: Int,
        anchor: MacRect,
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
        val height = view.bounds.useContents { size.height }
        val (mouseX, mouseY) = view.convertPoint(window.mouseLocationOutsideOfEventStream, fromView = null)
            .useContents { x to y }
        val pointerY = if (flipped) mouseY else height - mouseY
        val inAnchor = mouseX >= anchor.left && mouseX < anchor.right &&
            pointerY >= anchor.top && pointerY < anchor.bottom
        val cornerX = if (inAnchor) mouseX else anchor.left
        val cornerY = if (inAnchor) pointerY else anchor.bottom
        menu.popUpMenuPositioningItem(null, NSMakePoint(cornerX, if (flipped) cornerY else height - cornerY), view)
        return chosen
    }

    suspend fun chooseImage(
        maxSide: Int,
        quality: Double,
    ): ByteArray? {
        val url = withContext(Dispatchers.Main) {
            val panel = NSOpenPanel.openPanel().apply {
                canChooseFiles = true
                canChooseDirectories = false
                allowsMultipleSelection = false
                allowedContentTypes = listOf(UTTypeImage)
            }
            suspendCancellableCoroutine<NSURL?> { continuation ->
                panel.beginSheetModalForWindow(window) { response ->
                    continuation.resume(if (response == NSModalResponseOK) panel.URL else null)
                }
            }
        } ?: return null
        return withContext(Dispatchers.Default) { url.readJpeg(maxSide, quality) }
    }
}
