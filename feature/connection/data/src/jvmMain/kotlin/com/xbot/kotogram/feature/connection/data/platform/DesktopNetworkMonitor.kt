package com.xbot.kotogram.feature.connection.data.platform

import io.xbot.tdlib.api.NetworkType
import kotlinx.coroutines.flow.Flow

internal class DesktopNetworkMonitor(
    override val changes: Flow<NetworkType>,
) : NetworkMonitor
