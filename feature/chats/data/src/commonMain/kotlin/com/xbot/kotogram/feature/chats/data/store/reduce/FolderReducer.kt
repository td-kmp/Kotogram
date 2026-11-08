package com.xbot.kotogram.feature.chats.data.store.reduce

import com.xbot.kotogram.feature.chats.data.store.ChatsState
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateChatFolders
import io.xbot.tdlib.api.UpdateUnreadChatCount

internal object FolderReducer : Reducer {
    override fun reduce(state: ChatsState, update: Update): ChatsState = when (update) {
        is UpdateChatFolders -> state.copy(folders = update)

        is UpdateUnreadChatCount -> state.copy(
            unreadChatCounts = state.unreadChatCounts.putting(update.chatList, update)
        )

        else -> state
    }
}
