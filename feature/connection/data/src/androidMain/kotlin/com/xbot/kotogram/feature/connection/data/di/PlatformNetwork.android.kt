package com.xbot.kotogram.feature.connection.data.di

import com.xbot.kotogram.feature.connection.data.platform.AndroidNetworkMonitor
import com.xbot.kotogram.feature.connection.data.platform.NetworkMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope

internal actual fun Scope.platformNetworkMonitor(): NetworkMonitor? = AndroidNetworkMonitor(androidContext())
