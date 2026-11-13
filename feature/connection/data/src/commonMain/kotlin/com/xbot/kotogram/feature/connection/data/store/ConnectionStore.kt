package com.xbot.kotogram.feature.connection.data.store

import com.xbot.kotogram.core.account.AccountUpdates
import io.xbot.tdlib.api.ConnectionState
import io.xbot.tdlib.api.UpdateConnectionState
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class ConnectionStore : AccountUpdates {
    val state: StateFlow<ConnectionState?>
        field = MutableStateFlow<ConnectionState?>(null)

    override fun start(
        scope: CoroutineScope,
        client: TdClient,
    ) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            client.updatesOf<UpdateConnectionState>().collect { state.value = it.state }
        }
    }
}
