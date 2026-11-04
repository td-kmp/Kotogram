package com.xbot.kotogram.feature.auth.impl.screen.qr

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.auth.domain.usecase.ObserveAuthorizationStateUseCase
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Scope
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

@Scope(AccountId::class)
@KoinViewModel
internal class QrViewModel(
    private val observeAuthorizationState: ObserveAuthorizationStateUseCase,
    private val authRepository: AuthRepository
) : ViewModel(),
    OrbitContainerHost<QrScreenState, QrScreenState, Nothing> {

    override val container = orbitContainer<QrScreenState, Nothing>(QrScreenState()) {
        observeAuthorizationState().collect { authorizationState ->
            reduce {
                state.copy(
                    link = (authorizationState as? AuthorizationStateWaitOtherDeviceConfirmation)
                        ?.link,
                    needsQrCode = authorizationState == AuthorizationStateWaitPhoneNumber
                )
            }
        }
    }

    fun onAction(action: QrScreenAction) {
        when (action) {
            QrScreenAction.OnRequestQrCode -> onRequestQrCode()
        }
    }

    private fun onRequestQrCode() = intent {
        val error = authRepository.requestQrCodeAuthentication().leftOrNull()
        reduce { state.copy(error = error) }
    }
}
