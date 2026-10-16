package com.xbot.kotogram.ui.root

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.domain.model.AuthState
import com.xbot.kotogram.feature.auth.domain.usecase.ObserveAuthStateUseCase
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

/** Top-level destinations, switched by TDLib's authorization state. */
sealed interface RootRoute {
    data object Splash : RootRoute

    data object Auth : RootRoute

    data object Main : RootRoute
}

data class RootState(val route: RootRoute = RootRoute.Splash)

class RootViewModel(private val observeAuthState: ObserveAuthStateUseCase) :
    ViewModel(),
    OrbitContainerHost<RootState, RootState, Nothing> {

    override val container = orbitContainer<RootState, Nothing>(RootState()) {
        observeAuthState().collect { authState ->
            val route = when (authState) {
                AuthState.Initializing, AuthState.LoggingOut -> RootRoute.Splash
                AuthState.Ready -> RootRoute.Main
                else -> RootRoute.Auth
            }
            reduce { state.copy(route = route) }
        }
    }
}
