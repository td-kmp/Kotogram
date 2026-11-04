package com.xbot.kotogram.feature.chats.data.repository

import arrow.core.Either
import arrow.core.raise.either
import com.xbot.kotogram.core.data.client.ClientHolder
import com.xbot.kotogram.core.data.store.AccountState
import com.xbot.kotogram.core.data.store.AccountStore
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.feature.chats.domain.model.ChatError
import com.xbot.kotogram.feature.chats.domain.model.ChatListEntry
import com.xbot.kotogram.feature.chats.domain.repository.ChatRepository
import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.GetChat
import io.xbot.tdlib.api.LoadChats
import io.xbot.tdlib.api.MessageSenderChat
import io.xbot.tdlib.api.MessageSenderUser
import io.xbot.tdlib.client.TdException
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
        .distinctUntilChanged { old, new ->
            old.chatLists[list] === new.chatLists[list] &&
                old.chats === new.chats &&
                old.users === new.users
        }
        .map { it.entriesOf(list) }

    override suspend fun loadMore(list: ChatList): Either<ChatError, Boolean> = either {
        try {
            client.send(LoadChats(chatList = list, limit = PAGE_SIZE))
            true
        } catch (e: TdException) {
            // TDLib's way of saying the whole list is loaded.
            if (e.code == NOT_FOUND) false else raise(ChatError.Unknown(e.error.message))
        }
    }

    override fun chat(chatId: Long): Flow<Chat?> = flow {
        // A restored back stack can name a chat TDLib hasn't told this instance about yet.
        if (store.state.value.chats[chatId] == null) {
            try {
                client.send(GetChat(chatId = chatId))
            } catch (_: TdException) {
                // No such chat: the flow stays at null.
            }
        }
        emitAll(
            store.state.map {
                it.chats[chatId]
            }.distinctUntilChanged { old, new -> old === new }
        )
    }

    private fun AccountState.entriesOf(list: ChatList): List<ChatListEntry> =
        chatLists[list]?.chatIds.orEmpty().mapNotNull { id ->
            val chat = chats[id] ?: return@mapNotNull null
            val sender = chat.lastMessage?.senderId
            ChatListEntry(
                chat = chat,
                position = chat.positions.firstOrNull { it.list == list },
                senderUser = (sender as? MessageSenderUser)?.let { users[it.userId] },
                senderChat = (sender as? MessageSenderChat)?.let { chats[it.chatId] }
            )
        }

    private companion object {
        const val PAGE_SIZE = 30
        const val NOT_FOUND = 404
    }
}
