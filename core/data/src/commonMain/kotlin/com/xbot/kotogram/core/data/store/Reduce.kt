package com.xbot.kotogram.core.data.store

import io.xbot.tdlib.api.AuthorizationStateClosed
import io.xbot.tdlib.api.OptionValueEmpty
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateAuthorizationState
import io.xbot.tdlib.api.UpdateBasicGroup
import io.xbot.tdlib.api.UpdateConnectionState
import io.xbot.tdlib.api.UpdateOption
import io.xbot.tdlib.api.UpdateSecretChat
import io.xbot.tdlib.api.UpdateSupergroup
import io.xbot.tdlib.api.UpdateUser
import io.xbot.tdlib.api.UpdateUserStatus

/** Pure, and returns `this` when nothing changed. UpdateCoverageTest fails when TDLib adds an update type. */
internal fun AccountState.reduce(update: Update): AccountState = when (update) {
    // A new instance tells the app everything again; a real log out ends the whole store anyway.
    is UpdateAuthorizationState ->
        if (update.authorizationState is AuthorizationStateClosed) AccountState.Empty else this

    is UpdateOption -> copy(
        options = if (update.value is OptionValueEmpty) {
            options.removing(update.name)
        } else {
            options.putting(update.name, update.value)
        }
    )

    is UpdateConnectionState -> copy(connectionState = update.state)

    is UpdateUser -> copy(users = users.putting(update.user.id, update.user))

    is UpdateUserStatus -> users[update.userId]?.let { user ->
        copy(users = users.putting(user.id, user.copy(status = update.status)))
    } ?: this

    is UpdateBasicGroup -> copy(
        basicGroups = basicGroups.putting(update.basicGroup.id, update.basicGroup)
    )

    is UpdateSupergroup -> copy(
        supergroups = supergroups.putting(update.supergroup.id, update.supergroup)
    )

    is UpdateSecretChat -> copy(
        secretChats = secretChats.putting(update.secretChat.id, update.secretChat)
    )

    else -> reduceChat(update)
}
