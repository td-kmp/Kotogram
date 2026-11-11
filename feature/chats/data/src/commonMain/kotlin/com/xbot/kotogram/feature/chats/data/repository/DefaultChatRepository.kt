package com.xbot.kotogram.feature.chats.data.repository

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.recover
import com.xbot.kotogram.core.account.extensions.isNotFound
import com.xbot.kotogram.core.account.extensions.sendCatching
import com.xbot.kotogram.feature.chats.data.extensions.archiveSummary
import com.xbot.kotogram.feature.chats.data.extensions.chatListEntries
import com.xbot.kotogram.feature.chats.data.extensions.folderTabs
import com.xbot.kotogram.feature.chats.data.extensions.hasSameEntriesAs
import com.xbot.kotogram.feature.chats.data.extensions.toChatError
import com.xbot.kotogram.feature.chats.data.store.ChatStore
import com.xbot.kotogram.feature.chats.domain.model.ArchiveSummary
import com.xbot.kotogram.feature.chats.domain.model.ChatError
import com.xbot.kotogram.feature.chats.domain.model.ChatFolderTab
import com.xbot.kotogram.feature.chats.domain.model.ChatListEntry
import com.xbot.kotogram.feature.chats.domain.repository.ChatRepository
import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.ChatListArchive
import io.xbot.tdlib.api.GetChat
import io.xbot.tdlib.api.GetForumTopics
import io.xbot.tdlib.api.LoadActiveStories
import io.xbot.tdlib.api.LoadChats
import io.xbot.tdlib.api.StoryListArchive
import io.xbot.tdlib.api.StoryListMain
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class DefaultChatRepository(
    private val store: ChatStore,
    private val client: TdClient,
) : ChatRepository {

    override fun chatList(list: ChatList): Flow<List<ChatListEntry>> = store.state
        .distinctUntilChanged { old, new -> old.hasSameEntriesAs(new, list) }
        .map { it.chatListEntries(list) }

    override suspend fun loadMore(list: ChatList): Either<ChatError, Boolean> =
        client.sendCatching(LoadChats(chatList = list, limit = PAGE_SIZE))
            .map { true }
            // Workaround: TDLib answers 404 once the whole list is loaded.
            .recover { error -> if (error.isNotFound) false else raise(error.toChatError()) }

    override fun folderTabs(): Flow<List<ChatFolderTab>> = store.state
        .distinctUntilChanged { old, new ->
            old.folders === new.folders && old.unreadChatCounts === new.unreadChatCounts
        }
        .map { it.folderTabs() }

    override fun archive(): Flow<ArchiveSummary?> = store.state
        .distinctUntilChanged { old, new ->
            old.chatLists[ChatListArchive] === new.chatLists[ChatListArchive] &&
                old.chats === new.chats &&
                old.unreadChatCounts === new.unreadChatCounts
        }
        .map { it.archiveSummary() }

    override suspend fun loadStories(): Either<ChatError, Unit> = either {
        for (list in listOf(StoryListMain, StoryListArchive)) {
            do {
                val more = client.sendCatching(LoadActiveStories(storyList = list))
                    .map { true }
                    // Workaround: TDLib answers 404 once every active story of the list is loaded.
                    .recover { error -> if (error.isNotFound) false else raise(error.toChatError()) }
                    .bind()
            } while (more)
        }
    }

    override suspend fun loadForumTopics(chatId: Long): Either<ChatError, Unit> =
        client.sendCatching(GetForumTopics(chatId = chatId, limit = TOPICS_PAGE_SIZE))
            .map { store.putForumTopics(chatId, it) }
            .mapLeft { it.toChatError() }

    override fun chat(chatId: Long): Flow<Chat?> = flow {
        // Workaround: a restored back stack can name a chat TDLib hasn't told this instance about yet.
        if (store.state.value.chats[chatId] == null) client.sendCatching(GetChat(chatId = chatId))
        emitAll(
            store.state
                .map { it.chats[chatId] }
                // Workaround: compared by identity, generated equals on DTOs is deep and has a ByteArray hole.
                .distinctUntilChanged { old, new -> old === new },
        )
    }

    private companion object {
        const val PAGE_SIZE = 30
        const val TOPICS_PAGE_SIZE = 10
    }
}
