package com.xbot.kotogram.feature.auth.presentation.qr

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.domain.model.AuthState
import com.xbot.kotogram.feature.auth.domain.usecase.ObserveAuthStateUseCase
import kotlinx.coroutines.flow.filterIsInstance
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

/** @property link the tg://login link to render as a QR code; null until TDLib issues one. */
data class QrState(val link: String? = null)

class QrViewModel(private val observeAuthState: ObserveAuthStateUseCase) :
    ViewModel(),
    OrbitContainerHost<QrState, QrState, Nothing> {

    override val container = orbitContainer<QrState, Nothing>(QrState()) {
        observeAuthState().filterIsInstance<AuthState.WaitQrConfirmation>().collect { authState ->
            reduce { state.copy(link = authState.link) }
        }
    }
}
