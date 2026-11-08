package com.xbot.kotogram.feature.chats.data.store.reduce

import com.xbot.kotogram.feature.chats.data.store.ChatsState
import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateChatAccentColors
import io.xbot.tdlib.api.UpdateChatDraftMessage
import io.xbot.tdlib.api.UpdateChatEmojiStatus
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

internal object ChatReducer : Reducer {
    override fun reduce(state: ChatsState, update: Update): ChatsState = when (update) {
        is UpdateNewChat -> state.copy(chats = state.chats.putting(update.chat.id, update.chat))

        is UpdateChatPosition -> state.changeChat(update.chatId) { chat ->
            val others = chat.positions.filter { it.list != update.position.list }
            val positions = if (update.position.order == 0L) others else others + update.position
            chat.copy(positions = positions)
        }

        is UpdateChatLastMessage -> state.changeChat(update.chatId) {
            it.copy(lastMessage = update.lastMessage, positions = update.positions)
        }

        is UpdateChatDraftMessage -> state.changeChat(update.chatId) {
            it.copy(draftMessage = update.draftMessage, positions = update.positions)
        }

        is UpdateChatTitle -> state.changeChat(update.chatId) { it.copy(title = update.title) }

        is UpdateChatPhoto -> state.changeChat(update.chatId) { it.copy(photo = update.photo) }

        is UpdateChatEmojiStatus -> state.changeChat(update.chatId) {
            it.copy(emojiStatus = update.emojiStatus)
        }

        is UpdateChatAccentColors -> state.changeChat(update.chatId) {
            it.copy(accentColorId = update.accentColorId)
        }

        is UpdateChatNotificationSettings -> state.changeChat(update.chatId) {
            it.copy(notificationSettings = update.notificationSettings)
        }

        else -> state.reduceReadState(update)
    }

    private fun ChatsState.reduceReadState(update: Update): ChatsState = when (update) {
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

        is UpdateChatIsMarkedAsUnread -> changeChat(update.chatId) {
            it.copy(isMarkedAsUnread = update.isMarkedAsUnread)
        }

        else -> this
    }

    // TDLib guarantees updateNewChat comes first, so an unknown chat means a bug or an undecodable update.
    private inline fun ChatsState.changeChat(chatId: Long, change: (Chat) -> Chat): ChatsState {
        val chat = chats[chatId] ?: return this
        return copy(chats = chats.putting(chatId, change(chat)))
    }
}
