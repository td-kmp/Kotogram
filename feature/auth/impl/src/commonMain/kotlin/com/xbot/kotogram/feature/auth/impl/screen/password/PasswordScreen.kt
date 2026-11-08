package com.xbot.kotogram.feature.auth.impl.screen.password

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.isSpecified
import com.xbot.kotogram.core.designsystem.KotogramLaptopPreview
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.CodeField
import com.xbot.kotogram.core.designsystem.component.IconButton
import com.xbot.kotogram.core.designsystem.component.ProminentButton
import com.xbot.kotogram.core.designsystem.component.SecureTextField
import com.xbot.kotogram.core.designsystem.component.TextButton
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.media.derivedMediaQuery
import com.xbot.kotogram.core.designsystem.media.mediaQuery
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.login_new_password_info
import com.xbot.kotogram.core.resources.login_new_password_label
import com.xbot.kotogram.core.resources.login_new_password_placeholder
import com.xbot.kotogram.core.resources.login_next
import com.xbot.kotogram.core.resources.login_passwordforgot
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.core.resources.twostepauth_recoverysent
import com.xbot.kotogram.core.resources.twostepauth_recoverytitle
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.isBook
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun PasswordScreen(viewModel: PasswordViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.collectAsState()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester, state.isRecovering) { focusRequester.requestFocus() }
    PasswordScreenContent(
        state = state,
        password = viewModel.password,
        recoveryCode = viewModel.recoveryCode,
        onAction = viewModel::onAction,
        modifier = modifier,
        focusRequester = focusRequester
    )
}

@Composable
internal fun PasswordScreenContent(
    state: PasswordScreenState,
    password: TextFieldState,
    recoveryCode: TextFieldState,
    onAction: (PasswordScreenAction) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val style = rememberStyle(::PasswordScreenStyleScope, PasswordScreenStyle.Adaptive)
    val posture = mediaQuery { windowPosture }
    val landscape by derivedMediaQuery { windowHeight < style.landscapeHeight }
    // At the bottom as Telegram for iOS has it; under the field on laptops and phones on their side.
    val actionAtBottom = !style.mediaQueryInfo.isLaptop && !landscape
    context(style) {
        Scaffold(modifier = modifier, contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
            Column(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (posture.isBook) 0.5f else 1f)
                    .padding(innerPadding)
            ) {
                BoxWithConstraints(Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .heightIn(min = maxHeight)
                            .padding(horizontal = style.horizontalPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (!landscape && !posture.isTabletop) {
                            // Temporary: the step's animation goes here.
                            Box(Modifier.size(style.animationSize).background(Color.White))
                            Spacer(Modifier.height(style.beforeTitle))
                        }
                        val title = if (state.isRecovering) {
                            Res.string.twostepauth_recoverytitle
                        } else {
                            Res.string.login_new_password_label
                        }
                        Text(text = stringResource(title), style = style.titleTextStyle)
                        Spacer(Modifier.height(style.beforeInfo))
                        Text(
                            text = if (state.isRecovering) {
                                stringResource(Res.string.twostepauth_recoverysent, state.recoveryEmailPattern)
                            } else {
                                stringResource(Res.string.login_new_password_info)
                            },
                            style = style.infoTextStyle,
                            modifier = Modifier.widthIn(max = style.infoWidth)
                        )
                        Spacer(Modifier.height(style.beforeField))
                        if (state.isRecovering) {
                            CodeField(
                                state = recoveryCode,
                                length = PasswordViewModel.RECOVERY_CODE_LENGTH,
                                modifier = Modifier.focusRequester(focusRequester),
                                isError = state.error != null,
                                onKeyboardAction = { onAction(PasswordScreenAction.OnSubmit) }
                            )
                        } else {
                            PasswordField(state, password, onAction, Modifier.focusRequester(focusRequester))
                        }
                        Crossfade(
                            targetState = state.error,
                            animationSpec = style.errorFadeSpec,
                            modifier = Modifier
                                .height(style.afterField)
                                .padding(top = style.beforeError)
                        ) { error ->
                            if (error != null) {
                                Text(text = stringResource(error.localizedMessage()), style = style.errorTextStyle)
                            }
                        }
                        if (!actionAtBottom) {
                            PasswordAction(state, onAction)
                            Spacer(Modifier.height(style.beforeLinks))
                        }
                        if (!state.isRecovering && state.hasRecoveryEmail) {
                            TextButton(onClick = { onAction(PasswordScreenAction.OnForgotPassword) }) {
                                Text(stringResource(Res.string.login_passwordforgot))
                            }
                        }
                    }
                }
                // Half open on a table: the form above the fold, the keyboard below it.
                if (posture.isTabletop) Spacer(Modifier.weight(1f))
                if (actionAtBottom) {
                    PasswordAction(
                        state = state,
                        onAction = onAction,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(style.bottomActionInset)
                    )
                }
            }
        }
    }
}

@Composable
context(style: PasswordScreenStyleScope)
private fun PasswordField(
    state: PasswordScreenState,
    password: TextFieldState,
    onAction: (PasswordScreenAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var revealed by remember { mutableStateOf(false) }
    SecureTextField(
        state = password,
        modifier = modifier.contentWidth(),
        // Telegram for macOS shows the hint in place of "Enter Password".
        placeholder = { Text(state.hint.ifEmpty { stringResource(Res.string.login_new_password_placeholder) }) },
        trailingIcon = {
            IconButton(
                icon = if (revealed) KotogramIcons.EyeOff else KotogramIcons.Eye,
                contentDescription = null,
                onClick = { revealed = !revealed }
            )
        },
        isError = state.error != null,
        textObfuscationMode = if (revealed) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
        onKeyboardAction = { onAction(PasswordScreenAction.OnSubmit) }
    )
}

@Composable
context(style: PasswordScreenStyleScope)
private fun PasswordAction(
    state: PasswordScreenState,
    onAction: (PasswordScreenAction) -> Unit,
    modifier: Modifier = Modifier
) {
    ProminentButton(
        text = stringResource(Res.string.login_next),
        onClick = { onAction(PasswordScreenAction.OnSubmit) },
        state = ButtonState.of(enabled = true, loading = state.isSubmitting),
        modifier = modifier.contentWidth()
    )
}

context(style: PasswordScreenStyleScope)
private fun Modifier.contentWidth(): Modifier =
    if (style.contentMaxWidth.isSpecified) widthIn(max = style.contentMaxWidth).fillMaxWidth() else this

@KotogramPreview
@Composable
private fun PasswordScreenPreview(@PreviewParameter(PasswordScreenStateProvider::class) state: PasswordScreenState) {
    PasswordScreenContent(
        state = state,
        password = TextFieldState("password"),
        recoveryCode = TextFieldState("123"),
        onAction = {}
    )
}

@KotogramLaptopPreview
@Composable
private fun PasswordScreenLaptopPreview(
    @PreviewParameter(PasswordScreenStateProvider::class) state: PasswordScreenState
) = PasswordScreenPreview(state)

private class PasswordScreenStateProvider : PreviewParameterProvider<PasswordScreenState> {
    private val protected = PasswordScreenState(
        hint = "first pet",
        hasRecoveryEmail = true,
        recoveryEmailPattern = "j*****@g****.com"
    )
    override val values = sequenceOf(
        protected,
        protected.copy(error = AuthError.InvalidPassword),
        protected.copy(isRecovering = true)
    )
}
