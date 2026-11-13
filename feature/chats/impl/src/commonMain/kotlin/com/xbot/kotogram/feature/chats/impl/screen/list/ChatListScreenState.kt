package com.xbot.kotogram.feature.chats.impl.screen.list

import com.xbot.kotogram.feature.chats.domain.model.ArchiveSummary
import com.xbot.kotogram.feature.chats.domain.model.ChatFolderTab
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.ChatListMain
import io.xbot.tdlib.api.ConnectionState
import io.xbot.tdlib.api.Sticker

internal data class ChatListScreenState(
    val tabs: List<ChatFolderTab> = [],
    val selected: ChatList = ChatListMain,
    val rows: List<ChatListRow> = [],
    val archive: ArchiveSummary? = null,
    val customEmoji: Map<Long, Sticker> = emptyMap(),
    val connection: ConnectionState? = null,
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val loadFailed: Boolean = false,
)
