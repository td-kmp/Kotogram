package com.xbot.kotogram.core.account

import com.xbot.kotogram.core.account.extensions.sendCatching
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.LogOut
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import kotlin.concurrent.Volatile

val AccountScope: Qualifier = named("account")

interface AccountUpdates {
    // Workaround: TdClient.updates has no replay, so launch UNDISPATCHED to be subscribed on return.
    fun start(
        scope: CoroutineScope,
        client: TdClient,
    )
}

class AccountContext(
    val id: AccountId,
    val client: TdClient,
    val scope: CoroutineScope,
)

class AccountSession internal constructor(
    val id: AccountId,
    val koinScope: Scope,
    internal val client: TdClient,
    private val coroutines: CoroutineScope,
) {
    val authorizationState: StateFlow<AuthorizationState?> get() = client.authorizationState

    @Volatile
    internal var closingByApp = false

    internal suspend fun logOut() {
        client.sendCatching(LogOut)
    }

    internal fun cancelWork() {
        coroutines.cancel()
    }
}
