package com.xbot.kotogram.core.data

import arrow.core.Either
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.core.domain.repository.AccountError
import com.xbot.kotogram.core.domain.repository.AccountRepository
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class DefaultAccountRepositoryTest {
    private fun test(body: suspend () -> Unit) = runTest {
        withContext(Dispatchers.Default) { withTimeout(90.seconds) { body() } }
    }

    @Test
    fun firstLaunchOpensAccountOne() = test {
        val app = testKoin()
        val accounts = app.koin.get<AccountRepository>()
        val active = accounts.active.first {
            it?.authorizationState ==
                AuthorizationStateWaitPhoneNumber
        }
        assertEquals(AccountId(1), active?.id)
        assertEquals(listOf(AccountId(1)), accounts.accounts.value.map { it.id })
        app.close()
    }

    @Test
    fun aNonActiveAccountWaitingForLoginIsRemoved() = test {
        val root = Files.createTempDirectory("kotogram-tdlib")
        // Account 1 opens as the active one; account 2 is a leftover from an earlier run that never logged in.
        root.resolve("db/account-1").createDirectories()
        root.resolve("db/account-2").createDirectories()
        val app = testKoin(root)
        val accounts = app.koin.get<AccountRepository>()

        accounts.accounts.first { list -> list.map { it.id } == listOf(AccountId(1)) }
        // The account leaves the list at once; its TDLib instance closes and its data is deleted after.
        while (root.resolve("db/account-2").exists()) delay(50)
        assertEquals(AccountId(1), accounts.active.value?.id)
        app.close()
    }

    @Test
    fun addAccountMakesItActiveAndSwitchingNeedsAnAuthorizedAccount() = test {
        val app = testKoin()
        val accounts = app.koin.get<AccountRepository>()
        accounts.active.first { it != null }

        assertEquals(Either.Right(AccountId(2)), accounts.addAccount())
        assertEquals(AccountId(2), accounts.active.first { it?.id == AccountId(2) }?.id)
        assertEquals(Either.Left(AccountError.NotAuthorized), accounts.switchTo(AccountId(2)))
        assertEquals(Either.Left(AccountError.UnknownAccount), accounts.switchTo(AccountId(9)))
        app.close()
    }
}
