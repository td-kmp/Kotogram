package com.xbot.kotogram.feature.auth.impl.screen.code

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.xbot.kotogram.core.designsystem.KotogramLaptopPreview
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.CodeField
import com.xbot.kotogram.core.designsystem.component.LottieImage
import com.xbot.kotogram.core.designsystem.component.ProminentButton
import com.xbot.kotogram.core.designsystem.component.TextButton
import com.xbot.kotogram.core.designsystem.component.TextField
import com.xbot.kotogram.core.designsystem.media.derivedMediaQuery
import com.xbot.kotogram.core.designsystem.media.mediaQuery
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.annotatedStringResource
import com.xbot.kotogram.core.resources.editaccount_changenumber
import com.xbot.kotogram.core.resources.extensions.description
import com.xbot.kotogram.core.resources.extensions.title
import com.xbot.kotogram.core.resources.login_header_code
import com.xbot.kotogram.core.resources.login_next
import com.xbot.kotogram.core.resources.login_sendsmsifnotreceivedappcode
import com.xbot.kotogram.core.resources.login_willcall
import com.xbot.kotogram.core.resources.login_willsendsms
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.auth.domain.extensions.length
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.isBook
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
import io.xbot.tdlib.api.AuthenticationCodeInfo
import io.xbot.tdlib.api.AuthenticationCodeTypeCall
import io.xbot.tdlib.api.AuthenticationCodeTypeFragment
import io.xbot.tdlib.api.AuthenticationCodeTypeSms
import io.xbot.tdlib.api.AuthenticationCodeTypeSmsPhrase
import io.xbot.tdlib.api.AuthenticationCodeTypeSmsWord
import io.xbot.tdlib.api.AuthenticationCodeTypeTelegramMessage
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun CodeScreen(
    viewModel: CodeViewModel,
    onChangeNumberClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.collectAsState()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    CodeScreenContent(
        state = state,
        code = viewModel.code,
        onAction = viewModel::onAction,
        onChangeNumberClick = onChangeNumberClick,
        modifier = modifier,
        focusRequester = focusRequester,
    )
}

@Composable
internal fun CodeScreenContent(
    state: CodeScreenState,
    code: TextFieldState,
    onAction: (CodeScreenAction) -> Unit,
    onChangeNumberClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val style = rememberStyle(::CodeScreenStyleScope, CodeScreenStyle.Adaptive)
    val posture = mediaQuery { windowPosture }
    val landscape by derivedMediaQuery { windowHeight < style.landscapeHeight }
    val nextVisible = style.mediaQueryInfo.isLaptop
    val info = state.info
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
                            if (info?.type is AuthenticationCodeTypeFragment) {
                                LottieImage(
                                    PIRATE_FLAG,
                                    Modifier.size(style.animationSize),
                                    iterations = 3,
                                )
                            } else {
                                LottieImage(
                                    path = style.sticker,
                                    modifier = Modifier.size(style.animationSize),
                                    colors = style.stickerColors,
                                )
                            }
                            Spacer(Modifier.height(style.beforeTitle))
                        }
                        if (info != null) CodeHeader(info, onChangeNumberClick)
                        Spacer(Modifier.height(style.beforeField))
                        CodeInput(info, code, state.error, onAction, focusRequester)
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
                                onClick = { onAction(CodeScreenAction.OnSubmit) },
                                state = ButtonState.of(
                                    enabled = code.text.isNotEmpty(),
                                    loading = state.isSubmitting,
                                ),
                            )
                            Spacer(Modifier.height(style.beforeLinks))
                        }
                        if (info != null) CodeLinks(info, state, onAction, onChangeNumberClick)
                    }
                }
                if (posture.isTabletop) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
context(style: CodeScreenStyleScope)
private fun CodeHeader(
    info: AuthenticationCodeInfo,
    onChangeNumberClick: () -> Unit,
) {
    Text(text = stringResource(info.title), style = style.titleTextStyle)
    Spacer(Modifier.height(style.beforeInfo))
    Text(
        text = annotatedStringResource(
            info.description,
            linkStyles = TextLinkStyles(SpanStyle(color = style.linkColor)),
            onLinkClick = { onChangeNumberClick() },
        ),
        style = style.infoTextStyle,
        modifier = Modifier.widthIn(max = style.infoWidth),
    )
}

@Composable
private fun CodeInput(
    info: AuthenticationCodeInfo?,
    code: TextFieldState,
    error: AuthError?,
    onAction: (CodeScreenAction) -> Unit,
    focusRequester: FocusRequester,
) {
    val length = info?.type?.length ?: 0
    if (length > 0) {
        CodeField(
            state = code,
            length = length,
            modifier = Modifier.focusRequester(focusRequester),
            isError = error != null,
            onKeyboardAction = { onAction(CodeScreenAction.OnSubmit) },
        )
    } else {
        TextField(
            state = code,
            modifier = Modifier.focusRequester(focusRequester),
            placeholder = { Text(stringResource(Res.string.login_header_code)) },
            isError = error != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onKeyboardAction = { onAction(CodeScreenAction.OnSubmit) },
            lineLimits = TextFieldLineLimits.SingleLine,
        )
    }
}

@Composable
private fun CodeLinks(
    info: AuthenticationCodeInfo,
    state: CodeScreenState,
    onAction: (CodeScreenAction) -> Unit,
    onChangeNumberClick: () -> Unit,
) {
    val nextType = info.nextType
    if (nextType != null) {
        TextButton(onClick = {
            onAction(CodeScreenAction.OnResend)
        }, state = ButtonState.of(enabled = state.canResend)) {
            Text(
                if (state.canResend) {
                    stringResource(Res.string.login_sendsmsifnotreceivedappcode)
                } else {
                    val countdown = if (nextType is AuthenticationCodeTypeCall) {
                        Res.string.login_willcall
                    } else {
                        Res.string.login_willsendsms
                    }
                    stringResource(
                        countdown,
                        state.resendSecondsLeft / SECONDS_PER_MINUTE,
                        (state.resendSecondsLeft % SECONDS_PER_MINUTE).toString().padStart(2, '0'),
                    )
                },
            )
        }
    }
    if (info.type is AuthenticationCodeTypeSmsWord ||
        info.type is AuthenticationCodeTypeSmsPhrase
    ) {
        TextButton(onClick = onChangeNumberClick) {
            Text(stringResource(Res.string.editaccount_changenumber))
        }
    }
}

private const val SECONDS_PER_MINUTE = 60
private const val PIRATE_FLAG = "files/pirate_flag.lottie"

@KotogramPreview
@Composable
private fun CodeScreenPreview(@PreviewParameter(CodeScreenStateProvider::class) state: CodeScreenState) {
    CodeScreenContent(state = state, code = TextFieldState("12"), onAction = {
    }, onChangeNumberClick = {})
}

@KotogramLaptopPreview
@Composable
private fun CodeScreenLaptopPreview(@PreviewParameter(CodeScreenStateProvider::class) state: CodeScreenState) =
    CodeScreenPreview(state)

private class CodeScreenStateProvider : PreviewParameterProvider<CodeScreenState> {
    private val info = AuthenticationCodeInfo(
        phoneNumber = "+998 90 123 45 67",
        type = AuthenticationCodeTypeTelegramMessage(length = 5),
        nextType = AuthenticationCodeTypeSms(length = 5),
        timeout = 60,
    )
    override val values = sequenceOf(
        CodeScreenState(info = info, resendSecondsLeft = 45),
        CodeScreenState(
            info = info.copy(
                type = AuthenticationCodeTypeSms(length = 5),
                nextType = AuthenticationCodeTypeCall(length = 5),
            ),
        ),
        CodeScreenState(info = info.copy(nextType = null), error = AuthError.InvalidCode),
    )
}
