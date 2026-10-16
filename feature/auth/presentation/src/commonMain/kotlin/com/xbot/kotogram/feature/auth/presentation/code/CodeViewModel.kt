package com.xbot.kotogram.feature.auth.presentation.code

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.AuthException
import com.xbot.kotogram.feature.auth.domain.model.AuthState
import com.xbot.kotogram.feature.auth.domain.model.CodeInfo
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.auth.domain.usecase.ObserveAuthStateUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

data class CodeState(
    val info: CodeInfo? = null,
    val code: String = "",
    /** Seconds until [CodeInfo.nextType] can be requested; 0 when resending is allowed. */
    val resendSecondsLeft: Int = 0,
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
) {
    val canResend: Boolean get() = info?.nextType != null && resendSecondsLeft == 0
}

class CodeViewModel(
    private val observeAuthState: ObserveAuthStateUseCase,
    private val authRepository: AuthRepository
) : ViewModel(),
    OrbitContainerHost<CodeState, CodeState, Nothing> {

    override val container = orbitContainer<CodeState, Nothing>(CodeState()) {
        observeAuthState().filterIsInstance<AuthState.WaitCode>().collectLatest { authState ->
            reduce {
                state.copy(
                    info = authState.info,
                    code = "",
                    resendSecondsLeft = authState.info.timeoutSeconds
                )
            }
            while (state.resendSecondsLeft > 0) {
                delay(TICK_MILLIS)
                reduce { state.copy(resendSecondsLeft = state.resendSecondsLeft - 1) }
            }
        }
    }

    fun onCodeChange(value: String) = intent {
        val length = state.info?.type?.length ?: 0
        val code = value.filter(Char::isDigit).let { if (length > 0) it.take(length) else it }
        reduce { state.copy(code = code, error = null) }
        if (length > 0 && code.length == length) submit()
    }

    fun onSubmit() = intent { submit() }

    fun onResend() = intent {
        if (!state.canResend) return@intent
        try {
            authRepository.resendCode()
        } catch (e: AuthException) {
            reduce { state.copy(error = e.error) }
        }
    }

    private fun submit() = intent {
        if (state.code.isEmpty() || state.isSubmitting) return@intent
        reduce { state.copy(isSubmitting = true, error = null) }
        try {
            authRepository.checkCode(state.code)
        } catch (e: AuthException) {
            reduce { state.copy(error = e.error, code = "") }
        } finally {
            reduce { state.copy(isSubmitting = false) }
        }
    }

    private companion object {
        const val TICK_MILLIS = 1_000L
    }
}
