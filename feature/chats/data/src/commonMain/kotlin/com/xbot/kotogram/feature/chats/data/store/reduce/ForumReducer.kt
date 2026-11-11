package com.xbot.kotogram.feature.chats.data.store.reduce

import com.xbot.kotogram.feature.chats.data.store.ChatsState
import com.xbot.kotogram.feature.chats.data.store.ForumTopicsState
import io.xbot.tdlib.api.MessageTopicForum
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateChatLastMessage
import io.xbot.tdlib.api.UpdateForumTopic
import io.xbot.tdlib.api.UpdateForumTopicInfo

internal object ForumReducer : Reducer {
    override fun reduce(
        state: ChatsState,
        update: Update,
    ): ChatsState = when (update) {
        is UpdateForumTopicInfo -> state.changeTopics(update.info.chatId) {
            val known = update.info.forumTopicId in it.order
            it.copy(
                infos = it.infos.putting(update.info.forumTopicId, update.info),
                order = if (known) it.order else it.order.adding(update.info.forumTopicId),
            )
        }

        is UpdateForumTopic -> state.changeTopics(update.chatId) {
            it.copy(lastReadInbox = it.lastReadInbox.putting(update.forumTopicId, update.lastReadInboxMessageId))
        }

        is UpdateChatLastMessage -> {
            val message = update.lastMessage
            val topicId = (message?.topicId as? MessageTopicForum)?.forumTopicId
            if (message == null || topicId == null || state.topics[update.chatId] == null) {
                state
            } else {
                state.changeTopics(update.chatId) { topics ->
                    val moved = topics.movedToFront(topicId)
                    if (message.isOutgoing) moved else moved.copy(
                        lastIncoming = moved.lastIncoming.putting(topicId, message.id),
                    )
                }
            }
        }

        else -> state
    }

    private inline fun ChatsState.changeTopics(
        chatId: Long,
        change: (ForumTopicsState) -> ForumTopicsState,
    ): ChatsState = copy(topics = topics.putting(chatId, change(topics[chatId] ?: ForumTopicsState())))
}
