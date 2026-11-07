package com.xbot.kotogram.feature.auth.impl.screen.auth

import com.xbot.kotogram.feature.auth.api.AuthStep
import com.xbot.kotogram.feature.auth.domain.LoginEntry
import com.xbot.kotogram.feature.auth.domain.model.LoginFlow
import com.xbot.kotogram.feature.auth.impl.FakeAuthRepository
import com.xbot.kotogram.feature.auth.impl.FakeLanguageRepository
import com.xbot.kotogram.feature.auth.impl.FakeLanguageSuggestionRepository
import com.xbot.kotogram.feature.language.domain.model.LanguageSuggestion
import io.xbot.tdlib.api.AuthenticationCodeInfo
import io.xbot.tdlib.api.AuthenticationCodeTypeSms
import io.xbot.tdlib.api.AuthorizationStateWaitCode
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import io.xbot.tdlib.api.AuthorizationStateWaitPassword
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.test.testWithInternalState

class AuthViewModelTest {
    private val repository = FakeAuthRepository()
    private val languages = FakeLanguageRepository()
    private val suggestions = FakeLanguageSuggestionRepository()
    private val russian = LanguageSuggestion(languagePackId = "ru", text = "Продолжить на русском")

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
    fun leavingTheQrCodeKeepsItsSignIn() = runTest {
        repository.authorizationState.value =
            AuthorizationStateWaitOtherDeviceConfirmation(link = "tg://login")
        assertEquals(
            listOf(AuthStep.PhoneNumber),
            backStack(LoginFlow.QrCodeFirst, AuthScreenAction.OnPhoneLoginClick)
        )
        assertEquals(0, repository.restarts)
    }

    @Test
    fun theQrCodePastAStepRestartsTheSignIn() = runTest {
        repository.authorizationState.value = waitCode
        assertEquals(
            listOf(AuthStep.QrCode),
            backStack(LoginFlow.QrCodeFirst, AuthScreenAction.OnQrLoginClick)
        )
        assertEquals(1, repository.restarts)
    }

    @Test
    fun theServersLanguageIsOfferedAtTheFirstSignIn() = runTest {
        suggestions.suggestion.value = russian
        assertEquals(russian, state(LoginFlow.QrCodeFirst).languageSuggestion)
    }

    @Test
    fun noLanguageIsOfferedWhenAnotherAccountIsSignedIn() = runTest {
        suggestions.suggestion.value = russian
        repository.isFirstSignIn.value = false
        assertNull(state(LoginFlow.QrCodeFirst).languageSuggestion)
    }

    @Test
    fun continuingSwitchesToTheOfferedLanguage() = runTest {
        suggestions.suggestion.value = russian
        languages.switched = CompletableDeferred()
        val switching = state(LoginFlow.QrCodeFirst, AuthScreenAction.OnContinueOnLanguageClick)
        assertTrue(switching.isSwitchingLanguage)
        assertEquals(listOf("ru"), languages.applied)
    }

    private suspend fun TestScope.backStack(
        flow: LoginFlow,
        action: AuthScreenAction? = null,
        afterAction: () -> Unit = {}
    ): List<AuthStep> = state(flow, action, afterAction).backStack

    private suspend fun TestScope.state(
        flow: LoginFlow,
        action: AuthScreenAction? = null,
        afterAction: () -> Unit = {}
    ): AuthScreenState {
        lateinit var state: AuthScreenState
        AuthViewModel(flow, LoginEntry(flow), repository, suggestions, languages).testWithInternalState(this) {
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
            timeout = 0
        )
    )
}
