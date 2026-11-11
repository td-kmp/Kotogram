package com.xbot.kotogram.feature.chats.impl.extensions

import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatTypePrivate
import io.xbot.tdlib.api.ChatTypeSecret

internal fun Chat.letters(): String {
    val words = title.split(' ').filter { it.isNotBlank() }
    val person = type is ChatTypePrivate || type is ChatTypeSecret
    val initials = if (person) words.take(2) else words.take(1)
    return initials.joinToString("") { it.firstGrapheme().uppercase() }
}
