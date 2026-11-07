package com.xbot.kotogram.feature.auth.impl.screen.auth

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.api.AuthStep
import com.xbot.kotogram.feature.auth.domain.LoginEntry
import com.xbot.kotogram.feature.auth.domain.model.LoginFlow
import com.xbot.kotogram.feature.auth.domain.model.LoginMethod
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.auth.impl.extensions.toAuthStep
import com.xbot.kotogram.feature.language.domain.repository.LanguageRepository
import com.xbot.kotogram.feature.language.domain.repository.LanguageSuggestionRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class AuthViewModel(
    private val flow: LoginFlow,
    private val loginEntry: LoginEntry,
    private val authRepository: AuthRepository,
    private val languageSuggestionRepository: LanguageSuggestionRepository,
    private val languageRepository: LanguageRepository
) : ViewModel(),
    OrbitContainerHost<AuthScreenState, AuthScreenState, Nothing> {

    private val dismissedStep = MutableStateFlow<AuthStep?>(null)

    override val container = orbitContainer<AuthScreenState, Nothing>(
        AuthScreenState(
            flow = flow,
            backStack = backStack(loginEntry.method.value, step = null, dismissed = null)
        )
    ) {
        coroutineScope {
            launch {
                combine(loginEntry.method, authRepository.authorizationState, dismissedStep, ::Triple)
                    .collect { (method, authorizationState, dismissed) ->
                        val step = authorizationState.toAuthStep()
                        if (dismissed != null && step != dismissed) dismissedStep.value = null
                        reduce { state.copy(backStack = backStack(method, step, dismissed)) }
                    }
            }
            launch {
                // As Telegram for macOS: only until the device's first account is signed in.
                combine(languageSuggestionRepository.suggestion, authRepository.isFirstSignIn) { suggestion, isFirst ->
                    suggestion.takeIf { isFirst }
                }.collect { suggestion -> reduce { state.copy(languageSuggestion = suggestion) } }
            }
        }
    }

    fun onAction(action: AuthScreenAction) {
        when (action) {
            AuthScreenAction.OnBack -> onBack()
            AuthScreenAction.OnEntrySubmitted -> dismissedStep.value = null
            AuthScreenAction.OnPhoneLoginClick -> loginEntry.method.value = LoginMethod.PhoneNumber
            AuthScreenAction.OnQrLoginClick -> onQrLoginClick()
            AuthScreenAction.OnContinueOnLanguageClick -> onContinueOnLanguageClick()
        }
    }

    private fun onBack() = intent {
        if (state.backStack.size > 1) dismissedStep.value = state.backStack.last()
    }

    private fun onQrLoginClick() = intent {
        if (flow != LoginFlow.QrCodeFirst) return@intent
        loginEntry.method.value = LoginMethod.QrCode
        // As Unigram does: past a step the sign-in starts over, or the server answers AUTH_TOKEN_ALREADY_ACCEPTED.
        if (authRepository.authorizationState.first().toAuthStep() != null) {
            authRepository.restartSignIn()
        }
    }

    private fun onContinueOnLanguageClick() = intent {
        val suggestion = state.languageSuggestion ?: return@intent
        if (state.isSwitchingLanguage) return@intent
        reduce { state.copy(isSwitchingLanguage = true) }
        languageRepository.apply(suggestion.languagePackId)
        reduce { state.copy(isSwitchingLanguage = false) }
    }

    private fun backStack(
        method: LoginMethod,
        step: AuthStep?,
        dismissed: AuthStep?
    ): List<AuthStep> {
        // TDLib shows a QR code only before the first step, so past it the entry is the phone.
        val entry = if (method == LoginMethod.QrCode && step == null) {
            AuthStep.QrCode
        } else {
            AuthStep.PhoneNumber
        }
        return listOfNotNull(entry, step?.takeUnless { it == dismissed })
    }
}
