package com.xbot.kotogram.core.data

import com.xbot.kotogram.core.data.client.ClientHolder
import com.xbot.kotogram.core.domain.model.AccountId
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
import io.xbot.tdlib.api.GetOption
import io.xbot.tdlib.api.LogOut
import io.xbot.tdlib.api.OptionValueString
import io.xbot.tdlib.api.RequestQrCodeAuthentication
import io.xbot.tdlib.client.TdException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.koin.core.qualifier.named

class ClientHolderTest {
    private fun test(body: suspend CoroutineScope.() -> Unit) = runTest {
        withContext(Dispatchers.Default) { withTimeout(90.seconds) { coroutineScope { body() } } }
    }

    @Test
    fun startsEagerlyRestartsAfterClosedAndClosesWithItsScope() = test {
        val koin = testKoin().koin
        val scope = koin.createScope("account-1", named<AccountId>(), source = AccountId(1))
        val holder = scope.get<ClientHolder>()

        // Nothing was sent by the test: the parameters went first, eagerly.
        holder.authorizationState.first { it is AuthorizationStateWaitPhoneNumber }

        // Leaving QR login takes a log out; the holder starts a new instance on the same database.
        holder.send(RequestQrCodeAuthentication())
        holder.authorizationState.first { it is AuthorizationStateWaitOtherDeviceConfirmation }
        val states = mutableListOf<AuthorizationState?>()
        val recording = launch(start = CoroutineStart.UNDISPATCHED) {
            holder.authorizationState.toList(states)
        }
        holder.send(LogOut)
        holder.authorizationState.first { it is AuthorizationStateWaitPhoneNumber }
        recording.cancel()
        // The UI routes on these states: the restart must not look like an unstarted account.
        assertFalse(null in states, "States during the restart: $states")
        assertIs<OptionValueString>(holder.send(GetOption(name = "version")))

        scope.close()
        assertFailsWith<TdException> { holder.send(GetOption(name = "version")) }
    }
}
