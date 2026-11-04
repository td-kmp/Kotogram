package com.xbot.kotogram.feature.auth.impl.screen.auth

import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.navigation.LocalNavigator
import com.xbot.kotogram.feature.auth.impl.extensions.clearSteps
import org.orbitmvi.orbit.compose.collectSideEffect

/**
 * One screen of the login flow, applying [AuthViewModel]'s routing. Every entry of the flow wraps its
 * screen in it; only the visible one collects.
 */
@Composable
internal fun AuthEntry(viewModel: AuthViewModel, content: @Composable () -> Unit) {
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
    content()
}
