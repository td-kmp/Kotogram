package com.xbot.kotogram.core.tdlib

import arrow.core.Either
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.core.domain.model.AuthorizationState
import com.xbot.kotogram.core.domain.repository.AccountRepository
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
import io.xbot.tdlib.api.GetOption
import io.xbot.tdlib.api.LogOut
import io.xbot.tdlib.client.TdException
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.koin.core.qualifier.named

class AccountLifecycleTest {
    private val ready = listOf("authorizationStateReady")

    private fun test(body: suspend () -> Unit) = runTest {
        withContext(Dispatchers.Default) { withTimeout(30.seconds) { body() } }
    }

    private fun holderIn(tdLib: FakeTdLib): Pair<ClientHolder, AccountStore> {
        val scope = testKoin(
            tdLib = tdLib
        ).koin.createScope("account-1", named<AccountId>(), source = AccountId(1))
        return scope.get<ClientHolder>() to scope.get<AccountStore>()
    }

    @Test
    fun nothingIsLostBeforeTheFirstSend() = test {
        // The burst comes straight after the first request, before anything else could subscribe.
        val option = """{"@type":"updateOption","name":"my_id",""" +
            """"value":{"@type":"optionValueInteger","value":"7"}}"""
        val tdLib = FakeTdLib(mapOf("account-1" to listOf(option, "authorizationStateReady")))
        val (_, store) = holderIn(tdLib)
        assertEquals(7L, store.state.first { it.myId != null }.myId)
        assertEquals("1 setTdlibParameters", tdLib.requests.first())
    }

    @Test
    fun leavingALoginStepRestartsTheInstance() = test {
        val tdLib = FakeTdLib()
        val (holder, _) = holderIn(tdLib)
        holder.authorizationState.first { it is AuthorizationStateWaitPhoneNumber }
        holder.send(LogOut)
        withTimeout(10.seconds) { while (tdLib.instancesCreated < 2) delay(10) }
        holder.authorizationState.first { it is AuthorizationStateWaitPhoneNumber }
        assertEquals(AccountLifecycle.Live, holder.lifecycle.value)
    }

    @Test
    fun aRealLogOutEndsTheAccountWithoutANewInstance() = test {
        val tdLib = FakeTdLib(mapOf("account-1" to ready))
        val (holder, store) = holderIn(tdLib)
        holder.authorizationState.first { it != null }
        holder.send(LogOut)
        holder.lifecycle.first { it == AccountLifecycle.Ended }
        assertEquals(1, tdLib.instancesCreated)
        assertEquals(null, store.state.value.myId)
        assertEquals(
            500,
            assertFailsWith<TdException> {
                holder.send(GetOption(name = "version"))
            }.code
        )
    }

    @Test
    fun aSessionRevokedWhileClosedEndsTheAccount() = test {
        val tdLib = FakeTdLib(
            mapOf(
                "account-1" to listOf(
                    "authorizationStateLoggingOut",
                    "authorizationStateClosing",
                    "authorizationStateClosed"
                )
            )
        )
        val (holder, _) = holderIn(tdLib)
        holder.lifecycle.first { it == AccountLifecycle.Ended }
        assertEquals(1, tdLib.instancesCreated)
    }

    @Test
    fun anUnexplainedCloseStartsANewInstanceAndKeepsTheAccount() = test {
        val tdLib = FakeTdLib(mapOf("account-1" to ready))
        val (holder, _) = holderIn(tdLib)
        holder.authorizationState.first { it != null }
        tdLib.emitState(1, "authorizationStateClosed")
        withTimeout(10.seconds) { while (tdLib.instancesCreated < 2) delay(10) }
        assertEquals(AccountLifecycle.Live, holder.lifecycle.value)
    }

    @Test
    fun destroyEndsAnUnauthorizedAccount() = test {
        val tdLib = FakeTdLib()
        val (holder, _) = holderIn(tdLib)
        holder.authorizationState.first { it is AuthorizationStateWaitPhoneNumber }
        holder.destroy()
        holder.lifecycle.first { it == AccountLifecycle.Ended }
        assertEquals(1, tdLib.instancesCreated)
    }

    @Test
    fun shutdownClosesTheInstanceAndStartsNoOther() = test {
        val tdLib = FakeTdLib()
        val (holder, _) = holderIn(tdLib)
        holder.authorizationState.first { it is AuthorizationStateWaitPhoneNumber }
        assertTrue(holder.shutdown())
        assertTrue("1 close" in tdLib.requests)
        delay(200)
        assertEquals(1, tdLib.instancesCreated)
    }

    @Test
    fun aShutdownTDLibDoesNotConfirmIsReported() = test {
        val tdLib = FakeTdLib()
        val (holder, _) = holderIn(tdLib)
        holder.authorizationState.first { it is AuthorizationStateWaitPhoneNumber }
        tdLib.isStuck = true
        assertFalse(holder.shutdown())
    }

    @Test
    fun loggingOutTheLastAccountEndsItAndANewOneShowsTheLogin() = test {
        val root = Files.createTempDirectory("kotogram-tdlib")
        root.resolve("db/account-1").createDirectories()
        val tdLib = FakeTdLib(mapOf("account-1" to ready))
        val app = testKoin(root, tdLib)
        val accounts = app.koin.get<AccountRepository>()
        accounts.active.first { it?.authorizationState == AuthorizationState.Ready }

        app.koin.accountScope(AccountId(1))!!.get<ClientHolder>().send(LogOut)

        val next = accounts.active.first {
            it?.id == AccountId(2) && it.authorizationState == AuthorizationState.WaitPhoneNumber
        }
        assertEquals(AccountId(2), next?.id)
        while (root.resolve("db/account-1").exists()) delay(20)
        assertEquals(listOf(AccountId(2)), accounts.accounts.first { it.size == 1 }.map { it.id })
        assertEquals(null, app.koin.accountScope(AccountId(1)))
    }

    @Test
    fun loggingOutHandsOverToThePreviousAccount() = test {
        val root = Files.createTempDirectory("kotogram-tdlib")
        (1..3).forEach { root.resolve("db/account-$it").createDirectories() }
        val tdLib =
            FakeTdLib(mapOf("account-1" to ready, "account-2" to ready, "account-3" to ready))
        val app = testKoin(root, tdLib)
        val accounts = app.koin.get<AccountRepository>()
        accounts.accounts.first { list ->
            list.size == 3 && list.all { it.authorizationState == AuthorizationState.Ready }
        }

        assertEquals(Either.Right(Unit), accounts.switchTo(AccountId(3)))
        assertEquals(Either.Right(Unit), accounts.switchTo(AccountId(2)))
        accounts.active.first { it?.id == AccountId(2) }

        app.koin.accountScope(AccountId(2))!!.get<ClientHolder>().send(LogOut)

        assertEquals(AccountId(3), accounts.active.first { it?.id != AccountId(2) }?.id)
        while (root.resolve("db/account-2").exists()) delay(20)
    }

    @Test
    fun anAbandonedLoginIsDestroyedAtAnyStep() = test {
        val root = Files.createTempDirectory("kotogram-tdlib")
        root.resolve("db/account-1").createDirectories()
        root.resolve("db/account-2").createDirectories()
        val tdLib = FakeTdLib(
            mapOf(
                "account-1" to ready,
                "account-2" to listOf("authorizationStateWaitPassword")
            )
        )
        val app = testKoin(root, tdLib)
        val accounts = app.koin.get<AccountRepository>()
        accounts.accounts.first { list -> list.map { it.id } == listOf(AccountId(1)) }
        while (root.resolve("db/account-2").exists()) delay(20)
        assertEquals(AccountId(1), accounts.active.value?.id)
    }

    @Test
    fun idsAreNeverReused() = test {
        val root = Files.createTempDirectory("kotogram-tdlib")
        root.resolve("db/account-1").createDirectories()
        val tdLib = FakeTdLib(mapOf("account-1" to ready))
        val app = testKoin(root, tdLib)
        val accounts = app.koin.get<AccountRepository>()
        accounts.active.first { it?.authorizationState == AuthorizationState.Ready }

        assertEquals(Either.Right(AccountId(2)), accounts.addAccount())
        accounts.active.first { it?.id == AccountId(2) }
        assertEquals(Either.Right(Unit), accounts.switchTo(AccountId(1)))
        while (root.resolve("db/account-2").exists()) delay(20)

        assertEquals(Either.Right(AccountId(3)), accounts.addAccount())
    }

    @Test
    fun aScopeStaysOpenWhileItsAccountIsShown() = test {
        val root = Files.createTempDirectory("kotogram-tdlib")
        root.resolve("db/account-1").createDirectories()
        val tdLib = FakeTdLib(mapOf("account-1" to ready))
        val app = testKoin(root, tdLib)
        val accounts = app.koin.get<AccountRepository>()
        accounts.active.first { it?.authorizationState == AuthorizationState.Ready }
        accounts.onAccountShown(AccountId(1))

        app.koin.accountScope(AccountId(1))!!.get<ClientHolder>().send(LogOut)
        accounts.active.first { it?.id == AccountId(2) }
        delay(300)
        assertTrue(app.koin.accountScope(AccountId(1)) != null)

        accounts.onAccountShown(AccountId(2))
        while (app.koin.accountScope(AccountId(1)) != null) delay(20)
    }
}
