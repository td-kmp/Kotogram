package com.xbot.kotogram.feature.chats.data.store

import co.touchlab.kermit.Logger
import com.xbot.kotogram.core.account.AccountUpdates
import com.xbot.kotogram.feature.chats.data.extensions.reduce
import com.xbot.kotogram.feature.chats.data.extensions.withForumTopics
import io.xbot.tdlib.api.ForumTopics
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ChatStore : AccountUpdates {
    private val mutableState = MutableStateFlow(ChatsState.Empty)
    val state: StateFlow<ChatsState> = mutableState.asStateFlow()

    override fun start(
        scope: CoroutineScope,
        client: TdClient,
    ) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            client.updates.collect { onUpdate(it) }
        }
    }

    fun putForumTopics(
        chatId: Long,
        topics: ForumTopics,
    ) {
        mutableState.update { state -> state.withForumTopics(chatId, topics) }
    }

    private fun onUpdate(update: Update) {
        runCatching { mutableState.update { it.reduce(update) } }
            .onFailure { Logger.e(it) { "Can't apply ${update::class.simpleName}" } }
    }
}
