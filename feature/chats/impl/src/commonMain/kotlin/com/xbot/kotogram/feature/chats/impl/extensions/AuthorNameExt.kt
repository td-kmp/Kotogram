package com.xbot.kotogram.feature.chats.impl.extensions

import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.chatlist_you
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.chats.impl.screen.list.AuthorName

@Composable
internal fun AuthorName.text(): String = when (this) {
    is Name -> text
    You -> stringResource(Res.string.chatlist_you)
}
