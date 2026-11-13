package com.xbot.kotogram.feature.connection.data.platform

import io.xbot.tdlib.api.NetworkType
import io.xbot.tdlib.api.NetworkTypeNone
import io.xbot.tdlib.api.NetworkTypeOther
import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.w3c.dom.events.Event

internal class BrowserNetworkMonitor : NetworkMonitor {
    override val changes: Flow<NetworkType> = callbackFlow {
        val listener: (Event) -> Unit = { trySend(currentType()) }
        window.addEventListener(ONLINE, listener)
        window.addEventListener(OFFLINE, listener)
        trySend(currentType())
        awaitClose {
            window.removeEventListener(ONLINE, listener)
            window.removeEventListener(OFFLINE, listener)
        }
    }
}

private fun currentType(): NetworkType = if (window.navigator.onLine) NetworkTypeOther else NetworkTypeNone

private const val ONLINE = "online"
private const val OFFLINE = "offline"
