package com.xbot.kotogram.core.designsystem.component

import com.sun.jna.Callback
import com.sun.jna.Function
import com.sun.jna.Memory
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import com.sun.jna.Structure

// The Objective-C runtime through JNA: AppKit has no Java binding, and the JVM app links no native code of ours.
internal object ObjC {
    private val runtime = NativeLibrary.getInstance("objc")
    private val msgSend: Function = runtime.getFunction("objc_msgSend")

    fun cls(name: String): Pointer? = runtime.getFunction("objc_getClass").invokePointer(arrayOf(cString(name)))

    fun sel(name: String): Pointer = runtime.getFunction("sel_registerName").invokePointer(arrayOf(cString(name)))

    fun send(receiver: Pointer, selector: String, vararg args: Any): Pointer? =
        msgSend.invokePointer(arrayOf(receiver, sel(selector), *args))

    fun sendVoid(receiver: Pointer, selector: String, vararg args: Any) {
        msgSend.invokeVoid(arrayOf(receiver, sel(selector), *args))
    }

    // BOOL is one byte: only the low byte of the register is the answer.
    fun sendBoolean(receiver: Pointer, selector: String, vararg args: Any): Boolean =
        msgSend.invokeInt(arrayOf(receiver, sel(selector), *args)).toByte() != 0.toByte()

    fun sendSize(receiver: Pointer, selector: String): NSSize =
        msgSend.invoke(NSSize::class.java, arrayOf(receiver, sel(selector))) as NSSize

    fun string(text: String): Pointer =
        requireNotNull(send(requireNotNull(cls("NSString")), "stringWithUTF8String:", cString(text)))

    // A class whose one method calls back into Kotlin, for AppKit's target-action.
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

    private fun cString(text: String): Memory {
        val bytes = text.toByteArray(Charsets.UTF_8)
        return Memory(bytes.size + 1L).apply {
            write(0, bytes, 0, bytes.size)
            setByte(bytes.size.toLong(), 0)
        }
    }

    fun interface ActionImp : Callback {
        fun invoke(self: Pointer, cmd: Pointer, sender: Pointer)
    }

    @Structure.FieldOrder("width", "height")
    class NSSize : Structure(), Structure.ByValue {
        @JvmField var width: Double = 0.0

        @JvmField var height: Double = 0.0
    }
}
