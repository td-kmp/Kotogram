package com.xbot.kotogram.feature.chats.domain.repository

import arrow.core.Either
import com.xbot.kotogram.feature.chats.domain.model.ChatError
import com.xbot.kotogram.feature.chats.domain.model.ChatListEntry
import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatList
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    // Unchanged chats stay the same instances from one emission to the next.
    fun chatList(list: ChatList): Flow<List<ChatListEntry>>

    suspend fun loadMore(list: ChatList): Either<ChatError, Boolean>

    fun chat(chatId: Long): Flow<Chat?>
}
