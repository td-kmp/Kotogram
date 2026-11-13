package com.xbot.kotogram.feature.auth.impl.screen.password

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import io.xbot.tdlib.api.AuthorizationStateWaitPassword
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class PasswordViewModel(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel(),
    OrbitContainerHost<PasswordScreenState, PasswordScreenState, Nothing> {

    @OptIn(SavedStateHandleSaveableApi::class)
    val password by savedStateHandle.saveable(saver = TextFieldState.Saver) { TextFieldState() }

    @OptIn(SavedStateHandleSaveableApi::class)
    val recoveryCode by savedStateHandle.saveable(saver = TextFieldState.Saver) { TextFieldState() }

    override val container = orbitContainer<PasswordScreenState, Nothing>(PasswordScreenState()) {
        coroutineScope {
            launch { followInput() }
            authRepository.authorizationState
                .filterIsInstance<AuthorizationStateWaitPassword>()
                .collect { authState ->
                    reduce {
                        state.copy(
                            hint = authState.passwordHint,
                            hasRecoveryEmail = authState.hasRecoveryEmailAddress,
                            recoveryEmailPattern = authState.recoveryEmailAddressPattern,
                        )
                    }
                }
        }
    }

    private suspend fun Syntax<PasswordScreenState, Nothing>.followInput() {
        val passwordInput = snapshotFlow { password.text.toString() }.drop(1)
        val codeInput = snapshotFlow { recoveryCode.text.toString() }.drop(1)
        merge(passwordInput, codeInput).collect { value ->
            if (value.isNotEmpty()) reduce { state.copy(error = null) }
            if (state.isRecovering && recoveryCode.text.length == RECOVERY_CODE_LENGTH) onSubmit()
        }
    }

    fun onAction(action: PasswordScreenAction) {
        when (action) {
            OnSubmit -> onSubmit()
            OnForgotPassword -> onForgotPassword()
        }
    }

    private fun onSubmit() = intent {
        if (state.isSubmitting) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        val result = if (state.isRecovering) {
            authRepository.recoverAuthenticationPassword(recoveryCode.text.toString())
        } else {
            authRepository.checkAuthenticationPassword(password.text.toString())
        }
        reduce { state.copy(isSubmitting = false, error = result.leftOrNull()) }
    }

    private fun onForgotPassword() = intent {
        if (!state.hasRecoveryEmail) return@intent
        authRepository.requestAuthenticationPasswordRecovery().fold(
            ifLeft = { error -> reduce { state.copy(error = error) } },
            ifRight = { reduce { state.copy(isRecovering = true, error = null) } },
        )
    }

    companion object {
        const val RECOVERY_CODE_LENGTH = 6
    }
}
