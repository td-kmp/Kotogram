package com.xbot.kotogram.feature.chats.data.store

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

internal class ChatListOrder(
    private val orders: PersistentMap<Long, Long> = persistentMapOf(),
) {
    val chatIds: List<Long> by lazy {
        orders.entries
            .sortedWith(
                compareByDescending<Map.Entry<Long, Long>> {
                    it.value
                }.thenByDescending { it.key },
            )
            .map { it.key }
    }

    fun with(
        chatId: Long,
        order: Long,
    ): ChatListOrder = ChatListOrder(if (order == 0L) orders.removing(chatId) else orders.putting(chatId, order))
}
