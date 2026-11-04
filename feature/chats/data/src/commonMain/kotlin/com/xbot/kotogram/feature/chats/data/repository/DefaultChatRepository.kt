package com.xbot.kotogram.feature.chats.data.repository

import arrow.core.Either
import arrow.core.recover
import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.core.tdlib.client.ClientHolder
import com.xbot.kotogram.core.tdlib.extensions.isNotFound
import com.xbot.kotogram.core.tdlib.extensions.select
import com.xbot.kotogram.core.tdlib.extensions.sendCatching
import com.xbot.kotogram.core.tdlib.store.AccountStore
import com.xbot.kotogram.feature.chats.data.extensions.chatListEntries
import com.xbot.kotogram.feature.chats.data.extensions.hasSameEntriesAs
import com.xbot.kotogram.feature.chats.data.extensions.toChatError
import com.xbot.kotogram.feature.chats.domain.model.ChatError
import com.xbot.kotogram.feature.chats.domain.model.ChatListEntry
import com.xbot.kotogram.feature.chats.domain.repository.ChatRepository
import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.GetChat
import io.xbot.tdlib.api.LoadChats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

@Scope(AccountId::class)
@Scoped
internal class DefaultChatRepository(
    private val store: AccountStore,
    private val client: ClientHolder
) : ChatRepository {

    override fun chatList(list: ChatList): Flow<List<ChatListEntry>> = store.state
        .distinctUntilChanged { old, new -> old.hasSameEntriesAs(new, list) }
        .map { it.chatListEntries(list) }

    override suspend fun loadMore(list: ChatList): Either<ChatError, Boolean> =
        client.sendCatching(LoadChats(chatList = list, limit = PAGE_SIZE))
            .map { true }
            // TDLib's way of saying the whole list is loaded.
            .recover { error -> if (error.isNotFound) false else raise(error.toChatError()) }

    override fun chat(chatId: Long): Flow<Chat?> = flow {
        // A restored back stack can name a chat TDLib hasn't told this instance about yet. If there
        // is no such chat, the flow stays at null.
        if (store.state.value.chats[chatId] == null) client.sendCatching(GetChat(chatId = chatId))
        emitAll(store.select { it.chats[chatId] })
    }

    private companion object {
        const val PAGE_SIZE = 30
    }
}
