package com.xbot.kotogram.feature.chats.data.extensions

import com.xbot.kotogram.feature.chats.data.store.NotificationScope
import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatTypeBasicGroup
import io.xbot.tdlib.api.ChatTypePrivate
import io.xbot.tdlib.api.ChatTypeSecret
import io.xbot.tdlib.api.ChatTypeSupergroup

internal fun Chat.scope(): NotificationScope = when (val type = type) {
    is ChatTypePrivate, is ChatTypeSecret -> NotificationScope.PrivateChats
    is ChatTypeBasicGroup -> NotificationScope.GroupChats
    is ChatTypeSupergroup -> if (type.isChannel) NotificationScope.ChannelChats else NotificationScope.GroupChats
}
