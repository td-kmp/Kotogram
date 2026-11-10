package com.xbot.kotogram.feature.auth.impl.screen.auth

import com.xbot.kotogram.feature.auth.api.AuthStep
import com.xbot.kotogram.feature.auth.domain.LoginEntry
import com.xbot.kotogram.feature.auth.domain.model.LoginFlow
import com.xbot.kotogram.feature.auth.impl.FakeAuthRepository
import io.xbot.tdlib.api.AuthenticationCodeInfo
import io.xbot.tdlib.api.AuthenticationCodeTypeSms
import io.xbot.tdlib.api.AuthorizationStateWaitCode
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import io.xbot.tdlib.api.AuthorizationStateWaitPassword
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.test.testWithInternalState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
            backStack(LoginFlow.QrCodeFirst),
        )
    }

    @Test
    fun aStepTheUserWentBackFromStaysHidden() = runTest {
        repository.authorizationState.value = waitCode
        assertEquals(
            listOf(AuthStep.PhoneNumber),
            backStack(LoginFlow.PhoneNumberOnly, AuthScreenAction.OnBack),
        )
    }

    @Test
    fun aNewStepShowsAfterGoingBackFromAnother() = runTest {
        repository.authorizationState.value = waitCode
        assertEquals(
            listOf(AuthStep.PhoneNumber, AuthStep.Password),
            backStack(LoginFlow.PhoneNumberOnly, AuthScreenAction.OnBack) {
                repository.authorizationState.value = AuthorizationStateWaitPassword()
            },
        )
    }

    @Test
    fun withoutAStepThereIsOnlyTheEntry() = runTest {
        assertEquals(listOf(AuthStep.PhoneNumber), backStack(LoginFlow.PhoneNumberOnly))
    }

    @Test
    fun leavingTheQrCodeKeepsItsSignIn() = runTest {
        repository.authorizationState.value =
            AuthorizationStateWaitOtherDeviceConfirmation(link = "tg://login")
        assertEquals(
            listOf(AuthStep.PhoneNumber),
            backStack(LoginFlow.QrCodeFirst, AuthScreenAction.OnPhoneLoginClick),
        )
        assertEquals(0, repository.restarts)
    }

    @Test
    fun theQrCodePastAStepRestartsTheSignIn() = runTest {
        repository.authorizationState.value = waitCode
        assertEquals(
            listOf(AuthStep.QrCode),
            backStack(LoginFlow.QrCodeFirst, AuthScreenAction.OnQrLoginClick),
        )
        assertEquals(1, repository.restarts)
    }

    @Test
    fun theFirstStepStaysUntilTheFlowMovesOn() = runTest {
        assertFalse(state(LoginFlow.QrCodeFirst).hasLeftFirstStep)
    }

    @Test
    fun leavingTheFirstStepIsForGood() = runTest {
        assertTrue(
            state(LoginFlow.QrCodeFirst, AuthScreenAction.OnPhoneLoginClick) {
                repository.authorizationState.value = AuthorizationStateWaitPhoneNumber
            }.hasLeftFirstStep,
        )
    }

    @Test
    fun theFirstStepIsTdlibsNotTheEntrysGuess() = runTest {
        repository.authorizationState.value = waitCode
        assertFalse(state(LoginFlow.QrCodeFirst).hasLeftFirstStep)
    }

    private suspend fun TestScope.backStack(
        flow: LoginFlow,
        action: AuthScreenAction? = null,
        afterAction: () -> Unit = {},
    ): List<AuthStep> = state(flow, action, afterAction).backStack

    private suspend fun TestScope.state(
        flow: LoginFlow,
        action: AuthScreenAction? = null,
        afterAction: () -> Unit = {},
    ): AuthScreenState {
        lateinit var state: AuthScreenState
        AuthViewModel(flow, LoginEntry(flow), repository).testWithInternalState(this) {
            val onCreate = runOnCreate()
            runCurrent()
            action?.let(containerHost::onAction)
            runCurrent()
            afterAction()
            runCurrent()
            state = containerHost.container.stateFlow.value
            onCreate.cancel()
            cancelAndIgnoreRemainingItems()
        }
        return state
    }

    private val waitCode = AuthorizationStateWaitCode(
        AuthenticationCodeInfo(
            phoneNumber = "+15550000000",
            type = AuthenticationCodeTypeSms(length = 5),
            nextType = null,
            timeout = 0,
        ),
    )
}
