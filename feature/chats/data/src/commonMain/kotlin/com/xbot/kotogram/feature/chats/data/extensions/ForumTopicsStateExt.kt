package com.xbot.kotogram.feature.chats.data.extensions

import com.xbot.kotogram.feature.chats.data.store.ForumTopicsState
import com.xbot.kotogram.feature.chats.domain.model.ForumTopicEntry

internal fun ForumTopicsState.entries(): List<ForumTopicEntry> = order.asSequence()
    .mapNotNull { id -> infos[id]?.let { ForumTopicEntry(it, isUnread(id)) } }
    .filterNot { it.info.isHidden }
    .take(FORUM_PREVIEW_TOPICS)
    .toList()

private const val FORUM_PREVIEW_TOPICS = 5
