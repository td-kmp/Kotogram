package com.xbot.kotogram.feature.chats.data.store

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

// TDLib sorts a list by the pair (order, chat id), descending.
internal class ChatListOrder(private val orders: PersistentMap<Long, Long> = persistentMapOf()) {
    // Sorted on the first read of an instance, so a burst of position updates sorts nothing.
    val chatIds: List<Long> by lazy {
        orders.entries
            .sortedWith(
                compareByDescending<Map.Entry<Long, Long>> {
                    it.value
                }.thenByDescending { it.key }
            )
            .map { it.key }
    }

    // Order 0 means the chat left this list.
    fun with(chatId: Long, order: Long): ChatListOrder =
        ChatListOrder(if (order == 0L) orders.removing(chatId) else orders.putting(chatId, order))
}
