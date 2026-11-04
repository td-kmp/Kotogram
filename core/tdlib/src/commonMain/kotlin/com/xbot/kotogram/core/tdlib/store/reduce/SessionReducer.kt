package com.xbot.kotogram.core.tdlib.store.reduce

import com.xbot.kotogram.core.tdlib.store.AccountState
import io.xbot.tdlib.api.AuthorizationStateClosed
import io.xbot.tdlib.api.OptionValueEmpty
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateAuthorizationState
import io.xbot.tdlib.api.UpdateConnectionState
import io.xbot.tdlib.api.UpdateOption

/** What belongs to a TDLib instance rather than to an entity. */
internal object SessionReducer : Reducer {
    override fun reduce(state: AccountState, update: Update): AccountState = when (update) {
        // A new instance tells the app everything again; a real log out ends the whole store anyway.
        is UpdateAuthorizationState ->
            if (update.authorizationState is AuthorizationStateClosed) AccountState.Empty else state

        is UpdateOption -> state.copy(
            options = if (update.value is OptionValueEmpty) {
                state.options.removing(update.name)
            } else {
                state.options.putting(update.name, update.value)
            }
        )

        is UpdateConnectionState -> state.copy(connectionState = update.state)

        else -> state
    }
}
