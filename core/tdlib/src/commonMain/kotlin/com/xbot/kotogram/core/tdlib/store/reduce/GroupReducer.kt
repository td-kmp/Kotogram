package com.xbot.kotogram.core.tdlib.store.reduce

import com.xbot.kotogram.core.tdlib.store.AccountState
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateBasicGroup
import io.xbot.tdlib.api.UpdateSecretChat
import io.xbot.tdlib.api.UpdateSupergroup

internal object GroupReducer : Reducer {
    override fun reduce(state: AccountState, update: Update): AccountState = when (update) {
        is UpdateBasicGroup -> state.copy(
            basicGroups = state.basicGroups.putting(update.basicGroup.id, update.basicGroup)
        )

        is UpdateSupergroup -> state.copy(
            supergroups = state.supergroups.putting(update.supergroup.id, update.supergroup)
        )

        is UpdateSecretChat -> state.copy(
            secretChats = state.secretChats.putting(update.secretChat.id, update.secretChat)
        )

        else -> state
    }
}
