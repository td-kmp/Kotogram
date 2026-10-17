package com.xbot.kotogram.feature.auth.presentation.screen.code

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.xbot.kotogram.core.domain.model.AuthenticationCodeType
import com.xbot.kotogram.feature.auth.presentation.component.AuthErrorText
import com.xbot.kotogram.feature.auth.presentation.component.AuthScaffold
import com.xbot.kotogram.feature.auth.presentation.component.AuthSubtitle
import com.xbot.kotogram.feature.auth.presentation.component.asString
import com.xbot.kotogram.feature.auth.presentation.component.toUiText
import com.xbot.kotogram.feature.auth.presentation.resources.Res
import com.xbot.kotogram.feature.auth.presentation.resources.action_continue
import com.xbot.kotogram.feature.auth.presentation.resources.code_hint
import com.xbot.kotogram.feature.auth.presentation.resources.code_resend
import com.xbot.kotogram.feature.auth.presentation.resources.code_resend_in
import com.xbot.kotogram.feature.auth.presentation.resources.code_sent_call
import com.xbot.kotogram.feature.auth.presentation.resources.code_sent_flash_call
import com.xbot.kotogram.feature.auth.presentation.resources.code_sent_fragment
import com.xbot.kotogram.feature.auth.presentation.resources.code_sent_missed_call
import com.xbot.kotogram.feature.auth.presentation.resources.code_sent_sms
import com.xbot.kotogram.feature.auth.presentation.resources.code_sent_sms_phrase
import com.xbot.kotogram.feature.auth.presentation.resources.code_sent_sms_word
import com.xbot.kotogram.feature.auth.presentation.resources.code_sent_telegram
import com.xbot.kotogram.feature.auth.presentation.resources.wrong_number
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun CodeScreen(
    onChangeNumberClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CodeViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    CodeScreenContent(
        state = state,
        onAction = viewModel::onAction,
        onChangeNumberClick = onChangeNumberClick,
        modifier = modifier
    )
}

@Composable
private fun CodeScreenContent(
    state: CodeScreenState,
    onAction: (CodeScreenAction) -> Unit,
    onChangeNumberClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val info = state.info
    AuthScaffold(
        title = info?.phoneNumber.orEmpty(),
        subtitle = { if (info != null) AuthSubtitle(codeDescription(info.type, info.phoneNumber)) },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = state.code,
            onValueChange = { onAction(CodeScreenAction.OnCodeChange(it)) },
            placeholder = { Text(stringResource(Res.string.code_hint)) },
            singleLine = true,
            isError = state.error != null,
            enabled = !state.isSubmitting,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onAction(CodeScreenAction.OnSubmit) }),
            modifier = Modifier.fillMaxWidth()
        )
        AuthErrorText(state.error?.toUiText()?.asString())
        Button(
            onClick = { onAction(CodeScreenAction.OnSubmit) },
            enabled = state.code.isNotEmpty() && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.action_continue))
        }
        if (info?.nextType != null) {
            TextButton(onClick = {
                onAction(CodeScreenAction.OnResend)
            }, enabled = state.canResend) {
                Text(
                    if (state.canResend) {
                        stringResource(Res.string.code_resend)
                    } else {
                        stringResource(
                            Res.string.code_resend_in,
                            formatSeconds(state.resendSecondsLeft)
                        )
                    }
                )
            }
        }
        TextButton(onClick = onChangeNumberClick) { Text(stringResource(Res.string.wrong_number)) }
    }
}

@Composable
private fun codeDescription(type: AuthenticationCodeType, phoneNumber: String) = when (type) {
    is AuthenticationCodeType.TelegramMessage -> stringResource(Res.string.code_sent_telegram)

    is AuthenticationCodeType.Sms,
    is AuthenticationCodeType.FirebaseAndroid,
    is AuthenticationCodeType.FirebaseIos -> stringResource(Res.string.code_sent_sms, phoneNumber)

    is AuthenticationCodeType.SmsWord -> stringResource(
        Res.string.code_sent_sms_word,
        phoneNumber,
        type.firstLetter
    )

    is AuthenticationCodeType.SmsPhrase -> stringResource(
        Res.string.code_sent_sms_phrase,
        phoneNumber,
        type.firstWord
    )

    is AuthenticationCodeType.Call -> stringResource(Res.string.code_sent_call, phoneNumber)

    is AuthenticationCodeType.FlashCall ->
        stringResource(Res.string.code_sent_flash_call, phoneNumber)

    is AuthenticationCodeType.MissedCall -> stringResource(
        Res.string.code_sent_missed_call,
        type.phoneNumberPrefix,
        type.length
    )

    is AuthenticationCodeType.Fragment -> stringResource(Res.string.code_sent_fragment)
}

private const val SECONDS_PER_MINUTE = 60

private fun formatSeconds(seconds: Int): String =
    "${seconds / SECONDS_PER_MINUTE}:${(seconds % SECONDS_PER_MINUTE).toString().padStart(2, '0')}"
