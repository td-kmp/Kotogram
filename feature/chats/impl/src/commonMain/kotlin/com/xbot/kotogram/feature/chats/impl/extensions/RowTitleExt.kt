package com.xbot.kotogram.feature.chats.impl.extensions

import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.peer_deleteduser
import com.xbot.kotogram.core.resources.peer_repliesnotifications
import com.xbot.kotogram.core.resources.peer_savedmessages
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.chats.impl.screen.list.RowTitle

@Composable
internal fun RowTitle.text(): String = when (this) {
    is RowTitle.Name -> text
    RowTitle.SavedMessages -> stringResource(Res.string.peer_savedmessages)
    RowTitle.Replies -> stringResource(Res.string.peer_repliesnotifications)
    RowTitle.DeletedAccount -> stringResource(Res.string.peer_deleteduser)
}
