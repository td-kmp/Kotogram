package com.xbot.kotogram.feature.chats.data.extensions

import com.xbot.kotogram.core.tdlib.store.AccountState
import com.xbot.kotogram.feature.chats.domain.model.ChatListEntry
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.MessageSenderChat
import io.xbot.tdlib.api.MessageSenderUser

/** Whether [other] gives the same entries of [list]. Identity: unchanged entities stay one instance. */
internal fun AccountState.hasSameEntriesAs(other: AccountState, list: ChatList): Boolean =
    chatLists[list] === other.chatLists[list] && chats === other.chats && users === other.users

internal fun AccountState.chatListEntries(list: ChatList): List<ChatListEntry> =
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
