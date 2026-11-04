package com.xbot.kotogram.core.tdlib.store

import io.xbot.tdlib.api.BasicGroup
import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.ConnectionState
import io.xbot.tdlib.api.OptionValue
import io.xbot.tdlib.api.OptionValueInteger
import io.xbot.tdlib.api.SecretChat
import io.xbot.tdlib.api.Supergroup
import io.xbot.tdlib.api.User
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

/**
 * One account's TDLib state as an immutable snapshot, a pure fold of its updates. Unchanged entities stay
 * the same instance, which is what Compose skipping and identity selectors rely on.
 *
 * A plain class: StateFlow compares with equals, and generated equals over DTOs is deep with a ByteArray hole.
 */
class AccountState internal constructor(
    val options: PersistentMap<String, OptionValue> = persistentMapOf(),
    val connectionState: ConnectionState? = null,
    val chats: PersistentMap<Long, Chat> = persistentMapOf(),
    val users: PersistentMap<Long, User> = persistentMapOf(),
    val basicGroups: PersistentMap<Long, BasicGroup> = persistentMapOf(),
    val supergroups: PersistentMap<Long, Supergroup> = persistentMapOf(),
    val secretChats: PersistentMap<Int, SecretChat> = persistentMapOf(),
    val chatLists: PersistentMap<ChatList, ChatListOrder> = persistentMapOf()
) {
    val myId: Long? get() = (options[MY_ID] as? OptionValueInteger)?.value

    @Suppress("LongParameterList")
    internal fun copy(
        options: PersistentMap<String, OptionValue> = this.options,
        connectionState: ConnectionState? = this.connectionState,
        chats: PersistentMap<Long, Chat> = this.chats,
        users: PersistentMap<Long, User> = this.users,
        basicGroups: PersistentMap<Long, BasicGroup> = this.basicGroups,
        supergroups: PersistentMap<Long, Supergroup> = this.supergroups,
        secretChats: PersistentMap<Int, SecretChat> = this.secretChats,
        chatLists: PersistentMap<ChatList, ChatListOrder> = this.chatLists
    ) = AccountState(
        options,
        connectionState,
        chats,
        users,
        basicGroups,
        supergroups,
        secretChats,
        chatLists
    )

    internal companion object {
        val Empty = AccountState()
        private const val MY_ID = "my_id"
    }
}
