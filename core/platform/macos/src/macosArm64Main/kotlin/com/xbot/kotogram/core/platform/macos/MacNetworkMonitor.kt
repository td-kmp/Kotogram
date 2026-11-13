package com.xbot.kotogram.core.platform.macos

import kotlinx.cinterop.toKString
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Network.nw_interface_get_name
import platform.Network.nw_interface_type_cellular
import platform.Network.nw_path_enumerate_interfaces
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_t
import platform.Network.nw_path_uses_interface_type
import platform.darwin.dispatch_queue_create

class MacNetworkMonitor {
    fun changes(): Flow<MacNetworkKind> = callbackFlow {
        val monitor = nw_path_monitor_create()
        var lastRoute: String? = null
        nw_path_monitor_set_update_handler(monitor) { path ->
            val route = routeOf(path)
            if (route != lastRoute) {
                lastRoute = route
                trySend(kindOf(path))
            }
        }
        nw_path_monitor_set_queue(monitor, dispatch_queue_create(QUEUE_LABEL, null))
        nw_path_monitor_start(monitor)
        awaitClose { nw_path_monitor_cancel(monitor) }
    }
}

private fun kindOf(path: nw_path_t): MacNetworkKind = when {
    nw_path_get_status(path) != nw_path_status_satisfied -> MacNetworkKind.None
    nw_path_uses_interface_type(path, nw_interface_type_cellular) -> MacNetworkKind.Cellular
    else -> MacNetworkKind.WiFi
}

// Workaround: the handler fires on any change of the path, and TDLib reopens its connections on each report.
private fun routeOf(path: nw_path_t): String = buildString {
    append(nw_path_get_status(path))
    nw_path_enumerate_interfaces(path) { network ->
        append(' ').append(nw_interface_get_name(network)?.toKString())
        true
    }
}

private const val QUEUE_LABEL = "com.xbot.kotogram.network"
