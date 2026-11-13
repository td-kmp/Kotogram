package com.xbot.kotogram.core.platform.windows

import com.xbot.kotogram.core.platform.windows.netlistmgr.IID_INetworkListManagerEvents
import com.xbot.kotogram.core.platform.windows.netlistmgr.INetworkListManagerEvents
import com.xbot.kotogram.core.platform.windows.netlistmgr.INetworkListManagerEventsVtbl
import com.xbot.kotogram.core.platform.windows.netlistmgr.NLM_CONNECTIVITY
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.value
import platform.posix.GUID
import platform.posix.memcmp
import platform.windows.E_NOINTERFACE
import platform.windows.HRESULT
import platform.windows.IID_IUnknown
import platform.windows.S_OK
import kotlin.concurrent.atomics.AtomicReference

internal val connectivityListener = AtomicReference<((NLM_CONNECTIVITY) -> Unit)?>(null)

// Workaround: one static COM object for the process; AddRef and Release don't count, so it is never freed.
internal val NetworkEventsSink: INetworkListManagerEvents = nativeHeap.alloc<INetworkListManagerEvents>().apply {
    lpVtbl = nativeHeap.alloc<INetworkListManagerEventsVtbl>().apply {
        QueryInterface = staticCFunction(::queryInterface)
        AddRef = staticCFunction { _: CPointer<INetworkListManagerEvents>? -> 1u }
        Release = staticCFunction { _: CPointer<INetworkListManagerEvents>? -> 1u }
        ConnectivityChanged =
            staticCFunction { _: CPointer<INetworkListManagerEvents>?, connectivity: NLM_CONNECTIVITY ->
                connectivityListener.load()?.invoke(connectivity)
                S_OK
            }
    }.ptr
}

private fun queryInterface(
    self: CPointer<INetworkListManagerEvents>?,
    riid: CPointer<GUID>?,
    out: CPointer<COpaquePointerVar>?,
): HRESULT {
    val known = riid != null && (isSame(riid, IID_IUnknown) || isSame(riid, IID_INetworkListManagerEvents))
    out?.pointed?.value = if (known) self else null
    return if (known) S_OK else E_NOINTERFACE
}

private fun isSame(
    riid: CPointer<GUID>,
    guid: GUID,
): Boolean = memcmp(riid, guid.ptr, GUID_SIZE) == 0

private const val GUID_SIZE = 16uL
