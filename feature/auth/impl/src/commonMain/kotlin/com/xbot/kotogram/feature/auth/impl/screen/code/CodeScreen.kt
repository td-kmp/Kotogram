package com.xbot.kotogram.feature.auth.impl.screen.code

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.action_continue
import com.xbot.kotogram.core.resources.code_hint
import com.xbot.kotogram.core.resources.code_resend
import com.xbot.kotogram.core.resources.code_resend_in
import com.xbot.kotogram.core.resources.extensions.description
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.core.resources.wrong_number
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
import io.xbot.tdlib.api.AuthenticationCodeInfo
import io.xbot.tdlib.api.AuthenticationCodeTypeCall
import io.xbot.tdlib.api.AuthenticationCodeTypeSms
import io.xbot.tdlib.api.AuthenticationCodeTypeTelegramMessage
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
    Scaffold(modifier = modifier, contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .wrapContentWidth()
                .widthIn(max = 420.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = info?.phoneNumber.orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            if (info != null) {
                Text(
                    text = stringResource(info.description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
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
            state.error?.let { error ->
                Text(
                    text = stringResource(error.localizedMessage()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = { onAction(CodeScreenAction.OnSubmit) },
                enabled = state.code.isNotEmpty() && !state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.action_continue))
            }
            if (info?.nextType != null) {
                TextButton(
                    onClick = { onAction(CodeScreenAction.OnResend) },
                    enabled = state.canResend
                ) {
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
            TextButton(onClick = onChangeNumberClick) {
                Text(stringResource(Res.string.wrong_number))
            }
        }
    }
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
