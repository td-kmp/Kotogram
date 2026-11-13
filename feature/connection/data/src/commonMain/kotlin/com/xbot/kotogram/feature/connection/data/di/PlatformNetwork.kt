package com.xbot.kotogram.feature.connection.data.di

import com.xbot.kotogram.feature.connection.data.platform.NetworkMonitor
import org.koin.core.scope.Scope

internal expect fun Scope.platformNetworkMonitor(): NetworkMonitor?
