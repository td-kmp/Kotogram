package com.xbot.kotogram.feature.chats.impl.screen.list

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.chats.domain.repository.ChatRepository
import io.xbot.tdlib.api.ChatListMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class ChatListViewModel(private val repository: ChatRepository) :
    ViewModel(),
    OrbitContainerHost<ChatListScreenState, ChatListScreenState, Nothing> {

    private val mapper = ChatListRowMapper()

    override val container = orbitContainer<ChatListScreenState, Nothing>(ChatListScreenState()) {
        repeatOnSubscription {
            repository.chatList(ChatListMain)
                .map(mapper::map)
                .flowOn(Dispatchers.Default)
                .collect { rows -> reduce { state.copy(rows = rows) } }
        }
    }

    fun onAction(action: ChatListScreenAction) {
        when (action) {
            ChatListScreenAction.OnEndReached -> onEndReached()
        }
    }

    private fun onEndReached() = intent {
        if (state.isLoading || state.isComplete) return@intent
        reduce { state.copy(isLoading = true, loadFailed = false) }
        repository.loadMore(ChatListMain).fold(
            ifLeft = { reduce { state.copy(isLoading = false, loadFailed = true) } },
            ifRight = { hasMore -> reduce { state.copy(isLoading = false, isComplete = !hasMore) } }
        )
    }
}
