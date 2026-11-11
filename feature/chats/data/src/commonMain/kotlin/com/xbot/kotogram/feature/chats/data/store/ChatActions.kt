package com.xbot.kotogram.feature.chats.data.store

import io.xbot.tdlib.api.ChatAction
import io.xbot.tdlib.api.MessageSender
import kotlinx.collections.immutable.PersistentList

internal typealias ChatActions = PersistentList<ChatActionEntry>

internal class ChatActionEntry(
    val sender: MessageSender,
    val action: ChatAction,
)
