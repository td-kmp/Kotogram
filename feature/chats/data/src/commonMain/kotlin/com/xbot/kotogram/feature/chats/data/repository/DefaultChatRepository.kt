package com.xbot.kotogram.feature.chats.data.repository

import arrow.core.Either
import arrow.core.recover
import com.xbot.kotogram.core.account.extensions.isNotFound
import com.xbot.kotogram.core.account.extensions.sendCatching
import com.xbot.kotogram.feature.chats.data.extensions.chatListEntries
import com.xbot.kotogram.feature.chats.data.extensions.hasSameEntriesAs
import com.xbot.kotogram.feature.chats.data.extensions.toChatError
import com.xbot.kotogram.feature.chats.data.store.ChatStore
import com.xbot.kotogram.feature.chats.domain.model.ChatError
import com.xbot.kotogram.feature.chats.domain.model.ChatListEntry
import com.xbot.kotogram.feature.chats.domain.repository.ChatRepository
import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.GetChat
import io.xbot.tdlib.api.LoadChats
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class DefaultChatRepository(private val store: ChatStore, private val client: TdClient) :
    ChatRepository {

    override fun chatList(list: ChatList): Flow<List<ChatListEntry>> = store.state
        .distinctUntilChanged { old, new -> old.hasSameEntriesAs(new, list) }
        .map { it.chatListEntries(list) }

    override suspend fun loadMore(list: ChatList): Either<ChatError, Boolean> =
        client.sendCatching(LoadChats(chatList = list, limit = PAGE_SIZE))
            .map { true }
            // TDLib's way of saying the whole list is loaded.
            .recover { error -> if (error.isNotFound) false else raise(error.toChatError()) }

    override fun chat(chatId: Long): Flow<Chat?> = flow {
        // A restored back stack can name a chat TDLib hasn't told this instance about yet.
        if (store.state.value.chats[chatId] == null) client.sendCatching(GetChat(chatId = chatId))
        emitAll(
            store.state
                .map { it.chats[chatId] }
                // By identity: equals on DTOs is deep and has a ByteArray hole.
                .distinctUntilChanged { old, new -> old === new }
        )
    }

    private companion object {
        const val PAGE_SIZE = 30
    }
}
