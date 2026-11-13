package com.xbot.kotogram.feature.connection.data.account

import co.touchlab.kermit.Logger
import com.xbot.kotogram.core.account.AccountUpdates
import com.xbot.kotogram.core.account.extensions.sendCatching
import com.xbot.kotogram.feature.connection.data.repository.DefaultConnectionRepository
import com.xbot.kotogram.feature.connection.data.store.ConnectionStore
import io.xbot.tdlib.api.ConnectionStateReady
import io.xbot.tdlib.api.NetworkType
import io.xbot.tdlib.api.SetNetworkType
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

internal class NetworkTypeUpdates(
    private val connection: DefaultConnectionRepository,
    private val store: ConnectionStore,
) : AccountUpdates {
    private val logger = Logger.withTag("NetworkTypeUpdates")

    override fun start(
        scope: CoroutineScope,
        client: TdClient,
    ) {
        scope.launch {
            connection.networkTypes.collect { scope.report(client, it) }
        }
        scope.launch {
            connection.inForeground.drop(1).filter { it }.collect {
                val type = connection.networkTypes.replayCache.lastOrNull()
                if (type != null && store.state.value != ConnectionStateReady) scope.report(client, type)
            }
        }
    }

    // Workaround: awaiting hangs, TDLib answers after setTdlibParameters; send keeps the order.
    private fun CoroutineScope.report(
        client: TdClient,
        type: NetworkType,
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            client.sendCatching(SetNetworkType(type)).onLeft {
                logger.e { "setNetworkType failed: ${it.message}" }
            }
        }
    }
}
