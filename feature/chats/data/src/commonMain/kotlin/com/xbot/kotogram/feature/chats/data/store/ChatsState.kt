package com.xbot.kotogram.feature.chats.data.store

import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatActiveStories
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.ScopeNotificationSettings
import io.xbot.tdlib.api.SecretChat
import io.xbot.tdlib.api.Supergroup
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
    val supergroups: PersistentMap<Long, Supergroup> = persistentMapOf(),
    val secretChats: PersistentMap<Int, SecretChat> = persistentMapOf(),
    val actions: PersistentMap<Long, ChatActions> = persistentMapOf(),
    val stories: PersistentMap<Long, ChatActiveStories> = persistentMapOf(),
    val scopeSettings: PersistentMap<NotificationScope, ScopeNotificationSettings> = persistentMapOf(),
    val topics: PersistentMap<Long, ForumTopicsState> = persistentMapOf(),
    val options: AccountOptions = AccountOptions(),
) {
    @Suppress("LongParameterList")
    fun copy(
        chats: PersistentMap<Long, Chat> = this.chats,
        users: PersistentMap<Long, User> = this.users,
        chatLists: PersistentMap<ChatList, ChatListOrder> = this.chatLists,
        folders: UpdateChatFolders? = this.folders,
        unreadChatCounts: PersistentMap<ChatList, UpdateUnreadChatCount> = this.unreadChatCounts,
        supergroups: PersistentMap<Long, Supergroup> = this.supergroups,
        secretChats: PersistentMap<Int, SecretChat> = this.secretChats,
        actions: PersistentMap<Long, ChatActions> = this.actions,
        stories: PersistentMap<Long, ChatActiveStories> = this.stories,
        scopeSettings: PersistentMap<NotificationScope, ScopeNotificationSettings> = this.scopeSettings,
        topics: PersistentMap<Long, ForumTopicsState> = this.topics,
        options: AccountOptions = this.options,
    ) = ChatsState(
        chats,
        users,
        chatLists,
        folders,
        unreadChatCounts,
        supergroups,
        secretChats,
        actions,
        stories,
        scopeSettings,
        topics,
        options,
    )

    companion object {
        val Empty = ChatsState()
    }
}

internal enum class NotificationScope { PrivateChats, GroupChats, ChannelChats }

internal class AccountOptions(
    val myUserId: Long = 0L,
    val repliesChatId: Long = 0L,
    val serviceChatId: Long = 0L,
    val verificationCodesChatId: Long = 0L,
) {
    fun copy(
        myUserId: Long = this.myUserId,
        repliesChatId: Long = this.repliesChatId,
        serviceChatId: Long = this.serviceChatId,
        verificationCodesChatId: Long = this.verificationCodesChatId,
    ) = AccountOptions(myUserId, repliesChatId, serviceChatId, verificationCodesChatId)
}
