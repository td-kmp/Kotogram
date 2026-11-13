package com.xbot.kotogram.core.platform.windows

import com.xbot.kotogram.core.platform.windows.netlistmgr.CLSID_NetworkListManager
import com.xbot.kotogram.core.platform.windows.netlistmgr.IID_INetworkListManager
import com.xbot.kotogram.core.platform.windows.netlistmgr.IID_INetworkListManagerEvents
import com.xbot.kotogram.core.platform.windows.netlistmgr.INetworkListManager
import com.xbot.kotogram.core.platform.windows.netlistmgr.NLM_CONNECTIVITY
import com.xbot.kotogram.core.platform.windows.netlistmgr.NLM_CONNECTIVITYVar
import com.xbot.kotogram.core.platform.windows.netlistmgr.NLM_CONNECTIVITY_DISCONNECTED
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.free
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.newSingleThreadContext
import platform.windows.CLSCTX_ALL
import platform.windows.COINIT_MULTITHREADED
import platform.windows.CoCreateInstance
import platform.windows.CoInitializeEx
import platform.windows.CoUninitialize
import platform.windows.IConnectionPoint
import platform.windows.IConnectionPointContainer
import platform.windows.IID_IConnectionPointContainer
import platform.windows.IUnknown

class WindowsNetworkMonitor {
    fun changes(): Flow<WindowsNetworkKind> = callbackFlow {
        val initialized = CoInitializeEx(null, COINIT_MULTITHREADED) >= 0
        val manager = createManager()
        val point = manager?.let(::eventsConnectionPoint)
        val cookie = nativeHeap.alloc<UIntVar>()
        var last: NLM_CONNECTIVITY? = null
        val report = { connectivity: NLM_CONNECTIVITY ->
            if (connectivity != last) {
                last = connectivity
                trySend(kindOf(connectivity))
            }
        }
        if (manager == null || point == null) {
            close(IllegalStateException("NetworkListManager is unavailable"))
        } else {
            connectivityListener.store(report)
            val advised =
                point.pointed.lpVtbl!!.pointed.Advise!!(
                    point,
                    NetworkEventsSink.ptr.reinterpret<IUnknown>(),
                    cookie.ptr,
                )
            if (advised < 0) close(IllegalStateException("NetworkListManager's events are unavailable"))
            connectivityOf(manager)?.let(report)
        }
        awaitClose {
            connectivityListener.store(null)
            point?.let { it.pointed.lpVtbl!!.pointed.Unadvise!!(it, cookie.value) }
            point?.let { it.pointed.lpVtbl!!.pointed.Release!!(it) }
            manager?.let { it.pointed.lpVtbl!!.pointed.Release!!(it) }
            nativeHeap.free(cookie)
            if (initialized) CoUninitialize()
        }
    }.flowOn(comThread)
}

private fun kindOf(connectivity: NLM_CONNECTIVITY): WindowsNetworkKind =
    if (connectivity == NLM_CONNECTIVITY_DISCONNECTED) WindowsNetworkKind.None else WindowsNetworkKind.Other

private fun createManager(): CPointer<INetworkListManager>? = memScoped {
    val out = alloc<COpaquePointerVar>()
    val result = CoCreateInstance(
        CLSID_NetworkListManager.ptr,
        null,
        CLSCTX_ALL.toUInt(),
        IID_INetworkListManager.ptr,
        out.ptr,
    )
    if (result < 0) null else out.value?.reinterpret()
}

private fun eventsConnectionPoint(manager: CPointer<INetworkListManager>): CPointer<IConnectionPoint>? = memScoped {
    val containerOut = alloc<COpaquePointerVar>()
    val queried = manager.pointed.lpVtbl!!.pointed.QueryInterface!!(
        manager,
        IID_IConnectionPointContainer.ptr,
        containerOut.ptr,
    )
    val container = containerOut.value?.reinterpret<IConnectionPointContainer>()
    if (queried < 0 || container == null) return@memScoped null
    val pointOut = alloc<CPointerVar<IConnectionPoint>>()
    val found = container.pointed.lpVtbl!!.pointed.FindConnectionPoint!!(
        container,
        IID_INetworkListManagerEvents.ptr,
        pointOut.ptr,
    )
    container.pointed.lpVtbl!!.pointed.Release!!(container)
    if (found < 0) null else pointOut.value
}

private fun connectivityOf(manager: CPointer<INetworkListManager>): NLM_CONNECTIVITY? = memScoped {
    val connectivity = alloc<NLM_CONNECTIVITYVar>()
    val result = manager.pointed.lpVtbl!!.pointed.GetConnectivity!!(manager, connectivity.ptr)
    if (result < 0) null else connectivity.value
}

// Workaround: COM wants CoInitializeEx and CoUninitialize on one thread, and a flow's close may run elsewhere.
private val comThread = newSingleThreadContext("network-list-manager")
