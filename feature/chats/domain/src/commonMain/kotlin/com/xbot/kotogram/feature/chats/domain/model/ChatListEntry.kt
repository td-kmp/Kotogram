package com.xbot.kotogram.feature.chats.domain.model

import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatPosition
import io.xbot.tdlib.api.User

class ChatListEntry(
    val chat: Chat,
    val position: ChatPosition?,
    val senderUser: User?,
    val senderChat: Chat?
)
