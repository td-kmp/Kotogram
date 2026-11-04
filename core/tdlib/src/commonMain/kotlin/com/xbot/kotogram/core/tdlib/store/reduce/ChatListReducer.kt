package com.xbot.kotogram.core.tdlib.store.reduce

import com.xbot.kotogram.core.tdlib.store.AccountState
import com.xbot.kotogram.core.tdlib.store.ChatListOrder
import io.xbot.tdlib.api.ChatPosition
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateChatDraftMessage
import io.xbot.tdlib.api.UpdateChatLastMessage
import io.xbot.tdlib.api.UpdateChatPosition
import io.xbot.tdlib.api.UpdateNewChat

/** Which chats are in which list. The chat's own copy of its positions is [ChatReducer]'s. */
internal object ChatListReducer : Reducer {
    override fun reduce(state: AccountState, update: Update): AccountState = when (update) {
        is UpdateNewChat ->
            state.withPositions(update.chat.id, old = emptyList(), new = update.chat.positions)

        is UpdateChatPosition -> state.withPosition(update.chatId, update.position)

        // These two carry the chat's whole set of positions, and may arrive instead of updateChatPosition.
        is UpdateChatLastMessage -> state.withNewPositions(update.chatId, update.positions)

        is UpdateChatDraftMessage -> state.withNewPositions(update.chatId, update.positions)

        else -> state
    }

    private fun AccountState.withNewPositions(
        chatId: Long,
        positions: List<ChatPosition>
    ): AccountState {
        val old = chats[chatId]?.positions ?: return this
        return withPositions(chatId, old, positions)
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
        return copy(
            chatLists = chatLists.putting(position.list, order.with(chatId, position.order))
        )
    }
}
