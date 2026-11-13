package com.xbot.kotogram.core.platform.linux

import com.xbot.kotogram.core.platform.linux.netlink.NETLINK_ROUTE
import com.xbot.kotogram.core.platform.linux.netlink.RTMGRP_IPV4_IFADDR
import com.xbot.kotogram.core.platform.linux.netlink.RTMGRP_IPV4_ROUTE
import com.xbot.kotogram.core.platform.linux.netlink.RTMGRP_IPV6_IFADDR
import com.xbot.kotogram.core.platform.linux.netlink.RTMGRP_IPV6_ROUTE
import com.xbot.kotogram.core.platform.linux.netlink.RTMGRP_LINK
import com.xbot.kotogram.core.platform.linux.netlink.sockaddr_nl
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import platform.posix.AF_NETLINK
import platform.posix.EINTR
import platform.posix.F_OK
import platform.posix.MSG_DONTWAIT
import platform.posix.POLLIN
import platform.posix.SOCK_CLOEXEC
import platform.posix.SOCK_RAW
import platform.posix.access
import platform.posix.bind
import platform.posix.close
import platform.posix.errno
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.memset
import platform.posix.poll
import platform.posix.pollfd
import platform.posix.recv
import platform.posix.socket

class LinuxNetworkMonitor {
    fun changes(): Flow<LinuxNetworkKind> = flow {
        val socket = openRouteSocket()
        check(socket >= 0) { "rtnetlink is unavailable" }
        try {
            var lastRoute: String? = null
            while (true) {
                val route = defaultRoute()
                if (route != lastRoute) {
                    lastRoute = route
                    emit(kindOf(route))
                }
                awaitRouteChange(socket)
            }
        } finally {
            close(socket)
        }
    }.flowOn(Dispatchers.IO)
}

private fun openRouteSocket(): Int {
    val socket = socket(AF_NETLINK, SOCK_RAW or SOCK_CLOEXEC, NETLINK_ROUTE)
    if (socket >= 0 && bindToRoutes(socket) < 0) {
        close(socket)
        return -1
    }
    return socket
}

private fun bindToRoutes(socket: Int): Int = memScoped {
    val address = alloc<sockaddr_nl>()
    memset(address.ptr, 0, sizeOf<sockaddr_nl>().convert())
    address.nl_family = AF_NETLINK.convert()
    address.nl_groups = ROUTE_GROUPS.convert()
    bind(socket, address.ptr.reinterpret(), sizeOf<sockaddr_nl>().convert())
}

private suspend fun awaitRouteChange(socket: Int) {
    memScoped {
        val request = alloc<pollfd>()
        request.fd = socket
        request.events = POLLIN.convert()
        while (true) {
            request.revents = 0
            val ready = poll(request.ptr, 1u, POLL_TIMEOUT_MS)
            if (ready > 0) break
            check(ready == 0 || errno == EINTR) { "rtnetlink poll failed: $errno" }
            currentCoroutineContext().ensureActive()
        }
    }
    val buffer = ByteArray(MESSAGE_BUFFER_SIZE)
    buffer.usePinned { pinned ->
        do {
            val read = recv(socket, pinned.addressOf(0), MESSAGE_BUFFER_SIZE.convert(), MSG_DONTWAIT)
        } while (read > 0)
    }
}

private fun defaultRoute(): String? = ipv4DefaultRoute() ?: ipv6DefaultRoute()

private fun ipv4DefaultRoute(): String? = readLines("/proc/net/route").drop(1)
    .map { it.trim().split(WHITESPACE) }
    .firstOrNull { fields ->
        fields.size > IPV4_MASK &&
            fields[IPV4_DESTINATION] == IPV4_ANY &&
            fields[IPV4_MASK] == IPV4_ANY &&
            isUp(fields[IPV4_FLAGS])
    }
    ?.let { "${it[IPV4_INTERFACE]} ${it[IPV4_GATEWAY]}" }

private fun ipv6DefaultRoute(): String? = readLines("/proc/net/ipv6_route")
    .map { it.trim().split(WHITESPACE) }
    .firstOrNull { fields ->
        fields.size > IPV6_INTERFACE &&
            fields[IPV6_PREFIX_LENGTH] == IPV6_ANY_PREFIX &&
            fields[IPV6_DESTINATION].all { it == '0' } &&
            fields[IPV6_INTERFACE] != LOOPBACK &&
            isUp(fields[IPV6_FLAGS])
    }
    ?.let { "${it[IPV6_INTERFACE]} ${it[IPV6_NEXT_HOP]}" }

private fun isUp(routeFlags: String): Boolean {
    val flags = routeFlags.toIntOrNull(HEX) ?: return false
    return flags and RTF_UP != 0 && flags and RTF_REJECT == 0
}

private fun kindOf(route: String?): LinuxNetworkKind {
    val name = route?.substringBefore(' ') ?: return LinuxNetworkKind.None
    val device = "/sys/class/net/$name"
    return when {
        exists("$device/wireless") || exists("$device/phy80211") -> LinuxNetworkKind.WiFi
        readLines("$device/uevent").any { it.trim() == "DEVTYPE=wwan" } -> LinuxNetworkKind.Cellular
        else -> LinuxNetworkKind.Other
    }
}

private fun exists(path: String): Boolean = access(path, F_OK) == 0

private fun readLines(path: String): List<String> {
    val file = fopen(path, "r") ?: return []
    return try {
        memScoped {
            val line = allocArray<ByteVar>(LINE_SIZE)
            buildList {
                while (fgets(line, LINE_SIZE, file) != null) add(line.toKString())
            }
        }
    } finally {
        fclose(file)
    }
}

private val WHITESPACE = Regex("\\s+")

private const val ROUTE_GROUPS =
    RTMGRP_LINK or RTMGRP_IPV4_IFADDR or RTMGRP_IPV6_IFADDR or RTMGRP_IPV4_ROUTE or RTMGRP_IPV6_ROUTE
private const val IPV4_ANY = "00000000"
private const val IPV4_INTERFACE = 0
private const val IPV4_DESTINATION = 1
private const val IPV4_GATEWAY = 2
private const val IPV4_FLAGS = 3
private const val IPV4_MASK = 7
private const val IPV6_ANY_PREFIX = "00"
private const val IPV6_DESTINATION = 0
private const val IPV6_PREFIX_LENGTH = 1
private const val IPV6_NEXT_HOP = 4
private const val IPV6_FLAGS = 8
private const val IPV6_INTERFACE = 9
private const val LOOPBACK = "lo"
private const val RTF_UP = 0x1
private const val RTF_REJECT = 0x200
private const val HEX = 16
private const val POLL_TIMEOUT_MS = 1000
private const val MESSAGE_BUFFER_SIZE = 8192
private const val LINE_SIZE = 512
