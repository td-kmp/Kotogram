package com.xbot.kotogram.core.account

import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateReady
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
import io.xbot.tdlib.api.AuthorizationStateWaitTdlibParameters
import io.xbot.tdlib.api.OptionValue
import io.xbot.tdlib.api.OptionValueEmpty
import io.xbot.tdlib.api.OptionValueString
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateAuthorizationState
import io.xbot.tdlib.client.TdClient
import java.nio.file.Files
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.scopedOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class AccountManagerTest {
    private val ready = listOf("authorizationStateReady")

    private fun test(body: suspend () -> Unit) = runTest {
        withContext(Dispatchers.Default) { withTimeout(30.seconds) { body() } }
    }

    private fun registryOf(
        vararg accounts: AccountEntry,
        active: AccountId? = accounts.firstOrNull()?.id
    ) = AccountRegistry(accounts.toList(), active, accounts.maxOfOrNull { it.id.value } ?: 0)

    private fun signedIn(id: Int) = AccountEntry(AccountId(id), userId = 100L + id)

    @Test
    fun aFirstStartOpensAnAccountAtTheLogin() = test {
        val app = TestApp().started()
        val session = app.active { it is AuthorizationStateWaitPhoneNumber }
        assertEquals(AccountId(1), session.id)
        assertEquals(registryOf(AccountEntry(AccountId(1))), app.store.saved)
        assertEquals(
            listOf("1 getOption", "1 setTdlibParameters"),
            app.tdLib.requests.take(2)
        )
    }

    @Test
    fun optionsGoBeforeTheParameters() = test {
        val options = MutableStateFlow(mapOf("a" to OptionValueString("1"), "b" to OptionValueEmpty))
        val app = TestApp(options = FakeOptions(options)).started()
        app.active { it is AuthorizationStateWaitPhoneNumber }
        assertEquals(
            listOf("1 getOption", "1 setOption a", "1 setOption b", "1 setTdlibParameters"),
            app.tdLib.requests.take(4)
        )
    }

    @Test
    fun onlyAChangedOptionIsSentAgain() = test {
        val options = MutableStateFlow(mapOf("a" to OptionValueString("1"), "b" to OptionValueEmpty))
        val app = TestApp(options = FakeOptions(options)).started()
        app.active { it is AuthorizationStateWaitPhoneNumber }

        options.value = mapOf("a" to OptionValueString("2"), "b" to OptionValueEmpty)

        while (app.tdLib.requests.count { it == "1 setOption a" } < 2) delay(10)
        assertEquals(1, app.tdLib.requests.count { it == "1 setOption b" })
    }

    @Test
    fun followersSubscribeBeforeTheFirstRequest() = test {
        val app = TestApp().started()
        val session = app.active { it is AuthorizationStateWaitPhoneNumber }
        val updates = session.koinScope.get<UpdateRecorder>().updates
        while (updates.isEmpty()) delay(10)
        val first = updates.first()
        assertIs<AuthorizationStateWaitTdlibParameters>(
            assertIs<UpdateAuthorizationState>(first).authorizationState
        )
    }

    @Test
    fun aSignInSavesTheUserId() = test {
        val app = TestApp(FakeTdLib(mapOf(1 to ready), mapOf(1 to 77L))).started()
        app.manager.registry.first { it.entry(AccountId(1))?.userId == 77L }
        assertEquals(77L, app.store.saved.entry(AccountId(1))?.userId)
        assertEquals(
            listOf(AccountId(1)),
            app.manager.accounts.first { it.isNotEmpty() }.map { it.id }
        )
    }

    @Test
    fun aNewAccountStaysAboveTheActiveOneUntilItSignsIn() = test {
        val app = TestApp(FakeTdLib(mapOf(1 to ready))).started()
        app.active { it == AuthorizationStateReady }
        app.manager.registry.first { it.signedIn.size == 1 }

        val new = assertNotNull(app.manager.add())
        assertEquals(AccountId(2), new.id)
        val shown = app.manager.shown.first { it.adding != null }
        assertEquals(AccountId(1), shown.active?.id)
        new.authorizationState.first { it is AuthorizationStateWaitPhoneNumber }

        app.tdLib.emitState(app.tdLib.clientOf(2), "authorizationStateReady")

        val after = app.manager.shown.first { it.adding == null }
        assertEquals(AccountId(2), after.active?.id)
        assertEquals(AccountId(2), app.store.saved.active)
    }

    @Test
    fun cancellingAddingRemovesTheNewAccountOnly() = test {
        val app = TestApp(FakeTdLib(mapOf(1 to ready))).started()
        app.active { it == AuthorizationStateReady }
        app.manager.registry.first { it.signedIn.size == 1 }
        val new = assertNotNull(app.manager.add())
        new.authorizationState.first { it is AuthorizationStateWaitPhoneNumber }

        app.manager.cancelAdding()

        app.manager.shown.first { it.adding == null }
        app.manager.registry.first { it.entry(new.id) == null }
        app.awaitDeleted(new.id)
        assertEquals(AccountId(1), app.manager.shown.value.active?.id)
        assertTrue("${app.tdLib.clientOf(2)} logOut" in app.tdLib.requests)
    }

    @Test
    fun loggingOutTheLastAccountShowsTheLoginInANewOne() = test {
        val app = TestApp(FakeTdLib(mapOf(1 to ready))).started()
        val session = app.active { it == AuthorizationStateReady }
        app.manager.registry.first { it.signedIn.size == 1 }
        app.root.resolve("1/db").createDirectories()

        app.manager.logOut(session.id)

        val next = app.manager.shown.first { it.active?.id == AccountId(2) }.active
        next?.authorizationState?.first { it is AuthorizationStateWaitPhoneNumber }
        app.awaitDeleted(session.id)
        assertFalse(app.root.resolve("1").exists())
        assertEquals(listOf(AccountId(2)), app.store.saved.accounts.map { it.id })
    }

    @Test
    fun loggingOutHandsOverToAnotherSignedInAccount() = test {
        val store = InMemoryRegistryStore(registryOf(signedIn(1), signedIn(2)))
        val app = TestApp(FakeTdLib(mapOf(1 to ready, 2 to ready)), store).started()
        val session = app.active { it == AuthorizationStateReady }
        assertEquals(AccountId(1), session.id)

        app.manager.switchTo(AccountId(2))
        val second = app.manager.shown.first { it.active?.id == AccountId(2) }.active
        second?.authorizationState?.first { it == AuthorizationStateReady }
        app.manager.logOut(AccountId(2))

        assertEquals(
            AccountId(1),
            app.manager.shown.first { it.active?.id != AccountId(2) }.active?.id
        )
        app.awaitDeleted(AccountId(2))
    }

    @Test
    fun aLogOutFromAnotherDeviceRemovesTheAccount() = test {
        val store = InMemoryRegistryStore(registryOf(signedIn(1), signedIn(2)))
        val app = TestApp(FakeTdLib(mapOf(1 to ready, 2 to ready)), store).started()
        app.active { it == AuthorizationStateReady }
        val client = app.awaitClient(2)

        app.tdLib.emitState(client, "authorizationStateLoggingOut")
        app.tdLib.emitState(client, "authorizationStateClosed")

        app.manager.registry.first { it.entry(AccountId(2)) == null }
        app.awaitDeleted(AccountId(2))
        assertEquals(AccountId(1), app.manager.shown.value.active?.id)
    }

    @Test
    fun aSessionRevokedWhileTheAppWasClosedRemovesTheAccount() = test {
        val revoked = listOf(
            "authorizationStateLoggingOut",
            "authorizationStateClosing",
            "authorizationStateClosed"
        )
        val store = InMemoryRegistryStore(registryOf(signedIn(1)))
        val app = TestApp(FakeTdLib(mapOf(1 to revoked)), store).started()
        app.awaitDeleted(AccountId(1))
        assertEquals(
            AccountId(2),
            app.manager.shown.first { it.active?.id != AccountId(1) }.active?.id
        )
    }

    @Test
    fun aCloseWithoutALogOutKeepsTheAccountAndItsData() = test {
        val store = InMemoryRegistryStore(registryOf(signedIn(1)))
        val tdLib = FakeTdLib(mapOf(1 to ready), mapOf(1 to signedIn(1).userId!!))
        val app = TestApp(tdLib, store).started()
        val session = app.active { it == AuthorizationStateReady }

        app.tdLib.emitState(app.tdLib.clientOf(1), "authorizationStateClosed")
        session.authorizationState.first { it !is AuthorizationStateReady }
        delay(200)

        assertEquals(listOf(signedIn(1)), app.manager.registry.value.accounts)
        assertTrue(app.keys.deleted.isEmpty())
    }

    @Test
    fun aUserAlreadyOnTheDeviceIsLoggedOutOfTheNewAccount() = test {
        val store = InMemoryRegistryStore(registryOf(signedIn(1)))
        val tdLib = FakeTdLib(mapOf(1 to ready), mapOf(1 to 101L, 2 to 101L))
        val app = TestApp(tdLib, store).started()
        app.active { it == AuthorizationStateReady }
        val new = assertNotNull(app.manager.add())
        new.authorizationState.first { it is AuthorizationStateWaitPhoneNumber }

        tdLib.emitState(tdLib.clientOf(2), "authorizationStateReady")

        app.manager.registry.first { it.entry(new.id) == null }
        assertEquals(AccountId(1), app.manager.shown.first { it.adding == null }.active?.id)
        assertTrue("${tdLib.clientOf(2)} logOut" in tdLib.requests)
        app.awaitDeleted(new.id)
    }

    @Test
    fun aSignInTheAppDidNotLiveToFinishIsDroppedAtStart() = test {
        val store = InMemoryRegistryStore(
            registryOf(signedIn(1), AccountEntry(AccountId(3)), active = AccountId(1))
        )
        val app = TestApp(FakeTdLib(mapOf(1 to ready)), store).started()
        app.active { it == AuthorizationStateReady }
        assertEquals(1, app.tdLib.instancesCreated)
        assertEquals(listOf(AccountId(3)), app.keys.deleted.toList())
        assertNull(app.manager.registry.value.entry(AccountId(3)))
    }

    @Test
    fun theOnlyAccountStillSigningInSurvivesARestart() = test {
        val store = InMemoryRegistryStore(registryOf(AccountEntry(AccountId(4))))
        val app = TestApp(store = store).started()
        assertEquals(AccountId(4), app.active { it is AuthorizationStateWaitPhoneNumber }.id)
        assertTrue(app.keys.deleted.isEmpty())
    }

    @Test
    fun idsNeverRepeat() = test {
        val store = InMemoryRegistryStore(registryOf(signedIn(1), signedIn(3)))
        val app = TestApp(FakeTdLib(mapOf(1 to ready, 3 to ready)), store).started()
        app.active { it == AuthorizationStateReady }
        app.manager.logOut(AccountId(3))
        app.awaitDeleted(AccountId(3))
        assertEquals(AccountId(4), app.manager.add()?.id)
    }

    @Test
    fun onlyThreeAccountsFit() = test {
        val store = InMemoryRegistryStore(registryOf(signedIn(1), signedIn(2), signedIn(3)))
        val app = TestApp(FakeTdLib(mapOf(1 to ready, 2 to ready, 3 to ready)), store).started()
        app.active { it == AuthorizationStateReady }
        assertFalse(app.manager.canAdd())
        assertNull(app.manager.add())
    }

    @Test
    fun switchingToASignInInProgressIsRefused() = test {
        val store = InMemoryRegistryStore(registryOf(signedIn(1)))
        val app = TestApp(FakeTdLib(mapOf(1 to ready)), store).started()
        app.active { it == AuthorizationStateReady }
        val new = assertNotNull(app.manager.add())
        app.manager.switchTo(new.id)
        assertEquals(AccountId(1), app.manager.shown.value.active?.id)
    }

    @Test
    fun restartingTheActiveSignInMovesItToANewAccount() = test {
        val app = TestApp().started()
        val session = app.active { it is AuthorizationStateWaitPhoneNumber }

        app.manager.restartSignIn(session.id)

        val next = app.manager.shown.first { it.active?.id == AccountId(2) }.active
        next?.authorizationState?.first { it is AuthorizationStateWaitPhoneNumber }
        app.awaitDeleted(session.id)
        assertTrue("1 logOut" in app.tdLib.requests)
        assertEquals(listOf(AccountId(2)), app.store.saved.accounts.map { it.id })
    }

    @Test
    fun aRestartShowsTheNewAccountOnlyAtItsFirstScreen() = test {
        val app = TestApp().started()
        val session = app.active { it is AuthorizationStateWaitPhoneNumber }

        app.manager.restartSignIn(session.id)

        val next = app.manager.shown.first { it.active?.id != session.id }.active
        assertIs<AuthorizationStateWaitPhoneNumber>(next?.authorizationState?.value)
    }

    @Test
    fun restartingTheAddingSignInKeepsItAboveTheActiveAccount() = test {
        val store = InMemoryRegistryStore(registryOf(signedIn(1)))
        val app = TestApp(FakeTdLib(mapOf(1 to ready)), store).started()
        app.active { it == AuthorizationStateReady }
        val new = assertNotNull(app.manager.add())
        new.authorizationState.first { it is AuthorizationStateWaitPhoneNumber }

        app.manager.restartSignIn(new.id)

        val shown = app.manager.shown.first { it.adding?.id == AccountId(3) }
        assertEquals(AccountId(1), shown.active?.id)
        app.awaitDeleted(new.id)
    }

    @Test
    fun aRestartAskedTwiceRestartsOnce() = test {
        val app = TestApp().started()
        val session = app.active { it is AuthorizationStateWaitPhoneNumber }
        app.manager.restartSignIn(session.id)
        app.manager.restartSignIn(session.id)
        app.manager.shown.first { it.active?.id == AccountId(2) }
        app.awaitDeleted(session.id)
        delay(200)
        assertEquals(2, app.tdLib.instancesCreated)
    }

    @Test
    fun aScopeOutlivesItsAccountByAMomentForTheScreensStillOnDisplay() = test {
        val app = TestApp(FakeTdLib(mapOf(1 to ready))).started()
        val session = app.active { it == AuthorizationStateReady }
        app.manager.registry.first { it.signedIn.size == 1 }

        app.manager.logOut(session.id)
        app.manager.shown.first { it.active?.id == AccountId(2) }
        app.awaitDeleted(session.id)

        assertFalse(session.koinScope.closed)
        while (!session.koinScope.closed) delay(10)
    }

    @Test
    fun anAccountThatLostItsDatabaseKeySignsInAgain() = test {
        val store = InMemoryRegistryStore(registryOf(signedIn(1)))
        val app = TestApp(
            store = store,
            keys = FakeDatabaseKeys(lost = setOf(AccountId(1)))
        ).started()
        assertEquals(
            AccountId(2),
            app.manager.shown.first { it.active?.id == AccountId(2) }.active?.id
        )
        app.awaitDeleted(AccountId(1))
        assertTrue("1 close" in app.tdLib.requests)
    }

    @Test
    fun aBrokenKeystoreDoesNotOpenNewAccountsInALoop() = test {
        val app = TestApp(keys = FakeDatabaseKeys(lost = setOf(AccountId(1)))).started()
        app.manager.shown.first { it.active != null }
        delay(200)
        assertEquals(1, app.tdLib.instancesCreated)
        assertFalse(app.tdLib.requests.any { "setTdlibParameters" in it })
    }

    @Test
    fun closingAllClosesEveryClientAndKeepsTheAccounts() = test {
        val store = InMemoryRegistryStore(registryOf(signedIn(1), signedIn(2)))
        val app = TestApp(FakeTdLib(mapOf(1 to ready, 2 to ready)), store).started()
        app.active { it == AuthorizationStateReady }
        app.awaitClient(2)

        app.manager.closeAll()

        assertTrue("1 close" in app.tdLib.requests && "2 close" in app.tdLib.requests)
        delay(200)
        assertEquals(2, app.manager.registry.value.accounts.size)
        assertTrue(app.keys.deleted.isEmpty())
    }
}

private class TestApp(
    val tdLib: FakeTdLib = FakeTdLib(),
    val store: InMemoryRegistryStore = InMemoryRegistryStore(),
    val keys: FakeDatabaseKeys = FakeDatabaseKeys(),
    options: TdlibOptions? = null
) {
    val root = Files.createTempDirectory("kotogram-accounts")

    val manager = AccountManager(
        appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        tdLib = tdLib,
        config = TdlibConfig(
            apiId = 1,
            apiHash = "test",
            useTestDc = true,
            systemLanguageCode = "en",
            deviceModel = "test",
            systemVersion = "jvm",
            applicationVersion = "test"
        ),
        store = store,
        storage = DirectoryAccountStorage(root.toString()),
        keys = keys,
        koin = koinApplication {
            modules(
                module {
                    scope(AccountScope) {
                        scopedOf(::UpdateRecorder) { bind<AccountUpdates>() }
                        options?.let { owner -> scoped<TdlibOptions> { owner } }
                    }
                }
            )
        }.koin
    )

    fun started() = apply { manager.start() }

    suspend fun active(state: (AuthorizationState?) -> Boolean): AccountSession {
        val session = checkNotNull(manager.shown.first { it.active != null }.active)
        session.authorizationState.first(state)
        return session
    }

    suspend fun awaitClient(account: Int): Int {
        while (true) {
            runCatching { tdLib.clientOf(account) }.onSuccess { return it }
            delay(10)
        }
    }

    suspend fun awaitDeleted(id: AccountId) {
        while (id !in keys.deleted) delay(10)
    }
}

private class InMemoryRegistryStore(var saved: AccountRegistry = AccountRegistry()) :
    AccountRegistryStore {
    override suspend fun load(): AccountRegistry = saved

    override suspend fun save(registry: AccountRegistry) {
        saved = registry
    }
}

private class FakeDatabaseKeys(private val lost: Set<AccountId> = emptySet()) : DatabaseKeys {
    val deleted = CopyOnWriteArrayList<AccountId>()

    override suspend fun getOrCreate(id: AccountId): ByteArray? =
        if (id in lost) null else ByteArray(KEY_SIZE)

    override suspend fun delete(id: AccountId) {
        deleted += id
    }

    private companion object {
        const val KEY_SIZE = 32
    }
}

private class FakeOptions(override val options: Flow<Map<String, OptionValue>>) : TdlibOptions

private class UpdateRecorder : AccountUpdates {
    val updates = CopyOnWriteArrayList<Update>()

    override fun start(scope: CoroutineScope, client: TdClient) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            client.updates.collect { updates += it }
        }
    }
}
