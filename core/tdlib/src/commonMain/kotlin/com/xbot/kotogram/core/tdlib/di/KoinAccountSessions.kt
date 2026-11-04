package com.xbot.kotogram.core.tdlib.di

import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.core.tdlib.account.AccountLifetime
import com.xbot.kotogram.core.tdlib.account.AccountSession
import com.xbot.kotogram.core.tdlib.account.AccountSessions
import com.xbot.kotogram.core.tdlib.client.ManagedClientHolder
import com.xbot.kotogram.core.tdlib.extensions.accountScope
import com.xbot.kotogram.core.tdlib.extensions.directoryName
import com.xbot.kotogram.core.tdlib.store.AccountStore
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped
import org.koin.core.annotation.Singleton
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope as KoinScope
import org.koin.core.scope.ScopeCallback

/** A session is a Koin scope: its source is the id, and everything account-bound is scoped to it. */
@Singleton
internal class KoinAccountSessions(rootScope: KoinScope) : AccountSessions {
    private val koin = rootScope.getKoin()

    override fun open(id: AccountId): AccountSession =
        KoinAccountSession(id, koin.createScope(id.directoryName, named<AccountId>(), source = id))

    override fun get(id: AccountId): AccountSession? =
        koin.accountScope(id)?.let { KoinAccountSession(id, it) }
}

private class KoinAccountSession(override val id: AccountId, private val scope: KoinScope) :
    AccountSession {
    // Resolving the client is what starts the account's TDLib instance.
    override val client: ManagedClientHolder = scope.get()
    override val store: AccountStore = scope.get()

    override fun close() = scope.close()
}

@Scope(AccountId::class)
@Scoped
internal class KoinAccountLifetime(private val scope: KoinScope) : AccountLifetime {
    override fun addCloseable(closeable: AutoCloseable) {
        scope.registerCallback(
            object : ScopeCallback {
                override fun onScopeClose(scope: KoinScope) = closeable.close()
            }
        )
    }
}
