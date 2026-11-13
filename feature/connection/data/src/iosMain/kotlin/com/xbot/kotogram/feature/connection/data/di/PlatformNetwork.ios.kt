package com.xbot.kotogram.feature.connection.data.di

import com.xbot.kotogram.feature.connection.data.platform.IosNetworkMonitor
import com.xbot.kotogram.feature.connection.data.platform.NetworkMonitor
import org.koin.core.scope.Scope

internal actual fun Scope.platformNetworkMonitor(): NetworkMonitor? = IosNetworkMonitor()
