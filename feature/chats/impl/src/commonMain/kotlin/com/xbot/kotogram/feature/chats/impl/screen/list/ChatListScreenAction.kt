package com.xbot.kotogram.feature.chats.impl.screen.list

internal sealed interface ChatListScreenAction {
    data object OnEndReached : ChatListScreenAction
}
