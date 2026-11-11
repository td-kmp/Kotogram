package com.xbot.kotogram.feature.chats.domain.model

import io.xbot.tdlib.api.ChatFolderInfo
import io.xbot.tdlib.api.ChatList

class ChatFolderTab(
    val chatList: ChatList,
    val folder: ChatFolderInfo?,
    val unreadCount: Int,
    val hasUnmutedUnread: Boolean,
)
