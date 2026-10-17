package com.xbot.kotogram.feature.auth.presentation.di

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.xbot.kotogram.core.navigation.LocalNavigator
import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.navigation.SharedViewModelStoreNavEntryDecorator
import com.xbot.kotogram.core.navigation.dropUnlessResumed
import com.xbot.kotogram.core.navigation.polymorphic
import com.xbot.kotogram.feature.auth.api.AuthRoute
import com.xbot.kotogram.feature.auth.domain.model.LoginMethod
import com.xbot.kotogram.feature.auth.presentation.navigation.CodeRoute
import com.xbot.kotogram.feature.auth.presentation.navigation.EmailAddressRoute
import com.xbot.kotogram.feature.auth.presentation.navigation.EmailCodeRoute
import com.xbot.kotogram.feature.auth.presentation.navigation.PasswordRoute
import com.xbot.kotogram.feature.auth.presentation.navigation.PhoneNumberRoute
import com.xbot.kotogram.feature.auth.presentation.navigation.PremiumPurchaseRoute
import com.xbot.kotogram.feature.auth.presentation.navigation.RegistrationRoute
import com.xbot.kotogram.feature.auth.presentation.screen.auth.AuthEntry
import com.xbot.kotogram.feature.auth.presentation.screen.auth.AuthScreenAction
import com.xbot.kotogram.feature.auth.presentation.screen.auth.AuthViewModel
import com.xbot.kotogram.feature.auth.presentation.screen.auth.sharedAuthViewModel
import com.xbot.kotogram.feature.auth.presentation.screen.code.CodeScreen
import com.xbot.kotogram.feature.auth.presentation.screen.emailaddress.EmailAddressScreen
import com.xbot.kotogram.feature.auth.presentation.screen.emailcode.EmailCodeScreen
import com.xbot.kotogram.feature.auth.presentation.screen.password.PasswordScreen
import com.xbot.kotogram.feature.auth.presentation.screen.phone.PhoneScreen
import com.xbot.kotogram.feature.auth.presentation.screen.premiumpurchase.PremiumPurchaseScreen
import com.xbot.kotogram.feature.auth.presentation.screen.qr.QrScreen
import com.xbot.kotogram.feature.auth.presentation.screen.registration.RegistrationScreen
import kotlinx.serialization.modules.subclass
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.orbitmvi.orbit.compose.collectAsState

/** The flow's entry owns the shared [AuthViewModel]; every other screen of the flow is its child. */
private val authChild = SharedViewModelStoreNavEntryDecorator.parent(AuthRoute.toString())

@OptIn(KoinExperimentalAPI::class)
val authFeatureModule = module {
    polymorphic<NavKey> {
        subclass(AuthRoute::class)
        subclass(PhoneNumberRoute::class)
        subclass(CodeRoute::class)
        subclass(PasswordRoute::class)
        subclass(EmailAddressRoute::class)
        subclass(EmailCodeRoute::class)
        subclass(RegistrationRoute::class)
        subclass(PremiumPurchaseRoute::class)
    }

    navigation<AuthRoute> {
        val navigator = LocalNavigator.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val viewModel: AuthViewModel = koinViewModel()
        val state by viewModel.collectAsState()
        val canUsePasskey = LoginMethod.Passkey in state.loginMethods
        AuthEntry(viewModel) {
            when (state.entryMethod) {
                LoginMethod.QrCode -> QrScreen(
                    canUsePasskey = canUsePasskey,
                    onPhoneLoginClick = lifecycleOwner.dropUnlessResumed {
                        navigator.navigate(PhoneNumberRoute)
                    },
                    onPasskeyLoginClick = {
                        viewModel.onAction(AuthScreenAction.OnPasskeyLoginClick)
                    }
                )

                else -> PhoneScreen(
                    canUseQr = false,
                    canUsePasskey = canUsePasskey,
                    onSubmitted = { viewModel.onAction(AuthScreenAction.OnEntrySubmitted) },
                    onQrLoginClick = {},
                    onPasskeyLoginClick = {
                        viewModel.onAction(AuthScreenAction.OnPasskeyLoginClick)
                    }
                )
            }
        }
    }

    navigation<PhoneNumberRoute>(metadata = authChild) {
        val navigator = LocalNavigator.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val viewModel = sharedAuthViewModel()
        val state by viewModel.collectAsState()
        AuthEntry(viewModel) {
            PhoneScreen(
                canUseQr = true,
                canUsePasskey = LoginMethod.Passkey in state.loginMethods,
                onSubmitted = { viewModel.onAction(AuthScreenAction.OnEntrySubmitted) },
                // The QR code under this screen is still valid: TDLib leaves QR login only on submit.
                onQrLoginClick = lifecycleOwner.dropUnlessResumed { navigator.navigateBack() },
                onPasskeyLoginClick = { viewModel.onAction(AuthScreenAction.OnPasskeyLoginClick) }
            )
        }
    }

    navigation<CodeRoute>(metadata = authChild) {
        val navigator = LocalNavigator.current
        val lifecycleOwner = LocalLifecycleOwner.current
        AuthEntry(sharedAuthViewModel()) {
            CodeScreen(
                onChangeNumberClick = lifecycleOwner.dropUnlessResumed {
                    navigator.navigateBack()
                }
            )
        }
    }

    navigation<PasswordRoute>(metadata = authChild) {
        AuthEntry(sharedAuthViewModel()) { PasswordScreen() }
    }

    navigation<EmailAddressRoute>(metadata = authChild) {
        AuthEntry(sharedAuthViewModel()) { EmailAddressScreen() }
    }

    navigation<EmailCodeRoute>(metadata = authChild) {
        AuthEntry(sharedAuthViewModel()) { EmailCodeScreen() }
    }

    navigation<RegistrationRoute>(metadata = authChild) {
        AuthEntry(sharedAuthViewModel()) { RegistrationScreen() }
    }

    navigation<PremiumPurchaseRoute>(metadata = authChild) {
        val navigator = LocalNavigator.current
        val lifecycleOwner = LocalLifecycleOwner.current
        AuthEntry(sharedAuthViewModel()) {
            PremiumPurchaseScreen(
                onBackClick = lifecycleOwner.dropUnlessResumed {
                    navigator.navigateBack()
                }
            )
        }
    }
}
