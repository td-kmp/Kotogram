package com.xbot.kotogram.feature.connection.data.extensions

import com.xbot.kotogram.core.platform.macos.MacNetworkKind
import io.xbot.tdlib.api.NetworkType
import io.xbot.tdlib.api.NetworkTypeMobile
import io.xbot.tdlib.api.NetworkTypeNone
import io.xbot.tdlib.api.NetworkTypeWiFi

internal fun MacNetworkKind.toNetworkType(): NetworkType = when (this) {
    None -> NetworkTypeNone
    WiFi -> NetworkTypeWiFi
    Cellular -> NetworkTypeMobile
}
