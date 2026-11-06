package com.xbot.kotogram.feature.auth.impl.screen.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.xbot.kotogram.feature.auth.impl.navigation.AuthStep
import com.xbot.kotogram.feature.auth.impl.screen.code.CodeScreen
import com.xbot.kotogram.feature.auth.impl.screen.emailaddress.EmailAddressScreen
import com.xbot.kotogram.feature.auth.impl.screen.emailcode.EmailCodeScreen
import com.xbot.kotogram.feature.auth.impl.screen.password.PasswordScreen
import com.xbot.kotogram.feature.auth.impl.screen.phone.PhoneScreen
import com.xbot.kotogram.feature.auth.impl.screen.premiumpurchase.PremiumPurchaseScreen
import com.xbot.kotogram.feature.auth.impl.screen.qr.QrScreen
import com.xbot.kotogram.feature.auth.impl.screen.registration.RegistrationScreen
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun AuthScreen(viewModel: AuthViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.collectAsState()
    val onBack = { viewModel.onAction(AuthScreenAction.OnBack) }
    NavDisplay(
        backStack = state.backStack,
        modifier = modifier,
        onBack = onBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<AuthStep>(),
            rememberViewModelStoreNavEntryDecorator<AuthStep>()
        ),
        entryProvider = entryProvider {
            entry<AuthStep.QrCode> {
                QrScreen(
                    viewModel = koinViewModel(),
                    onPhoneLoginClick = { viewModel.onAction(AuthScreenAction.OnPhoneLoginClick) }
                )
            }
            entry<AuthStep.PhoneNumber> {
                PhoneScreen(
                    viewModel = koinViewModel(),
                    canUseQr = state.canUseQr,
                    onSubmitted = { viewModel.onAction(AuthScreenAction.OnEntrySubmitted) },
                    onQrLoginClick = { viewModel.onAction(AuthScreenAction.OnQrLoginClick) }
                )
            }
            entry<AuthStep.Code> {
                CodeScreen(viewModel = koinViewModel(), onChangeNumberClick = onBack)
            }
            entry<AuthStep.Password> { PasswordScreen(viewModel = koinViewModel()) }
            entry<AuthStep.EmailAddress> { EmailAddressScreen(viewModel = koinViewModel()) }
            entry<AuthStep.EmailCode> { EmailCodeScreen(viewModel = koinViewModel()) }
            entry<AuthStep.Registration> { RegistrationScreen(viewModel = koinViewModel()) }
            entry<AuthStep.PremiumPurchase> { PremiumPurchaseScreen(onBackClick = onBack) }
        }
    )
}
