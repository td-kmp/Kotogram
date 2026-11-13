package com.xbot.kotogram.feature.chats.impl.screen.list

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.core.designsystem.component.AvatarShape
import com.xbot.kotogram.feature.chats.domain.repository.ChatRepository
import com.xbot.kotogram.feature.connection.domain.repository.ConnectionStateRepository
import com.xbot.kotogram.feature.media.domain.repository.CustomEmojiRepository
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.ChatListArchive
import io.xbot.tdlib.api.ChatListMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class ChatListViewModel(
    private val repository: ChatRepository,
    private val customEmojiRepository: CustomEmojiRepository,
    private val connectionRepository: ConnectionStateRepository,
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
                launch {
                    repository.archive().collect { archive -> reduce { state.copy(archive = archive) } }
                }
                launch {
                    connectionRepository.state.collect { connection -> reduce { state.copy(connection = connection) } }
                }
                launch { repository.loadMore(ChatListArchive) }
                launch { repository.loadStories() }
                val latestRows = Channel<List<ChatListRow>>(Channel.CONFLATED)
                launch {
                    for (rows in latestRows) {
                        resolveCustomEmoji(rows)
                        loadForumTopics(rows)
                    }
                }
                selected
                    .flatMapLatest { list ->
                        val mapper = ChatListRowMapper()
                        repository.chatList(list).map(mapper::map).distinctUntilChanged { old, new -> old === new }
                    }
                    .flowOn(Dispatchers.Default)
                    .conflate()
                    .collect { rows ->
                        reduce { state.copy(rows = rows) }
                        latestRows.trySend(rows)
                    }
            }
        }
    }

    fun onAction(action: ChatListScreenAction) {
        when (action) {
            OnEndReached -> onEndReached()
            is OnFolderSelect -> onFolderSelect(action.chatList)
            OnSearchClick, OnEditClick, OnNewStoryClick, OnComposeClick -> Unit
        }
    }

    private suspend fun Syntax<ChatListScreenState, Nothing>.resolveCustomEmoji(rows: List<ChatListRow>) {
        val missing = rows.flatMapTo(HashSet()) { it.customEmojiIds } - state.customEmoji.keys
        if (missing.isEmpty()) return
        customEmojiRepository.stickers(missing).onRight { found ->
            reduce { state.copy(customEmoji = state.customEmoji + found) }
        }
    }

    private val requestedTopics = HashSet<Long>()

    private suspend fun loadForumTopics(rows: List<ChatListRow>) {
        rows.filter {
            it.avatar.shape == RoundedRect && it.topics.isEmpty() &&
                requestedTopics.add(it.chatId)
        }
            .forEach { repository.loadForumTopics(it.chatId) }
    }

    private fun onFolderSelect(chatList: ChatList) = intent {
        if (chatList == state.selected) return@intent
        reduce {
            state.copy(
                selected = chatList,
                rows = [],
                isComplete = false,
                loadFailed = false,
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
            ifRight = { hasMore -> reduce { state.copy(isLoading = false, isComplete = !hasMore) } },
        )
    }
}
