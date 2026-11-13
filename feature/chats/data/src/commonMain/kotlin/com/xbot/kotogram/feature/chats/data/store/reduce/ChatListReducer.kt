package com.xbot.kotogram.feature.chats.data.store.reduce

import com.xbot.kotogram.feature.chats.data.store.ChatListOrder
import com.xbot.kotogram.feature.chats.data.store.ChatsState
import io.xbot.tdlib.api.ChatPosition
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateChatDraftMessage
import io.xbot.tdlib.api.UpdateChatLastMessage
import io.xbot.tdlib.api.UpdateChatPosition
import io.xbot.tdlib.api.UpdateNewChat

internal object ChatListReducer : Reducer {
    override fun reduce(
        state: ChatsState,
        update: Update,
    ): ChatsState = when (update) {
        is UpdateNewChat ->
            state.withPositions(update.chat.id, old = [], new = update.chat.positions)

        is UpdateChatPosition -> state.withPosition(update.chatId, update.position)

        is UpdateChatLastMessage -> state.withNewPositions(update.chatId, update.positions)

        is UpdateChatDraftMessage -> state.withNewPositions(update.chatId, update.positions)

        else -> state
    }

    private fun ChatsState.withNewPositions(
        chatId: Long,
        positions: List<ChatPosition>,
    ): ChatsState {
        val old = chats[chatId]?.positions ?: return this
        return withPositions(chatId, old, positions)
    }

    private fun ChatsState.withPositions(
        chatId: Long,
        old: List<ChatPosition>,
        new: List<ChatPosition>,
    ): ChatsState {
        val left = old.filter { position -> new.none { it.list == position.list } }
        val afterLeft = left.fold(this) { state, position ->
            state.withPosition(chatId, position.copy(order = 0L))
        }
        return new.fold(afterLeft) { state, position -> state.withPosition(chatId, position) }
    }

    private fun ChatsState.withPosition(
        chatId: Long,
        position: ChatPosition,
    ): ChatsState {
        val order = chatLists[position.list] ?: ChatListOrder()
        return copy(
            chatLists = chatLists.putting(position.list, order.with(chatId, position.order)),
        )
    }
}
