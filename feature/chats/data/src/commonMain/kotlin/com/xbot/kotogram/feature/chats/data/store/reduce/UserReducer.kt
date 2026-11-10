package com.xbot.kotogram.feature.chats.data.store.reduce

import com.xbot.kotogram.feature.chats.data.store.ChatsState
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateUser
import io.xbot.tdlib.api.UpdateUserStatus

internal object UserReducer : Reducer {
    override fun reduce(
        state: ChatsState,
        update: Update,
    ): ChatsState = when (update) {
        is UpdateUser -> state.copy(users = state.users.putting(update.user.id, update.user))

        is UpdateUserStatus -> state.users[update.userId]?.let { user ->
            state.copy(users = state.users.putting(user.id, user.copy(status = update.status)))
        } ?: state

        else -> state
    }
}
