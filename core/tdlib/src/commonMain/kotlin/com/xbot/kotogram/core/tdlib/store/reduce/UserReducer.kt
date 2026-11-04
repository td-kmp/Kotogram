package com.xbot.kotogram.core.tdlib.store.reduce

import com.xbot.kotogram.core.tdlib.store.AccountState
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateUser
import io.xbot.tdlib.api.UpdateUserStatus

internal object UserReducer : Reducer {
    override fun reduce(state: AccountState, update: Update): AccountState = when (update) {
        is UpdateUser -> state.copy(users = state.users.putting(update.user.id, update.user))

        is UpdateUserStatus -> state.users[update.userId]?.let { user ->
            state.copy(users = state.users.putting(user.id, user.copy(status = update.status)))
        } ?: state

        else -> state
    }
}
