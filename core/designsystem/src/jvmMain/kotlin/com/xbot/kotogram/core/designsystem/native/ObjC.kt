package com.xbot.kotogram.core.designsystem.native

import com.sun.jna.Callback
import com.sun.jna.Function
import com.sun.jna.Memory
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer

internal object ObjC {
    private val runtime = NativeLibrary.getInstance("objc")
    private val msgSend: Function = runtime.getFunction("objc_msgSend")

    fun cls(name: String): Pointer? = runtime.getFunction("objc_getClass").invokePointer(arrayOf(cString(name)))

    fun sel(name: String): Pointer = runtime.getFunction("sel_registerName").invokePointer(arrayOf(cString(name)))

    fun send(receiver: Pointer, selector: String, vararg args: Any?): Pointer? =
        msgSend.invokePointer(arrayOf(receiver, sel(selector), *args))

    fun sendVoid(receiver: Pointer, selector: String, vararg args: Any?) {
        msgSend.invokeVoid(arrayOf(receiver, sel(selector), *args))
    }

    fun sendBoolean(receiver: Pointer, selector: String, vararg args: Any?): Boolean =
        msgSend.invokeInt(arrayOf(receiver, sel(selector), *args)).toByte() != 0.toByte()

    fun appearance(dark: Boolean): Pointer = requireNotNull(
        send(
            requireNotNull(cls("NSAppearance")),
            "appearanceNamed:",
            string(if (dark) "NSAppearanceNameDarkAqua" else "NSAppearanceNameAqua")
        )
    )

    fun string(text: String): Pointer =
        requireNotNull(send(requireNotNull(cls("NSString")), "stringWithUTF8String:", cString(text)))

    fun actionClass(name: String, selector: String, imp: ActionImp): Pointer {
        cls(name)?.let { return it }
        val superclass = requireNotNull(cls("NSObject"))
        val created = requireNotNull(
            runtime.getFunction("objc_allocateClassPair").invokePointer(arrayOf(superclass, cString(name), 0L))
        )
        runtime.getFunction("class_addMethod").invokeInt(arrayOf(created, sel(selector), imp, cString("v@:@")))
        runtime.getFunction("objc_registerClassPair").invokeVoid(arrayOf(created))
        return created
    }

    fun interface ActionImp : Callback {
        fun invoke(self: Pointer, cmd: Pointer, sender: Pointer)
    }

    private fun cString(text: String): Memory {
        val bytes = text.toByteArray(Charsets.UTF_8)
        return Memory(bytes.size + 1L).apply {
            write(0, bytes, 0, bytes.size)
            setByte(bytes.size.toLong(), 0)
        }
    }
}
