package com.xbot.kotogram.feature.chats.impl.screen.list

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.chats.domain.repository.ChatRepository
import com.xbot.kotogram.feature.media.domain.repository.CustomEmojiRepository
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.ChatListMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class ChatListViewModel(
    private val repository: ChatRepository,
    private val customEmojiRepository: CustomEmojiRepository
) : ViewModel(),
    OrbitContainerHost<ChatListScreenState, ChatListScreenState, Nothing> {

    private val selected = MutableStateFlow<ChatList>(ChatListMain)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val container = orbitContainer<ChatListScreenState, Nothing>(ChatListScreenState()) {
        repeatOnSubscription {
            coroutineScope {
                launch {
                    repository.folderTabs().collect { tabs -> reduce { state.copy(tabs = tabs) } }
                }
                selected
                    .flatMapLatest { list ->
                        val mapper = ChatListRowMapper()
                        repository.chatList(list).map(mapper::map)
                    }
                    .flowOn(Dispatchers.Default)
                    .collect { rows ->
                        reduce { state.copy(rows = rows) }
                        resolveCustomEmoji(rows)
                    }
            }
        }
    }

    fun onAction(action: ChatListScreenAction) {
        when (action) {
            ChatListScreenAction.OnEndReached -> onEndReached()
            is ChatListScreenAction.OnFolderSelect -> onFolderSelect(action.chatList)
        }
    }

    private suspend fun Syntax<ChatListScreenState, Nothing>.resolveCustomEmoji(
        rows: List<ChatListRow>
    ) {
        val missing = rows.flatMapTo(HashSet()) { it.customEmojiIds } - state.customEmoji.keys
        if (missing.isEmpty()) return
        customEmojiRepository.stickers(missing).onRight { found ->
            reduce { state.copy(customEmoji = state.customEmoji + found) }
        }
    }

    private fun onFolderSelect(chatList: ChatList) = intent {
        if (chatList == state.selected) return@intent
        reduce {
            state.copy(
                selected = chatList,
                rows = emptyList(),
                isComplete = false,
                loadFailed = false
            )
        }
        selected.value = chatList
    }

    private fun onEndReached() = intent {
        if (state.isLoading || state.isComplete) return@intent
        val list = state.selected
        reduce { state.copy(isLoading = true, loadFailed = false) }
        repository.loadMore(list).fold(
            ifLeft = { reduce { state.copy(isLoading = false, loadFailed = true) } },
            ifRight = { hasMore -> reduce { state.copy(isLoading = false, isComplete = !hasMore) } }
        )
    }
}
