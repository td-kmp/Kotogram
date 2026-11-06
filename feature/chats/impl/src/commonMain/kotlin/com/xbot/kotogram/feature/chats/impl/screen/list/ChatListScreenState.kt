package com.xbot.kotogram.feature.chats.impl.screen.list

internal data class ChatListScreenState(
    val rows: List<ChatListRow> = emptyList(),
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val loadFailed: Boolean = false
)
