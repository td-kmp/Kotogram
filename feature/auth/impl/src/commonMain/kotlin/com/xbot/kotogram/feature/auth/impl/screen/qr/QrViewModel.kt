package com.xbot.kotogram.feature.auth.impl.screen.qr

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class QrViewModel(
    private val authRepository: AuthRepository,
) : ViewModel(),
    OrbitContainerHost<QrScreenState, QrScreenState, Nothing> {

    override val container = orbitContainer<QrScreenState, Nothing>(QrScreenState()) {
        authRepository.authorizationState.collect { authorizationState ->
            reduce {
                state.copy(
                    link = (authorizationState as? AuthorizationStateWaitOtherDeviceConfirmation)
                        ?.link,
                    needsQrCode = authorizationState == AuthorizationStateWaitPhoneNumber,
                )
            }
        }
    }

    fun onAction(action: QrScreenAction) {
        when (action) {
            OnRequestQrCode -> onRequestQrCode()
        }
    }

    private fun onRequestQrCode() = intent {
        val error = authRepository.requestQrCodeAuthentication().leftOrNull()
        reduce { state.copy(error = error) }
    }
}
