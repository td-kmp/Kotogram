package com.xbot.kotogram.feature.chats.impl.screen.list

import com.xbot.kotogram.feature.chats.domain.model.ChatListEntry
import com.xbot.kotogram.feature.chats.impl.component.MemoizingRowMapper
import com.xbot.kotogram.feature.chats.impl.component.RowMapper
import com.xbot.kotogram.feature.chats.impl.extensions.hasSameInputsAs
import com.xbot.kotogram.feature.chats.impl.extensions.toRow

internal class ChatListRowMapper :
    RowMapper<ChatListEntry, ChatListRow> by MemoizingRowMapper<ChatListEntry, Long, ChatListRow>(
        key = { it.chat.id },
        hasSameInputs = ChatListEntry::hasSameInputsAs,
        toRow = ChatListEntry::toRow,
    )
