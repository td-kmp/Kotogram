package com.xbot.kotogram.feature.auth.presentation.code

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
import com.xbot.kotogram.feature.auth.domain.model.CodeType
import com.xbot.kotogram.feature.auth.presentation.common.AuthErrorText
import com.xbot.kotogram.feature.auth.presentation.common.AuthScaffold
import com.xbot.kotogram.feature.auth.presentation.common.AuthSubtitle
import com.xbot.kotogram.feature.auth.presentation.common.asString
import com.xbot.kotogram.feature.auth.presentation.common.toUiText
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
internal fun CodeRoute(onChangeNumber: () -> Unit, viewModel: CodeViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    CodeScreen(
        state,
        viewModel::onCodeChange,
        viewModel::onSubmit,
        viewModel::onResend,
        onChangeNumber
    )
}

@Composable
internal fun CodeScreen(
    state: CodeState,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onResend: () -> Unit,
    onChangeNumber: () -> Unit,
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
            onValueChange = onCodeChange,
            placeholder = { Text(stringResource(Res.string.code_hint)) },
            singleLine = true,
            isError = state.error != null,
            enabled = !state.isSubmitting,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            modifier = Modifier.fillMaxWidth()
        )
        AuthErrorText(state.error?.toUiText()?.asString())
        Button(
            onClick = onSubmit,
            enabled = state.code.isNotEmpty() && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.action_continue))
        }
        if (info?.nextType != null) {
            TextButton(onClick = onResend, enabled = state.canResend) {
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
        TextButton(onClick = onChangeNumber) { Text(stringResource(Res.string.wrong_number)) }
    }
}

@Composable
private fun codeDescription(type: CodeType, phoneNumber: String): String = when (type) {
    is CodeType.TelegramMessage -> stringResource(Res.string.code_sent_telegram)

    is CodeType.Sms, is CodeType.Firebase -> stringResource(Res.string.code_sent_sms, phoneNumber)

    is CodeType.SmsWord -> stringResource(
        Res.string.code_sent_sms_word,
        phoneNumber,
        type.firstLetter
    )

    is CodeType.SmsPhrase -> stringResource(
        Res.string.code_sent_sms_phrase,
        phoneNumber,
        type.firstWord
    )

    is CodeType.Call -> stringResource(Res.string.code_sent_call, phoneNumber)

    is CodeType.FlashCall -> stringResource(Res.string.code_sent_flash_call, phoneNumber)

    is CodeType.MissedCall -> stringResource(
        Res.string.code_sent_missed_call,
        type.phoneNumberPrefix,
        type.length
    )

    is CodeType.Fragment -> stringResource(Res.string.code_sent_fragment)
}

private const val SECONDS_PER_MINUTE = 60

private fun formatSeconds(seconds: Int): String =
    "${seconds / SECONDS_PER_MINUTE}:${(seconds % SECONDS_PER_MINUTE).toString().padStart(2, '0')}"
