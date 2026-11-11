package com.xbot.kotogram.feature.chats.data.extensions

import com.xbot.kotogram.feature.chats.domain.model.ChatError
import io.xbot.tdlib.api.Error

internal fun Error.toChatError(): ChatError = ChatError.Unknown(message)
