package com.xbot.kotogram.feature.auth.impl.screen.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.xbot.kotogram.core.designsystem.animation.rememberNavTransitions
import com.xbot.kotogram.core.designsystem.style.LocalMediaQueryInfo
import com.xbot.kotogram.feature.auth.api.AuthStep
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
internal fun AuthScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.collectAsState()
    val onBack = { viewModel.onAction(AuthScreenAction.OnBack) }
    val laptop = LocalMediaQueryInfo.current.isLaptop
    val onFirstStep = !state.hasLeftFirstStep

    Box(modifier.fillMaxSize()) {
        if (state.backStack.isEmpty()) return@Box
        AuthSteps(
            backStack = state.backStack,
            canUseQr = state.canUseQr,
            onAction = viewModel::onAction,
            onBack = onBack,
            firstStepFooter = {
                if (!laptop) FirstStepLanguageSuggestion(visible = onFirstStep) { koinViewModel() }
            },
        )

        if (laptop) {
            FirstStepLanguageSuggestion(
                visible = onFirstStep,
                modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding(),
            ) {
                koinViewModel(viewModelStoreOwner = rememberViewModelStoreOwner())
            }
        }
    }
}

@Composable
private fun FirstStepLanguageSuggestion(
    visible: Boolean,
    modifier: Modifier = Modifier,
    viewModel: @Composable () -> LanguageSuggestionViewModel,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(LANGUAGE_SUGGESTION_FADE_MILLIS)) + expandVertically(),
        exit = fadeOut(tween(LANGUAGE_SUGGESTION_FADE_MILLIS)) + shrinkVertically(),
    ) {
        LanguageSuggestion(viewModel = viewModel())
    }
}

@Composable
private fun AuthSteps(
    backStack: List<AuthStep>,
    canUseQr: Boolean,
    onAction: (AuthScreenAction) -> Unit,
    onBack: () -> Unit,
    firstStepFooter: @Composable () -> Unit,
) {
    val transitions = rememberNavTransitions<Scene<AuthStep>>()
    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        transitionSpec = transitions.push,
        popTransitionSpec = transitions.pop,
        predictivePopTransitionSpec = transitions.predictivePop,
        entryDecorators = [
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ],
        entryProvider = entryProvider {
            entry<AuthStep.QrCode> {
                QrScreen(
                    viewModel = koinViewModel(),
                    onPhoneLoginClick = { onAction(AuthScreenAction.OnPhoneLoginClick) },
                    footer = firstStepFooter,
                )
            }
            entry<AuthStep.PhoneNumber> {
                PhoneScreen(
                    viewModel = koinViewModel(),
                    canUseQr = canUseQr,
                    onSubmit = { onAction(AuthScreenAction.OnEntrySubmitted) },
                    onQrLoginClick = { onAction(AuthScreenAction.OnQrLoginClick) },
                    footer = firstStepFooter,
                )
            }
            entry<AuthStep.Code> {
                CodeScreen(
                    viewModel = koinViewModel(),
                    onChangeNumberClick = onBack,
                )
            }
            entry<AuthStep.Password> {
                PasswordScreen(
                    viewModel = koinViewModel(),
                )
            }
            entry<AuthStep.EmailAddress> {
                EmailAddressScreen(
                    viewModel = koinViewModel(),
                )
            }
            entry<AuthStep.EmailCode> {
                EmailCodeScreen(
                    viewModel = koinViewModel(),
                )
            }
            entry<AuthStep.Registration> {
                RegistrationScreen(
                    viewModel = koinViewModel(),
                )
            }
            entry<AuthStep.PremiumPurchase> {
                PremiumPurchaseScreen(
                    onBackClick = onBack,
                )
            }
        },
    )
}

private const val LANGUAGE_SUGGESTION_FADE_MILLIS = 200
