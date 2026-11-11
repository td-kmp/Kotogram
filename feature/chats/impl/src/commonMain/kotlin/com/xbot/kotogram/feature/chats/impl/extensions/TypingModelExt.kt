package com.xbot.kotogram.feature.chats.impl.extensions

import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.chats.impl.screen.list.TypingModel

@Composable
internal fun TypingModel.text(): String {
    val first = names.firstOrNull().orEmpty()
    return when {
        inPrivateChat -> stringResource(kind.userText())
        names.size > 1 -> stringResource(kind.multiText(), first, names.size - 1)
        else -> stringResource(kind.chatText(), first)
    }
}
