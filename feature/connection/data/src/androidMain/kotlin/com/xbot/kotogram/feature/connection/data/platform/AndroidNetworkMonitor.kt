package com.xbot.kotogram.feature.connection.data.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.xbot.kotogram.feature.connection.data.extensions.toNetworkType
import io.xbot.tdlib.api.NetworkType
import io.xbot.tdlib.api.NetworkTypeNone
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal class AndroidNetworkMonitor(
    context: Context,
) : NetworkMonitor {
    private val connectivity = context.getSystemService(ConnectivityManager::class.java)

    override val changes: Flow<NetworkType> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            private var network: Network? = null
            private var type: NetworkType? = null

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                val next = networkCapabilities.toNetworkType()
                if (network != this.network || next != type) {
                    this.network = network
                    type = next
                    trySend(next)
                }
            }

            // Workaround: a switch may report the old network's loss after the new network's callbacks.
            override fun onLost(network: Network) {
                if (network == this.network) {
                    this.network = null
                    type = NetworkTypeNone
                    trySend(NetworkTypeNone)
                }
            }
        }
        if (connectivity.activeNetwork == null) trySend(NetworkTypeNone)
        connectivity.registerDefaultNetworkCallback(callback)
        awaitClose { connectivity.unregisterNetworkCallback(callback) }
    }
}
