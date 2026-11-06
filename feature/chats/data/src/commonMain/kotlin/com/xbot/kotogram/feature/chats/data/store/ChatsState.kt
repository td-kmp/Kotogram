package com.xbot.kotogram.feature.chats.data.store

import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.User
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

// A plain class: StateFlow compares with equals, and generated equals over DTOs is deep with a ByteArray hole.
internal class ChatsState(
    val chats: PersistentMap<Long, Chat> = persistentMapOf(),
    val users: PersistentMap<Long, User> = persistentMapOf(),
    val chatLists: PersistentMap<ChatList, ChatListOrder> = persistentMapOf()
) {
    fun copy(
        chats: PersistentMap<Long, Chat> = this.chats,
        users: PersistentMap<Long, User> = this.users,
        chatLists: PersistentMap<ChatList, ChatListOrder> = this.chatLists
    ) = ChatsState(chats, users, chatLists)

    companion object {
        val Empty = ChatsState()
    }
}
