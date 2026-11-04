package com.xbot.kotogram.feature.auth.impl.screen.code

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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.component.AuthErrorText
import com.xbot.kotogram.feature.auth.impl.component.AuthScaffold
import com.xbot.kotogram.feature.auth.impl.component.AuthSubtitle
import com.xbot.kotogram.feature.auth.impl.extensions.asString
import com.xbot.kotogram.feature.auth.impl.extensions.length
import com.xbot.kotogram.feature.auth.impl.extensions.toUiText
import com.xbot.kotogram.feature.auth.impl.resources.Res
import com.xbot.kotogram.feature.auth.impl.resources.action_continue
import com.xbot.kotogram.feature.auth.impl.resources.code_hint
import com.xbot.kotogram.feature.auth.impl.resources.code_resend
import com.xbot.kotogram.feature.auth.impl.resources.code_resend_in
import com.xbot.kotogram.feature.auth.impl.resources.code_sent_call
import com.xbot.kotogram.feature.auth.impl.resources.code_sent_flash_call
import com.xbot.kotogram.feature.auth.impl.resources.code_sent_fragment
import com.xbot.kotogram.feature.auth.impl.resources.code_sent_missed_call
import com.xbot.kotogram.feature.auth.impl.resources.code_sent_sms
import com.xbot.kotogram.feature.auth.impl.resources.code_sent_sms_phrase
import com.xbot.kotogram.feature.auth.impl.resources.code_sent_sms_word
import com.xbot.kotogram.feature.auth.impl.resources.code_sent_telegram
import com.xbot.kotogram.feature.auth.impl.resources.wrong_number
import io.xbot.tdlib.api.AuthenticationCodeInfo
import io.xbot.tdlib.api.AuthenticationCodeType
import io.xbot.tdlib.api.AuthenticationCodeTypeCall
import io.xbot.tdlib.api.AuthenticationCodeTypeFirebaseAndroid
import io.xbot.tdlib.api.AuthenticationCodeTypeFirebaseIos
import io.xbot.tdlib.api.AuthenticationCodeTypeFlashCall
import io.xbot.tdlib.api.AuthenticationCodeTypeFragment
import io.xbot.tdlib.api.AuthenticationCodeTypeMissedCall
import io.xbot.tdlib.api.AuthenticationCodeTypeSms
import io.xbot.tdlib.api.AuthenticationCodeTypeSmsPhrase
import io.xbot.tdlib.api.AuthenticationCodeTypeSmsWord
import io.xbot.tdlib.api.AuthenticationCodeTypeTelegramMessage
import kotlin.time.Duration.Companion.seconds
import org.jetbrains.compose.resources.stringResource
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun CodeScreen(
    viewModel: CodeViewModel,
    onChangeNumberClick: () -> Unit,
    modifier: Modifier = Modifier
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
    is AuthenticationCodeTypeTelegramMessage -> stringResource(Res.string.code_sent_telegram)

    is AuthenticationCodeTypeSms,
    is AuthenticationCodeTypeFirebaseAndroid,
    is AuthenticationCodeTypeFirebaseIos -> stringResource(Res.string.code_sent_sms, phoneNumber)

    is AuthenticationCodeTypeSmsWord -> stringResource(
        Res.string.code_sent_sms_word,
        phoneNumber,
        type.firstLetter
    )

    is AuthenticationCodeTypeSmsPhrase -> stringResource(
        Res.string.code_sent_sms_phrase,
        phoneNumber,
        type.firstWord
    )

    is AuthenticationCodeTypeCall -> stringResource(Res.string.code_sent_call, phoneNumber)

    is AuthenticationCodeTypeFlashCall ->
        stringResource(Res.string.code_sent_flash_call, phoneNumber)

    is AuthenticationCodeTypeMissedCall -> stringResource(
        Res.string.code_sent_missed_call,
        type.phoneNumberPrefix,
        type.length
    )

    is AuthenticationCodeTypeFragment -> stringResource(Res.string.code_sent_fragment)
}

private const val SECONDS_PER_MINUTE = 60

private fun formatSeconds(seconds: Int): String =
    "${seconds / SECONDS_PER_MINUTE}:${(seconds % SECONDS_PER_MINUTE).toString().padStart(2, '0')}"

@KotogramPreview
@Composable
private fun CodeScreenPreview(
    @PreviewParameter(CodeScreenStateProvider::class) state: CodeScreenState
) {
    CodeScreenContent(state = state, onAction = {}, onChangeNumberClick = {})
}

private class CodeScreenStateProvider : PreviewParameterProvider<CodeScreenState> {
    private val info = AuthenticationCodeInfo(
        phoneNumber = "+998 90 123 45 67",
        type = AuthenticationCodeTypeTelegramMessage(length = 5),
        nextType = AuthenticationCodeTypeSms(length = 5),
        timeout = 60
    )

    override val values = sequenceOf(
        CodeScreenState(info = info, resendSecondsLeft = 45),
        CodeScreenState(
            info = info.copy(
                type = AuthenticationCodeTypeSms(length = 5),
                nextType = AuthenticationCodeTypeCall(length = 5)
            ),
            code = "12"
        ),
        CodeScreenState(
            info = info.copy(nextType = null),
            code = "11111",
            error = AuthError.InvalidCode
        )
    )
}
