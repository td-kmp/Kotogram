package com.xbot.kotogram.core.data.store

import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatPosition
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateChatDraftMessage
import io.xbot.tdlib.api.UpdateChatIsMarkedAsUnread
import io.xbot.tdlib.api.UpdateChatLastMessage
import io.xbot.tdlib.api.UpdateChatNotificationSettings
import io.xbot.tdlib.api.UpdateChatPhoto
import io.xbot.tdlib.api.UpdateChatPosition
import io.xbot.tdlib.api.UpdateChatReadInbox
import io.xbot.tdlib.api.UpdateChatReadOutbox
import io.xbot.tdlib.api.UpdateChatTitle
import io.xbot.tdlib.api.UpdateChatUnreadMentionCount
import io.xbot.tdlib.api.UpdateChatUnreadReactionCount
import io.xbot.tdlib.api.UpdateMessageMentionRead
import io.xbot.tdlib.api.UpdateMessageUnreadReactions
import io.xbot.tdlib.api.UpdateNewChat

internal fun AccountState.reduceChat(update: Update): AccountState = when (update) {
    is UpdateNewChat -> copy(chats = chats.putting(update.chat.id, update.chat))
        .withPositions(update.chat.id, old = emptyList(), new = update.chat.positions)

    is UpdateChatPosition -> changeChat(update.chatId) { chat ->
        val others = chat.positions.filter { it.list != update.position.list }
        val positions = if (update.position.order == 0L) others else others + update.position
        chat.copy(positions = positions)
    }.withPosition(update.chatId, update.position)

    // These two carry the chat's whole set of positions, and may arrive instead of updateChatPosition.
    is UpdateChatLastMessage -> withNewPositions(update.chatId, update.positions) {
        copy(lastMessage = update.lastMessage, positions = update.positions)
    }

    is UpdateChatDraftMessage -> withNewPositions(update.chatId, update.positions) {
        copy(draftMessage = update.draftMessage, positions = update.positions)
    }

    else -> reduceChatField(update)
}

private fun AccountState.reduceChatField(update: Update): AccountState = when (update) {
    is UpdateChatTitle -> changeChat(update.chatId) { it.copy(title = update.title) }

    is UpdateChatPhoto -> changeChat(update.chatId) { it.copy(photo = update.photo) }

    is UpdateChatReadInbox -> changeChat(update.chatId) {
        it.copy(
            lastReadInboxMessageId = update.lastReadInboxMessageId,
            unreadCount = update.unreadCount
        )
    }

    is UpdateChatReadOutbox -> changeChat(update.chatId) {
        it.copy(lastReadOutboxMessageId = update.lastReadOutboxMessageId)
    }

    is UpdateChatUnreadMentionCount -> changeChat(update.chatId) {
        it.copy(unreadMentionCount = update.unreadMentionCount)
    }

    is UpdateMessageMentionRead -> changeChat(update.chatId) {
        it.copy(unreadMentionCount = update.unreadMentionCount)
    }

    is UpdateChatUnreadReactionCount -> changeChat(update.chatId) {
        it.copy(unreadReactionCount = update.unreadReactionCount)
    }

    is UpdateMessageUnreadReactions -> changeChat(update.chatId) {
        it.copy(unreadReactionCount = update.unreadReactionCount)
    }

    is UpdateChatNotificationSettings -> changeChat(update.chatId) {
        it.copy(notificationSettings = update.notificationSettings)
    }

    is UpdateChatIsMarkedAsUnread -> changeChat(update.chatId) {
        it.copy(isMarkedAsUnread = update.isMarkedAsUnread)
    }

    else -> this
}

// TDLib guarantees updateNewChat comes first, so an unknown chat means a bug or an undecodable update.
private inline fun AccountState.changeChat(chatId: Long, change: (Chat) -> Chat): AccountState {
    val chat = chats[chatId] ?: return this
    return copy(chats = chats.putting(chatId, change(chat)))
}

private inline fun AccountState.withNewPositions(
    chatId: Long,
    positions: List<ChatPosition>,
    change: Chat.() -> Chat
): AccountState {
    val old = chats[chatId]?.positions ?: return this
    return changeChat(chatId) { it.change() }.withPositions(chatId, old, positions)
}

private fun AccountState.withPositions(
    chatId: Long,
    old: List<ChatPosition>,
    new: List<ChatPosition>
): AccountState {
    val left = old.filter { position -> new.none { it.list == position.list } }
    val afterLeft = left.fold(this) { state, position ->
        state.withPosition(chatId, position.copy(order = 0L))
    }
    return new.fold(afterLeft) { state, position -> state.withPosition(chatId, position) }
}

private fun AccountState.withPosition(chatId: Long, position: ChatPosition): AccountState {
    val order = chatLists[position.list] ?: ChatListOrder()
    return copy(chatLists = chatLists.putting(position.list, order.with(chatId, position.order)))
}
