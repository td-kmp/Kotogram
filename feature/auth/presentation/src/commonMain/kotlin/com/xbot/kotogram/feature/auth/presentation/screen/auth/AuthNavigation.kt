package com.xbot.kotogram.feature.auth.presentation.screen.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xbot.kotogram.core.navigation.LocalNavigator
import com.xbot.kotogram.core.navigation.LocalSharedViewModelStoreOwner
import com.xbot.kotogram.core.navigation.Navigator
import com.xbot.kotogram.feature.auth.presentation.component.resolve
import com.xbot.kotogram.feature.auth.presentation.component.toUiText
import com.xbot.kotogram.feature.auth.presentation.navigation.AuthStepRoute
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/** The flow's [AuthViewModel] from a step entry, whose parent is the flow's entry. */
@Composable
internal fun sharedAuthViewModel(): AuthViewModel =
    koinViewModel(viewModelStoreOwner = LocalSharedViewModelStoreOwner.current)

/**
 * One screen of the login flow: applies [AuthViewModel]'s routing and shows its errors (passkey).
 * Every entry of the flow wraps its screen in it; only the visible one collects.
 */
@Composable
internal fun AuthEntry(viewModel: AuthViewModel, content: @Composable () -> Unit) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it.toUiText().resolve()) }
    }
    AuthRouting(viewModel)
    Box(Modifier.fillMaxSize()) {
        content()
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun AuthRouting(viewModel: AuthViewModel) {
    val navigator = LocalNavigator.current
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is AuthScreenSideEffect.ShowStep -> {
                navigator.clearSteps()
                navigator.navigate(effect.route)
            }

            AuthScreenSideEffect.ClearSteps -> navigator.clearSteps()
        }
    }
}

private fun Navigator.clearSteps() {
    while (currentDestination is AuthStepRoute) navigateBack()
}
