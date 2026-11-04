package com.xbot.kotogram.core.data.store

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

/** The chats of one chat list. TDLib's rule: sorted by the pair (order, chat id), descending. */
class ChatListOrder internal constructor(
    private val orders: PersistentMap<Long, Long> = persistentMapOf()
) {
    // Sorted on the first read of an instance, off the lane, so a burst of position updates sorts nothing.
    val chatIds: List<Long> by lazy {
        orders.entries
            .sortedWith(
                compareByDescending<Map.Entry<Long, Long>> {
                    it.value
                }.thenByDescending { it.key }
            )
            .map { it.key }
    }

    /** Order 0 means the chat isn't in this list. */
    internal fun with(chatId: Long, order: Long): ChatListOrder =
        ChatListOrder(if (order == 0L) orders.removing(chatId) else orders.putting(chatId, order))
}
