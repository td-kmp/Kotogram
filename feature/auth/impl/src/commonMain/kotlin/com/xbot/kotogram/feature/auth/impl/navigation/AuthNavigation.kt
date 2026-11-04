package com.xbot.kotogram.feature.auth.impl.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import com.xbot.kotogram.core.navigation.FeatureNavigation
import com.xbot.kotogram.core.navigation.LocalNavigator
import com.xbot.kotogram.core.navigation.LocalSharedViewModelStoreOwner
import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.navigation.SharedViewModelStoreNavEntryDecorator
import com.xbot.kotogram.core.navigation.extensions.dropUnlessResumed
import com.xbot.kotogram.feature.auth.api.AuthRoute
import com.xbot.kotogram.feature.auth.domain.model.LoginMethod
import com.xbot.kotogram.feature.auth.impl.screen.auth.AuthEntry
import com.xbot.kotogram.feature.auth.impl.screen.auth.AuthScreenAction
import com.xbot.kotogram.feature.auth.impl.screen.auth.AuthViewModel
import com.xbot.kotogram.feature.auth.impl.screen.code.CodeScreen
import com.xbot.kotogram.feature.auth.impl.screen.emailaddress.EmailAddressScreen
import com.xbot.kotogram.feature.auth.impl.screen.emailcode.EmailCodeScreen
import com.xbot.kotogram.feature.auth.impl.screen.password.PasswordScreen
import com.xbot.kotogram.feature.auth.impl.screen.phone.PhoneScreen
import com.xbot.kotogram.feature.auth.impl.screen.premiumpurchase.PremiumPurchaseScreen
import com.xbot.kotogram.feature.auth.impl.screen.qr.QrScreen
import com.xbot.kotogram.feature.auth.impl.screen.registration.RegistrationScreen
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

/** The flow's entry owns the shared [AuthViewModel]; every other screen of the flow is its child. */
private val authChild = SharedViewModelStoreNavEntryDecorator.parent(AuthRoute.toString())

@Composable
private fun sharedAuthViewModel(): AuthViewModel =
    koinViewModel(viewModelStoreOwner = LocalSharedViewModelStoreOwner.current)

object AuthNavigation : FeatureNavigation {
    override fun PolymorphicModuleBuilder<NavKey>.routes() {
        subclass(AuthRoute::class)
        subclass(PhoneNumberRoute::class)
        subclass(CodeRoute::class)
        subclass(PasswordRoute::class)
        subclass(EmailAddressRoute::class)
        subclass(EmailCodeRoute::class)
        subclass(RegistrationRoute::class)
        subclass(PremiumPurchaseRoute::class)
    }

    override fun EntryProviderScope<NavKey>.entries() {
        entry<AuthRoute> {
            val navigator = LocalNavigator.current
            val lifecycleOwner = LocalLifecycleOwner.current
            val viewModel: AuthViewModel = koinViewModel()
            val state by viewModel.collectAsState()
            AuthEntry(viewModel) {
                when (state.entryMethod) {
                    LoginMethod.QrCode -> QrScreen(
                        viewModel = koinViewModel(),
                        onPhoneLoginClick = lifecycleOwner.dropUnlessResumed {
                            navigator.navigate(PhoneNumberRoute)
                        }
                    )

                    LoginMethod.PhoneNumber -> PhoneScreen(
                        viewModel = koinViewModel(),
                        canUseQr = false,
                        onSubmitted = { viewModel.onAction(AuthScreenAction.OnEntrySubmitted) },
                        onQrLoginClick = {}
                    )
                }
            }
        }

        entry<PhoneNumberRoute>(metadata = authChild) {
            val navigator = LocalNavigator.current
            val lifecycleOwner = LocalLifecycleOwner.current
            val viewModel = sharedAuthViewModel()
            AuthEntry(viewModel) {
                PhoneScreen(
                    viewModel = koinViewModel(),
                    canUseQr = true,
                    onSubmitted = { viewModel.onAction(AuthScreenAction.OnEntrySubmitted) },
                    // The QR code under this screen is still valid: TDLib leaves QR login only on submit.
                    onQrLoginClick = lifecycleOwner.dropUnlessResumed { navigator.navigateBack() }
                )
            }
        }

        entry<CodeRoute>(metadata = authChild) {
            val navigator = LocalNavigator.current
            val lifecycleOwner = LocalLifecycleOwner.current
            AuthEntry(sharedAuthViewModel()) {
                CodeScreen(
                    viewModel = koinViewModel(),
                    onChangeNumberClick = lifecycleOwner.dropUnlessResumed {
                        navigator.navigateBack()
                    }
                )
            }
        }

        entry<PasswordRoute>(metadata = authChild) {
            AuthEntry(sharedAuthViewModel()) { PasswordScreen(viewModel = koinViewModel()) }
        }

        entry<EmailAddressRoute>(metadata = authChild) {
            AuthEntry(sharedAuthViewModel()) { EmailAddressScreen(viewModel = koinViewModel()) }
        }

        entry<EmailCodeRoute>(metadata = authChild) {
            AuthEntry(sharedAuthViewModel()) { EmailCodeScreen(viewModel = koinViewModel()) }
        }

        entry<RegistrationRoute>(metadata = authChild) {
            AuthEntry(sharedAuthViewModel()) { RegistrationScreen(viewModel = koinViewModel()) }
        }

        entry<PremiumPurchaseRoute>(metadata = authChild) {
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
}
