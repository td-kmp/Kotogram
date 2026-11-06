package com.xbot.kotogram.feature.chats.data.extensions

import com.xbot.kotogram.feature.chats.data.store.ChatsState
import com.xbot.kotogram.feature.chats.data.store.reduce.ChatsReducer
import com.xbot.kotogram.feature.chats.domain.model.ChatListEntry
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.MessageSenderChat
import io.xbot.tdlib.api.MessageSenderUser
import io.xbot.tdlib.api.Update

internal fun ChatsState.reduce(update: Update): ChatsState = ChatsReducer.reduce(this, update)

// By identity: unchanged entities stay one instance.
internal fun ChatsState.hasSameEntriesAs(other: ChatsState, list: ChatList): Boolean =
    chatLists[list] === other.chatLists[list] && chats === other.chats && users === other.users

internal fun ChatsState.chatListEntries(list: ChatList): List<ChatListEntry> =
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
