package com.xbot.kotogram.feature.chats.data.store

import co.touchlab.kermit.Logger
import com.xbot.kotogram.core.account.AccountUpdates
import com.xbot.kotogram.feature.chats.data.extensions.reduce
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// One collector, because TDLib requires in-order processing.
internal class ChatStore : AccountUpdates {
    private val mutableState = MutableStateFlow(ChatsState.Empty)
    val state: StateFlow<ChatsState> = mutableState.asStateFlow()

    override fun start(scope: CoroutineScope, client: TdClient) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            client.updates.collect { onUpdate(it) }
        }
    }

    private fun onUpdate(update: Update) {
        // A reducer bug must not take the account down.
        runCatching { mutableState.value.reduce(update) }
            .onSuccess { mutableState.value = it }
            .onFailure { Logger.e(it) { "Can't apply ${update::class.simpleName}" } }
    }
}
