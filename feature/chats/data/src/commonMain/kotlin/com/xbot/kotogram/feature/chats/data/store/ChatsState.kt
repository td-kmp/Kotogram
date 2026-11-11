package com.xbot.kotogram.feature.chats.data.store

import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.UpdateChatFolders
import io.xbot.tdlib.api.UpdateUnreadChatCount
import io.xbot.tdlib.api.User
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

// Workaround: a plain class, StateFlow compares with equals and the DTOs' generated one has a ByteArray hole.
internal class ChatsState(
    val chats: PersistentMap<Long, Chat> = persistentMapOf(),
    val users: PersistentMap<Long, User> = persistentMapOf(),
    val chatLists: PersistentMap<ChatList, ChatListOrder> = persistentMapOf(),
    val folders: UpdateChatFolders? = null,
    val unreadChatCounts: PersistentMap<ChatList, UpdateUnreadChatCount> = persistentMapOf(),
) {
    fun copy(
        chats: PersistentMap<Long, Chat> = this.chats,
        users: PersistentMap<Long, User> = this.users,
        chatLists: PersistentMap<ChatList, ChatListOrder> = this.chatLists,
        folders: UpdateChatFolders? = this.folders,
        unreadChatCounts: PersistentMap<ChatList, UpdateUnreadChatCount> = this.unreadChatCounts,
    ) = ChatsState(chats, users, chatLists, folders, unreadChatCounts)

    companion object {
        val Empty = ChatsState()
    }
}
