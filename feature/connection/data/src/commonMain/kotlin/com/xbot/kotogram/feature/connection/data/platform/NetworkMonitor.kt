package com.xbot.kotogram.feature.connection.data.platform

import io.xbot.tdlib.api.NetworkType
import kotlinx.coroutines.flow.Flow

internal interface NetworkMonitor {
    val changes: Flow<NetworkType>
}
