package com.xbot.kotogram.feature.chats.presentation.screen.list

internal data class ChatListScreenState(
    val rows: List<ChatListRow> = emptyList(),
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val loadFailed: Boolean = false
)

internal sealed interface ChatListScreenAction {
    /** The list is scrolled near its end, or empty: time for the next page. */
    data object OnEndReached : ChatListScreenAction
}
