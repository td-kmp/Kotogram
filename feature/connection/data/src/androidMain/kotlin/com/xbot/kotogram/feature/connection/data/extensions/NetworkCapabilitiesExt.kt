package com.xbot.kotogram.feature.connection.data.extensions

import android.net.NetworkCapabilities
import io.xbot.tdlib.api.NetworkType
import io.xbot.tdlib.api.NetworkTypeMobile
import io.xbot.tdlib.api.NetworkTypeMobileRoaming
import io.xbot.tdlib.api.NetworkTypeWiFi

internal fun NetworkCapabilities.toNetworkType(): NetworkType = when {
    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
        NetworkTypeWiFi

    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
        !hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING) -> NetworkTypeMobileRoaming

    else -> NetworkTypeMobile
}
