package com.xbot.kotogram.feature.auth.impl.screen.emailcode

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.xbot.kotogram.core.designsystem.PreviewKotogram
import com.xbot.kotogram.core.designsystem.PreviewKotogramLaptop
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.CodeField
import com.xbot.kotogram.core.designsystem.component.LottieImage
import com.xbot.kotogram.core.designsystem.component.ProminentButton
import com.xbot.kotogram.core.designsystem.media.derivedMediaQuery
import com.xbot.kotogram.core.designsystem.media.mediaQuery
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.login_header_code
import com.xbot.kotogram.core.resources.login_new_code_email
import com.xbot.kotogram.core.resources.login_next
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.isBook
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun EmailCodeScreen(
    viewModel: EmailCodeViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.collectAsState()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester, state.codeLength) {
        if (state.codeLength >
            0
        ) {
            focusRequester.requestFocus()
        }
    }
    EmailCodeScreenContent(
        state = state,
        code = viewModel.code,
        onAction = viewModel::onAction,
        modifier = modifier,
        focusRequester = focusRequester,
    )
}

@Composable
internal fun EmailCodeScreenContent(
    state: EmailCodeScreenState,
    code: TextFieldState,
    onAction: (EmailCodeScreenAction) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val style = rememberStyle(::EmailCodeScreenStyleScope, EmailCodeScreenStyle.Adaptive)
    val posture = mediaQuery { windowPosture }
    val landscape by derivedMediaQuery { windowHeight < style.landscapeHeight }
    val nextVisible = style.mediaQueryInfo.isLaptop
    context(style) {
        Scaffold(
            modifier = modifier,
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { innerPadding ->
            Column(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (posture.isBook) 0.5f else 1f)
                    .padding(innerPadding),
            ) {
                BoxWithConstraints(Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .heightIn(min = maxHeight)
                            .padding(horizontal = style.horizontalPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (!landscape && !posture.isTabletop) {
                            LottieImage(STICKER, Modifier.size(style.animationSize))
                            Spacer(Modifier.height(style.beforeTitle))
                        }
                        Text(
                            text = stringResource(Res.string.login_header_code),
                            style = style.titleTextStyle,
                        )
                        Spacer(Modifier.height(style.beforeInfo))
                        Text(
                            text = stringResource(
                                Res.string.login_new_code_email,
                                state.emailPattern,
                            ),
                            style = style.infoTextStyle,
                            modifier = Modifier.widthIn(max = style.infoWidth),
                        )
                        Spacer(Modifier.height(style.beforeField))
                        if (state.codeLength > 0) {
                            CodeField(
                                state = code,
                                length = state.codeLength,
                                modifier = Modifier.focusRequester(focusRequester),
                                isError = state.error != null,
                                onKeyboardAction = { onAction(EmailCodeScreenAction.OnSubmit) },
                            )
                        }
                        Crossfade(
                            targetState = state.error,
                            animationSpec = style.errorFadeSpec,
                            modifier = Modifier
                                .height(style.afterField)
                                .padding(top = style.beforeError),
                        ) { error ->
                            if (error != null) {
                                Text(
                                    text = stringResource(error.localizedMessage()),
                                    style = style.errorTextStyle,
                                )
                            }
                        }
                        if (nextVisible) {
                            ProminentButton(
                                text = stringResource(Res.string.login_next),
                                onClick = { onAction(EmailCodeScreenAction.OnSubmit) },
                                state = ButtonState.of(
                                    enabled = code.text.isNotEmpty(),
                                    loading = state.isSubmitting,
                                ),
                            )
                        }
                    }
                }
                if (posture.isTabletop) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@PreviewKotogram
@Composable
private fun EmailCodeScreenPreview(@PreviewParameter(EmailCodeScreenStateProvider::class) state: EmailCodeScreenState) {
    EmailCodeScreenContent(state = state, code = TextFieldState("12"), onAction = {})
}

@PreviewKotogramLaptop
@Composable
private fun EmailCodeScreenLaptopPreview(
    @PreviewParameter(EmailCodeScreenStateProvider::class) state: EmailCodeScreenState,
) = EmailCodeScreenPreview(state)

private class EmailCodeScreenStateProvider : PreviewParameterProvider<EmailCodeScreenState> {
    private val sent = EmailCodeScreenState(emailPattern = "j*****@g****.com", codeLength = 6)
    override val values = sequenceOf(sent, sent.copy(error = AuthError.InvalidCode))
}

private const val STICKER = "files/letter.lottie"
