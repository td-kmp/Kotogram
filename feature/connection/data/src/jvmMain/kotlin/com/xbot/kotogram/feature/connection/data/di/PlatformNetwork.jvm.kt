package com.xbot.kotogram.feature.connection.data.di

import com.xbot.kotogram.core.platform.linux.LinuxNetworkMonitor
import com.xbot.kotogram.core.platform.macos.MacNetworkMonitor
import com.xbot.kotogram.core.platform.windows.WindowsNetworkMonitor
import com.xbot.kotogram.feature.connection.data.extensions.toNetworkType
import com.xbot.kotogram.feature.connection.data.platform.DesktopNetworkMonitor
import com.xbot.kotogram.feature.connection.data.platform.NetworkMonitor
import kotlinx.coroutines.flow.map
import org.koin.core.scope.Scope

internal actual fun Scope.platformNetworkMonitor(): NetworkMonitor? {
    val os = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    return when {
        os.startsWith("Mac") && arch == "aarch64" ->
            DesktopNetworkMonitor(MacNetworkMonitor().changes().map { it.toNetworkType() })

        os.startsWith("Windows") && arch == "amd64" ->
            DesktopNetworkMonitor(WindowsNetworkMonitor().changes().map { it.toNetworkType() })

        os.startsWith("Linux") && arch == "amd64" ->
            DesktopNetworkMonitor(LinuxNetworkMonitor().changes().map { it.toNetworkType() })

        else -> null
    }
}
