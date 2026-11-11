package com.xbot.kotogram.feature.chats.impl.extensions

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xbot.kotogram.core.designsystem.component.ForumTopic
import com.xbot.kotogram.core.designsystem.component.TopicIcon
import com.xbot.kotogram.feature.chats.impl.component.CustomEmoji
import com.xbot.kotogram.feature.chats.impl.screen.list.TopicIconModel
import com.xbot.kotogram.feature.chats.impl.screen.list.TopicModel
import io.xbot.tdlib.api.Sticker

@Composable
internal fun TopicModel.toTopic(customEmoji: Map<Long, Sticker>): ForumTopic = ForumTopic(
    title = title,
    icon = when (val icon = icon) {
        TopicIconModel.General -> TopicIcon.General

        is TopicIconModel.Bubble -> TopicIcon.Bubble(icon.color)

        is TopicIconModel.Emoji -> TopicIcon.Emoji {
            CustomEmoji(customEmoji[icon.customEmojiId], "", Modifier.fillMaxSize(), loops = 0)
        }

        null -> null
    },
    unread = unread,
)
