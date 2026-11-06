package com.xbot.kotogram.feature.auth.impl.screen.auth

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.domain.LoginEntry
import com.xbot.kotogram.feature.auth.domain.model.LoginFlow
import com.xbot.kotogram.feature.auth.domain.model.LoginMethod
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.auth.impl.extensions.toAuthStep
import com.xbot.kotogram.feature.auth.impl.navigation.AuthStep
import com.xbot.kotogram.feature.auth.impl.navigation.authBackStack
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class AuthViewModel(
    private val flow: LoginFlow,
    private val loginEntry: LoginEntry,
    private val authRepository: AuthRepository
) : ViewModel(),
    OrbitContainerHost<AuthScreenState, AuthScreenState, Nothing> {

    private val dismissedStep = MutableStateFlow<AuthStep?>(null)

    override val container = orbitContainer<AuthScreenState, Nothing>(
        AuthScreenState(
            flow = flow,
            backStack = authBackStack(loginEntry.method.value, step = null, dismissed = null)
        )
    ) {
        combine(loginEntry.method, authRepository.authorizationState, dismissedStep, ::Triple)
            .collect { (method, authorizationState, dismissed) ->
                // Here and not on the click: a QR request already on its way lands in this state too.
                if (
                    method == LoginMethod.PhoneNumber &&
                    authorizationState is AuthorizationStateWaitOtherDeviceConfirmation
                ) {
                    authRepository.restartSignIn()
                    return@collect
                }
                val step = authorizationState.toAuthStep()
                if (dismissed != null && step != dismissed) dismissedStep.value = null
                reduce { state.copy(backStack = authBackStack(method, step, dismissed)) }
            }
    }

    fun onAction(action: AuthScreenAction) {
        when (action) {
            AuthScreenAction.OnBack -> onBack()
            AuthScreenAction.OnEntrySubmitted -> dismissedStep.value = null
            AuthScreenAction.OnPhoneLoginClick -> loginEntry.method.value = LoginMethod.PhoneNumber
            AuthScreenAction.OnQrLoginClick -> onQrLoginClick()
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
}
