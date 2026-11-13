package com.xbot.kotogram.feature.chats.data.extensions

import com.xbot.kotogram.feature.chats.data.store.ChatsState
import com.xbot.kotogram.feature.chats.data.store.ForumTopicsState
import com.xbot.kotogram.feature.chats.data.store.NotificationScope
import com.xbot.kotogram.feature.chats.data.store.reduce.ChatsReducer
import com.xbot.kotogram.feature.chats.domain.model.ArchiveSummary
import com.xbot.kotogram.feature.chats.domain.model.ChatFolderTab
import com.xbot.kotogram.feature.chats.domain.model.ChatListEntry
import com.xbot.kotogram.feature.chats.domain.model.ChatTyping
import com.xbot.kotogram.feature.chats.domain.model.ForumTopicEntry
import com.xbot.kotogram.feature.chats.domain.model.SpecialChat
import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatFolderInfo
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.ChatListArchive
import io.xbot.tdlib.api.ChatListFolder
import io.xbot.tdlib.api.ChatListMain
import io.xbot.tdlib.api.ChatTypeBasicGroup
import io.xbot.tdlib.api.ChatTypePrivate
import io.xbot.tdlib.api.ChatTypeSecret
import io.xbot.tdlib.api.ChatTypeSupergroup
import io.xbot.tdlib.api.ForumTopics
import io.xbot.tdlib.api.MessageChatAddMembers
import io.xbot.tdlib.api.MessageChatDeleteMember
import io.xbot.tdlib.api.MessageContent
import io.xbot.tdlib.api.MessageGiftedPremium
import io.xbot.tdlib.api.MessageInviteVideoChatParticipants
import io.xbot.tdlib.api.MessageSenderChat
import io.xbot.tdlib.api.MessageSenderUser
import io.xbot.tdlib.api.Update
import kotlinx.collections.immutable.toPersistentList

internal fun ChatsState.reduce(update: Update): ChatsState = ChatsReducer.reduce(this, update)

internal fun ChatsState.hasSameEntriesAs(
    other: ChatsState,
    list: ChatList,
): Boolean = chatLists[list] === other.chatLists[list] &&
    chats === other.chats &&
    users === other.users &&
    supergroups === other.supergroups &&
    secretChats === other.secretChats &&
    actions === other.actions &&
    stories === other.stories &&
    scopeSettings === other.scopeSettings &&
    topics === other.topics &&
    folders === other.folders &&
    options === other.options

internal fun ChatsState.folderTabs(): List<ChatFolderTab> {
    val update = folders ?: return []
    val tabs = update.chatFolders.map { folder ->
        tab(ChatListFolder(chatFolderId = folder.id), folder)
    }
    val main = tab(ChatListMain, folder = null)
    return tabs.toMutableList().apply { add(update.mainChatListPosition.coerceIn(0, size), main) }
}

private fun ChatsState.tab(
    list: ChatList,
    folder: ChatFolderInfo?,
): ChatFolderTab {
    val counts = unreadChatCounts[list]
    return ChatFolderTab(
        chatList = list,
        folder = folder,
        unreadCount = counts?.unreadCount ?: 0,
        hasUnmutedUnread = (counts?.unreadUnmutedCount ?: 0) > 0,
    )
}

internal fun ChatsState.chatListEntries(list: ChatList): List<ChatListEntry> =
    chatLists[list]?.chatIds.orEmpty().mapNotNull { id -> chats[id]?.let { entry(it, list) } }

internal fun ChatsState.archiveSummary(): ArchiveSummary? {
    val ids = chatLists[ChatListArchive]?.chatIds.orEmpty()
    if (ids.isEmpty()) return null
    val counts = unreadChatCounts[ChatListArchive]
    return ArchiveSummary(
        chats = ids.take(ARCHIVE_PREVIEW_CHATS).mapNotNull { chats[it] },
        unreadCount = counts?.unreadCount ?: 0,
        hasUnmutedUnread = (counts?.unreadUnmutedCount ?: 0) > 0,
    )
}

private fun ChatsState.entry(
    chat: Chat,
    list: ChatList,
): ChatListEntry {
    val sender = chat.lastMessage?.senderId
    val type = chat.type
    val userId = when (type) {
        is ChatTypePrivate -> type.userId
        is ChatTypeSecret -> type.userId
        else -> null
    }
    val supergroup = (type as? ChatTypeSupergroup)?.let { supergroups[it.supergroupId] }
    return ChatListEntry(
        chat = chat,
        position = chat.positions.firstOrNull { it.list == list },
        senderUser = (sender as? MessageSenderUser)?.let { users[it.userId] },
        senderChat = (sender as? MessageSenderChat)?.let { chats[it.chatId] },
        user = userId?.let { users[it] },
        supergroup = supergroup,
        secretChat = (type as? ChatTypeSecret)?.let { secretChats[it.secretChatId] },
        typing = actions[chat.id].orEmpty().map { entry ->
            val actor = entry.sender
            ChatTyping(
                user = (actor as? MessageSenderUser)?.let { users[it.userId] },
                chat = (actor as? MessageSenderChat)?.let { chats[it.chatId] },
                action = entry.action,
            )
        },
        stories = stories[chat.id],
        scopeSettings = scopeSettings[chat.scope()],
        topics = if (supergroup?.isForum == true) topics[chat.id]?.entries().orEmpty() else [],
        tags = tags(chat),
        mentionedUsers = chat.lastMessage?.content?.mentionedUserIds().orEmpty().mapNotNull { users[it] },
        special = special(chat),
        myUserId = options.myUserId,
    )
}

private fun ChatsState.tags(chat: Chat): List<ChatFolderInfo> {
    val update = folders?.takeIf { it.areTagsEnabled } ?: return []
    return update.chatFolders.filter { folder ->
        folder.colorId >= 0 && chat.chatLists.any { it is ChatListFolder && it.chatFolderId == folder.id }
    }
}

private fun ChatsState.special(chat: Chat): SpecialChat? = when (chat.id) {
    0L -> null
    options.myUserId -> SpecialChat.SavedMessages
    options.repliesChatId -> SpecialChat.Replies
    options.serviceChatId -> SpecialChat.ServiceNotifications
    options.verificationCodesChatId -> SpecialChat.VerificationCodes
    else -> null
}

private const val ARCHIVE_PREVIEW_CHATS = 20

internal fun ChatsState.withForumTopics(
    chatId: Long,
    result: ForumTopics,
): ChatsState {
    val current = topics[chatId] ?: ForumTopicsState()
    var infos = current.infos
    var read = current.lastReadInbox
    var incoming = current.lastIncoming
    result.topics.forEach { topic ->
        val id = topic.info.forumTopicId
        infos = infos.putting(id, topic.info)
        read = read.putting(id, topic.lastReadInboxMessageId)
        topic.lastMessage?.takeUnless { it.isOutgoing }?.let { incoming = incoming.putting(id, it.id) }
    }
    val loaded = result.topics.map { it.info.forumTopicId }
    val order = (loaded + current.order.filterNot { it in loaded }).toPersistentList()
    return copy(
        topics = topics.putting(
            chatId,
            current.copy(infos = infos, order = order, lastReadInbox = read, lastIncoming = incoming),
        ),
    )
}
