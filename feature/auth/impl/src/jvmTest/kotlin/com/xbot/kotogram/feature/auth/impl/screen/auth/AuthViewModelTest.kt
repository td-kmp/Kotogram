package com.xbot.kotogram.feature.auth.impl.screen.auth

import arrow.core.Either
import arrow.core.right
import com.xbot.kotogram.feature.auth.api.AuthStep
import com.xbot.kotogram.feature.auth.domain.LoginEntry
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.LoginFlow
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import io.xbot.tdlib.api.AuthenticationCodeInfo
import io.xbot.tdlib.api.AuthenticationCodeTypeSms
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateWaitCode
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import io.xbot.tdlib.api.AuthorizationStateWaitPassword
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.test.testWithInternalState

class AuthViewModelTest {
    private val repository = FakeAuthRepository()

    @Test
    fun theQrCodeIsTheEntryBeforeTheFirstStep() = runTest {
        assertEquals(listOf(AuthStep.QrCode), backStack(LoginFlow.QrCodeFirst))
    }

    @Test
    fun pastAStepTheEntryIsThePhone() = runTest {
        repository.authorizationState.value = waitCode
        assertEquals(
            listOf(AuthStep.PhoneNumber, AuthStep.Code),
            backStack(LoginFlow.QrCodeFirst)
        )
    }

    @Test
    fun aStepTheUserWentBackFromStaysHidden() = runTest {
        repository.authorizationState.value = waitCode
        assertEquals(
            listOf(AuthStep.PhoneNumber),
            backStack(LoginFlow.PhoneNumberOnly, AuthScreenAction.OnBack)
        )
    }

    @Test
    fun aNewStepShowsAfterGoingBackFromAnother() = runTest {
        repository.authorizationState.value = waitCode
        assertEquals(
            listOf(AuthStep.PhoneNumber, AuthStep.Password),
            backStack(LoginFlow.PhoneNumberOnly, AuthScreenAction.OnBack) {
                repository.authorizationState.value = AuthorizationStateWaitPassword()
            }
        )
    }

    @Test
    fun withoutAStepThereIsOnlyTheEntry() = runTest {
        assertEquals(listOf(AuthStep.PhoneNumber), backStack(LoginFlow.PhoneNumberOnly))
    }

    @Test
    fun leavingTheQrCodeRestartsTheSignIn() = runTest {
        repository.authorizationState.value =
            AuthorizationStateWaitOtherDeviceConfirmation(link = "tg://login")
        backStack(LoginFlow.QrCodeFirst, AuthScreenAction.OnPhoneLoginClick)
        assertEquals(1, repository.restarts)
    }

    private suspend fun TestScope.backStack(
        flow: LoginFlow,
        action: AuthScreenAction? = null,
        afterAction: () -> Unit = {}
    ): List<AuthStep> {
        lateinit var backStack: List<AuthStep>
        AuthViewModel(flow, LoginEntry(flow), repository).testWithInternalState(this) {
            val onCreate = runOnCreate()
            runCurrent()
            action?.let(containerHost::onAction)
            runCurrent()
            afterAction()
            runCurrent()
            backStack = containerHost.container.stateFlow.value.backStack
            onCreate.cancel()
            cancelAndIgnoreRemainingItems()
        }
        return backStack
    }

    private val waitCode = AuthorizationStateWaitCode(
        AuthenticationCodeInfo(
            phoneNumber = "+15550000000",
            type = AuthenticationCodeTypeSms(length = 5),
            nextType = null,
            timeout = 0
        )
    )
}

private class FakeAuthRepository : AuthRepository {
    override val authorizationState =
        MutableStateFlow<AuthorizationState>(AuthorizationStateWaitPhoneNumber)

    var restarts = 0
        private set

    override fun restartSignIn() {
        restarts++
        authorizationState.value = AuthorizationStateWaitPhoneNumber
    }

    override suspend fun setAuthenticationPhoneNumber(phoneNumber: String) = ok()

    override suspend fun requestQrCodeAuthentication() = ok()

    override suspend fun checkAuthenticationCode(code: String) = ok()

    override suspend fun resendAuthenticationCode() = ok()

    override suspend fun setAuthenticationEmailAddress(emailAddress: String) = ok()

    override suspend fun checkAuthenticationEmailCode(code: String) = ok()

    override suspend fun checkAuthenticationPassword(password: String) = ok()

    override suspend fun requestAuthenticationPasswordRecovery() = ok()

    override suspend fun recoverAuthenticationPassword(recoveryCode: String) = ok()

    override suspend fun registerUser(firstName: String, lastName: String) = ok()

    private fun ok(): Either<AuthError, Unit> = Unit.right()
}
