package com.xbot.kotogram.feature.auth.impl.screen.code

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.auth.impl.extensions.length
import io.xbot.tdlib.api.AuthorizationStateWaitCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class CodeViewModel(private val authRepository: AuthRepository) :
    ViewModel(),
    OrbitContainerHost<CodeScreenState, CodeScreenState, Nothing> {

    override val container = orbitContainer<CodeScreenState, Nothing>(CodeScreenState()) {
        authRepository.authorizationState
            .filterIsInstance<AuthorizationStateWaitCode>()
            .collectLatest { authState ->
                reduce {
                    state.copy(
                        info = authState.codeInfo,
                        code = "",
                        resendSecondsLeft = authState.codeInfo.timeout
                    )
                }
                while (state.resendSecondsLeft > 0) {
                    delay(TICK_MILLIS)
                    reduce { state.copy(resendSecondsLeft = state.resendSecondsLeft - 1) }
                }
            }
    }

    fun onAction(action: CodeScreenAction) {
        when (action) {
            is CodeScreenAction.OnCodeChange -> onCodeChange(action.value)
            CodeScreenAction.OnSubmit -> submit()
            CodeScreenAction.OnResend -> onResend()
        }
    }

    private fun onCodeChange(value: String) = intent {
        val length = state.info?.type?.length ?: 0
        val code = value.filter(Char::isDigit).let { if (length > 0) it.take(length) else it }
        reduce { state.copy(code = code, error = null) }
        if (length > 0 && code.length == length) submit()
    }

    private fun onResend() = intent {
        if (!state.canResend) return@intent
        authRepository.resendAuthenticationCode().onLeft { error ->
            reduce { state.copy(error = error) }
        }
    }

    private fun submit() = intent {
        if (state.code.isEmpty() || state.isSubmitting) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        val error = authRepository.checkAuthenticationCode(state.code).leftOrNull()
        reduce {
            state.copy(
                isSubmitting = false,
                error = error,
                code = if (error !=
                    null
                ) {
                    ""
                } else {
                    state.code
                }
            )
        }
    }

    private companion object {
        const val TICK_MILLIS = 1_000L
    }
}
