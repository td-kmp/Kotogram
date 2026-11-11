package com.xbot.kotogram.feature.chats.data.store

import io.xbot.tdlib.api.ForumTopicInfo
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

internal class ForumTopicsState(
    val infos: PersistentMap<Int, ForumTopicInfo> = persistentMapOf(),
    val order: PersistentList<Int> = persistentListOf(),
    val lastReadInbox: PersistentMap<Int, Long> = persistentMapOf(),
    val lastIncoming: PersistentMap<Int, Long> = persistentMapOf(),
) {
    fun copy(
        infos: PersistentMap<Int, ForumTopicInfo> = this.infos,
        order: PersistentList<Int> = this.order,
        lastReadInbox: PersistentMap<Int, Long> = this.lastReadInbox,
        lastIncoming: PersistentMap<Int, Long> = this.lastIncoming,
    ) = ForumTopicsState(infos, order, lastReadInbox, lastIncoming)

    fun isUnread(topicId: Int): Boolean = (lastIncoming[topicId] ?: 0L) > (lastReadInbox[topicId] ?: Long.MAX_VALUE)

    fun movedToFront(topicId: Int): ForumTopicsState =
        if (order.firstOrNull() == topicId) this else copy(order = order.removing(topicId).addingAt(0, topicId))
}
