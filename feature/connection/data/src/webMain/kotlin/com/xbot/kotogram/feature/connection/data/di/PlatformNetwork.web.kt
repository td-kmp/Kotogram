package com.xbot.kotogram.feature.connection.data.di

import com.xbot.kotogram.feature.connection.data.platform.BrowserNetworkMonitor
import com.xbot.kotogram.feature.connection.data.platform.NetworkMonitor
import org.koin.core.scope.Scope

internal actual fun Scope.platformNetworkMonitor(): NetworkMonitor? = BrowserNetworkMonitor()
