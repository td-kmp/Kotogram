package com.xbot.kotogram.feature.auth.impl.screen.emailaddress

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.isSpecified
import com.xbot.kotogram.core.designsystem.KotogramLaptopPreview
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.ProminentButton
import com.xbot.kotogram.core.designsystem.component.TextField
import com.xbot.kotogram.core.designsystem.media.derivedMediaQuery
import com.xbot.kotogram.core.designsystem.media.mediaQuery
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.authemailsetup_description
import com.xbot.kotogram.core.resources.authemailsetup_placeholder
import com.xbot.kotogram.core.resources.authemailsetup_title
import com.xbot.kotogram.core.resources.login_next
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.isBook
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun EmailAddressScreen(viewModel: EmailAddressViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.collectAsState()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    EmailAddressScreenContent(
        state = state,
        email = viewModel.email,
        onAction = viewModel::onAction,
        modifier = modifier,
        focusRequester = focusRequester
    )
}

@Composable
internal fun EmailAddressScreenContent(
    state: EmailAddressScreenState,
    email: TextFieldState,
    onAction: (EmailAddressScreenAction) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val style = rememberStyle(::EmailAddressScreenStyleScope, EmailAddressScreenStyle.Adaptive)
    val posture = mediaQuery { windowPosture }
    val landscape by derivedMediaQuery { windowHeight < style.landscapeHeight }
    // At the bottom as Telegram for iOS has it; under the field on laptops and phones on their side.
    val actionAtBottom = !style.mediaQueryInfo.isLaptop && !landscape
    context(style) {
        Scaffold(
            modifier = modifier,
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
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
                        Text(
                            text = stringResource(Res.string.authemailsetup_title),
                            style = style.titleTextStyle
                        )
                        Spacer(Modifier.height(style.beforeInfo))
                        Text(
                            text = stringResource(Res.string.authemailsetup_description),
                            style = style.infoTextStyle,
                            modifier = Modifier.widthIn(max = style.infoWidth)
                        )
                        Spacer(Modifier.height(style.beforeField))
                        TextField(
                            state = email,
                            modifier = Modifier.focusRequester(focusRequester).contentWidth(),
                            placeholder = {
                                Text(stringResource(Res.string.authemailsetup_placeholder))
                            },
                            isError = state.error != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            onKeyboardAction = { onAction(EmailAddressScreenAction.OnSubmit) },
                            lineLimits = TextFieldLineLimits.SingleLine
                        )
                        Crossfade(
                            targetState = state.error,
                            animationSpec = style.errorFadeSpec,
                            modifier = Modifier
                                .height(style.afterField)
                                .padding(top = style.beforeError)
                        ) { error ->
                            if (error != null) {
                                Text(
                                    text = stringResource(error.localizedMessage()),
                                    style = style.errorTextStyle
                                )
                            }
                        }
                        if (!actionAtBottom) EmailAddressAction(state, email, onAction)
                    }
                }
                // Half open on a table: the form above the fold, the keyboard below it.
                if (posture.isTabletop) Spacer(Modifier.weight(1f))
                if (actionAtBottom) {
                    EmailAddressAction(
                        state = state,
                        email = email,
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
context(style: EmailAddressScreenStyleScope)
private fun EmailAddressAction(
    state: EmailAddressScreenState,
    email: TextFieldState,
    onAction: (EmailAddressScreenAction) -> Unit,
    modifier: Modifier = Modifier
) {
    ProminentButton(
        text = stringResource(Res.string.login_next),
        onClick = { onAction(EmailAddressScreenAction.OnSubmit) },
        state = ButtonState.of(enabled = email.text.isNotBlank(), loading = state.isSubmitting),
        modifier = modifier.contentWidth()
    )
}

context(style: EmailAddressScreenStyleScope)
private fun Modifier.contentWidth(): Modifier = if (style.contentMaxWidth.isSpecified) {
    widthIn(
        max = style.contentMaxWidth
    ).fillMaxWidth()
} else {
    this
}

@KotogramPreview
@Composable
private fun EmailAddressScreenPreview(
    @PreviewParameter(EmailAddressScreenStateProvider::class) state: EmailAddressScreenState
) {
    EmailAddressScreenContent(state = state, email = TextFieldState("name@example"), onAction = {})
}

@KotogramLaptopPreview
@Composable
private fun EmailAddressScreenLaptopPreview(
    @PreviewParameter(EmailAddressScreenStateProvider::class) state: EmailAddressScreenState
) = EmailAddressScreenPreview(state)

private class EmailAddressScreenStateProvider : PreviewParameterProvider<EmailAddressScreenState> {
    override val values = sequenceOf(
        EmailAddressScreenState(),
        EmailAddressScreenState(error = AuthError.InvalidEmail)
    )
}
