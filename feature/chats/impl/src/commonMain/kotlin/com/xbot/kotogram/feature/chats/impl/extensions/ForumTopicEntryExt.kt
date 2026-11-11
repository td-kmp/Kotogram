package com.xbot.kotogram.feature.chats.impl.extensions

import com.xbot.kotogram.feature.chats.domain.model.ForumTopicEntry
import com.xbot.kotogram.feature.chats.impl.screen.list.TopicIconModel
import com.xbot.kotogram.feature.chats.impl.screen.list.TopicModel

internal fun ForumTopicEntry.toModel(): TopicModel = TopicModel(
    title = info.name,
    icon = when {
        info.isGeneral -> TopicIconModel.General
        info.icon.customEmojiId != 0L -> TopicIconModel.Emoji(info.icon.customEmojiId)
        else -> TopicIconModel.Bubble(info.icon.color)
    },
    unread = isUnread,
)
