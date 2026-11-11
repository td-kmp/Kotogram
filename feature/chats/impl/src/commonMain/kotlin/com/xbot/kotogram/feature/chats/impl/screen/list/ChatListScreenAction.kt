package com.xbot.kotogram.feature.chats.impl.screen.list

import io.xbot.tdlib.api.ChatList

internal sealed interface ChatListScreenAction {
    data object OnEndReached : ChatListScreenAction

    data class OnFolderSelect(
        val chatList: ChatList,
    ) : ChatListScreenAction
}
