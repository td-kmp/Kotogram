package com.xbot.kotogram.feature.auth.presentation.flow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.xbot.kotogram.feature.auth.domain.model.LoginMethod
import com.xbot.kotogram.feature.auth.presentation.code.CodeRoute
import com.xbot.kotogram.feature.auth.presentation.common.resolve
import com.xbot.kotogram.feature.auth.presentation.common.toUiText
import com.xbot.kotogram.feature.auth.presentation.email.EmailAddressRoute
import com.xbot.kotogram.feature.auth.presentation.email.EmailCodeRoute
import com.xbot.kotogram.feature.auth.presentation.password.PasswordRoute
import com.xbot.kotogram.feature.auth.presentation.phone.PhoneRoute
import com.xbot.kotogram.feature.auth.presentation.qr.QrRoute
import com.xbot.kotogram.feature.auth.presentation.registration.RegistrationRoute
import com.xbot.kotogram.feature.auth.presentation.registration.UnsupportedScreen
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/** The whole login flow; leaves composition once TDLib reports authorizationStateReady. */
@Composable
fun AuthFlow(modifier: Modifier = Modifier, viewModel: AuthFlowViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is AuthFlowSideEffect.ShowError -> snackbarHostState.showSnackbar(
                effect.error.toUiText().resolve()
            )
        }
    }

    val loginMethods = state.loginMethods
    Box(modifier.fillMaxSize()) {
        NavDisplay(
            backStack = state.backStack,
            onBack = viewModel::onBack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<AuthRoute.Qr> {
                    QrRoute(
                        canUsePasskey = LoginMethod.Passkey in loginMethods,
                        onPhoneLogin = { viewModel.onSelectMethod(LoginMethod.PhoneNumber) },
                        onPasskeyLogin = { viewModel.onSelectMethod(LoginMethod.Passkey) }
                    )
                }
                entry<AuthRoute.Phone> {
                    PhoneRoute(
                        canUseQr = LoginMethod.QrCode in loginMethods,
                        canUsePasskey = LoginMethod.Passkey in loginMethods,
                        onSubmitted = viewModel::onEntryCompleted,
                        onQrLogin = { viewModel.onSelectMethod(LoginMethod.QrCode) },
                        onPasskeyLogin = { viewModel.onSelectMethod(LoginMethod.Passkey) }
                    )
                }
                entry<AuthRoute.Code> { CodeRoute(onChangeNumber = viewModel::onBack) }
                entry<AuthRoute.EmailAddress> { EmailAddressRoute() }
                entry<AuthRoute.EmailCode> { EmailCodeRoute() }
                entry<AuthRoute.Password> { PasswordRoute() }
                entry<AuthRoute.Registration> { RegistrationRoute() }
                entry<AuthRoute.Unsupported> { UnsupportedScreen(onBack = viewModel::onBack) }
            }
        )
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }
}
