package com.xbot.kotogram.feature.auth.presentation.flow

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.domain.model.AuthException
import com.xbot.kotogram.feature.auth.domain.model.AuthState
import com.xbot.kotogram.feature.auth.domain.model.LoginMethod
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.auth.domain.usecase.GetLoginMethodsUseCase
import com.xbot.kotogram.feature.auth.domain.usecase.LoginWithPasskeyUseCase
import com.xbot.kotogram.feature.auth.domain.usecase.ObserveAuthStateUseCase
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer

class AuthFlowViewModel(
    private val observeAuthState: ObserveAuthStateUseCase,
    private val getLoginMethods: GetLoginMethodsUseCase,
    private val loginWithPasskey: LoginWithPasskeyUseCase,
    private val authRepository: AuthRepository,
    supportsQrLogin: Boolean
) : ViewModel(),
    OrbitContainerHost<AuthFlowState, AuthFlowState, AuthFlowSideEffect> {

    override val container = orbitContainer<AuthFlowState, AuthFlowSideEffect>(
        initialState = AuthFlowState(
            entryMethod = if (supportsQrLogin) LoginMethod.QrCode else LoginMethod.PhoneNumber
        )
    ) {
        val methods = getLoginMethods()
        reduce { state.copy(loginMethods = methods) }
        observeAuthState().collect { authState ->
            reduce { state.copy(authState = authState, isEditingEntry = false) }
            if (authState == AuthState.WaitPhoneNumber &&
                state.entryMethod == LoginMethod.QrCode
            ) {
                requestQrCode()
            }
        }
    }

    fun onSelectMethod(method: LoginMethod) = intent {
        when (method) {
            LoginMethod.QrCode -> {
                reduce { state.copy(entryMethod = LoginMethod.QrCode) }
                if (state.authState !is AuthState.WaitQrConfirmation) requestQrCode()
            }

            LoginMethod.PhoneNumber -> {
                reduce { state.copy(entryMethod = LoginMethod.PhoneNumber) }
                // TDLib only leaves QR confirmation through a log out; nothing is authorized yet, so
                // it just restarts at WaitPhoneNumber.
                if (state.authState is AuthState.WaitQrConfirmation) cancelQrLogin()
            }

            LoginMethod.Passkey -> {
                reduce { state.copy(isPasskeyInProgress = true) }
                try {
                    loginWithPasskey()
                } catch (e: AuthException) {
                    postSideEffect(AuthFlowSideEffect.ShowError(e.error))
                } finally {
                    reduce { state.copy(isPasskeyInProgress = false) }
                }
            }
        }
    }

    /** Back from a step returns to the entry screen; TDLib accepts a new number or QR request from steps. */
    fun onBack() = intent {
        if (state.backStack.size <= 1) return@intent
        reduce { state.copy(isEditingEntry = true) }
        if (state.entryMethod == LoginMethod.QrCode) requestQrCode()
    }

    /** The entry screen submitted again; show the current step even if TDLib's state didn't change. */
    fun onEntryCompleted() = intent {
        reduce { state.copy(isEditingEntry = false) }
    }

    private suspend fun Syntax<AuthFlowState, AuthFlowSideEffect>.cancelQrLogin() {
        try {
            authRepository.logOut()
        } catch (e: AuthException) {
            postSideEffect(AuthFlowSideEffect.ShowError(e.error))
        }
    }

    private suspend fun Syntax<AuthFlowState, AuthFlowSideEffect>.requestQrCode() {
        try {
            authRepository.requestQrCode()
        } catch (e: AuthException) {
            reduce { state.copy(entryMethod = LoginMethod.PhoneNumber) }
            postSideEffect(AuthFlowSideEffect.ShowError(e.error))
        }
    }
}
