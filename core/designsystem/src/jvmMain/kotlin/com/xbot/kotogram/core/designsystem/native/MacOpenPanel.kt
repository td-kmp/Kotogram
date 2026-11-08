package com.xbot.kotogram.core.designsystem.native

// NSOpenPanel for one image. Run modal: a sheet takes a completion block, which JNA can't make.
internal object MacOpenPanel {
    fun chooseImage(): String? {
        val panel = requireNotNull(ObjC.send(requireNotNull(ObjC.cls("NSOpenPanel")), "openPanel"))
        ObjC.sendVoid(panel, "setCanChooseFiles:", true)
        ObjC.sendVoid(panel, "setCanChooseDirectories:", false)
        ObjC.sendVoid(panel, "setAllowsMultipleSelection:", false)
        val image = ObjC.cls("UTType")?.let { ObjC.send(it, "typeWithIdentifier:", ObjC.string("public.image")) }
        if (image != null) {
            val types = ObjC.send(requireNotNull(ObjC.cls("NSArray")), "arrayWithObject:", image)
            ObjC.sendVoid(panel, "setAllowedContentTypes:", types)
        }
        if (ObjC.sendLong(panel, "runModal") != MODAL_RESPONSE_OK) return null
        return ObjC.send(panel, "URL")
            ?.let { ObjC.send(it, "path") }
            ?.let { ObjC.send(it, "UTF8String") }
            ?.getString(0, "UTF-8")
    }
}

private const val MODAL_RESPONSE_OK = 1L
