package com.xbot.kotogram.feature.auth.presentation.screen.password

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.xbot.kotogram.feature.auth.presentation.component.AuthErrorText
import com.xbot.kotogram.feature.auth.presentation.component.AuthScaffold
import com.xbot.kotogram.feature.auth.presentation.component.AuthSubtitle
import com.xbot.kotogram.feature.auth.presentation.component.asString
import com.xbot.kotogram.feature.auth.presentation.component.toUiText
import com.xbot.kotogram.feature.auth.presentation.resources.Res
import com.xbot.kotogram.feature.auth.presentation.resources.action_continue
import com.xbot.kotogram.feature.auth.presentation.resources.password_forgot
import com.xbot.kotogram.feature.auth.presentation.resources.password_hint
import com.xbot.kotogram.feature.auth.presentation.resources.password_placeholder
import com.xbot.kotogram.feature.auth.presentation.resources.password_recovery_code
import com.xbot.kotogram.feature.auth.presentation.resources.password_recovery_sent
import com.xbot.kotogram.feature.auth.presentation.resources.password_subtitle
import com.xbot.kotogram.feature.auth.presentation.resources.password_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun PasswordScreen(
    modifier: Modifier = Modifier,
    viewModel: PasswordViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    PasswordScreenContent(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@Composable
private fun PasswordScreenContent(
    state: PasswordScreenState,
    onAction: (PasswordScreenAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScaffold(
        title = stringResource(Res.string.password_title),
        subtitle = {
            AuthSubtitle(
                if (state.isRecovering) {
                    stringResource(Res.string.password_recovery_sent, state.recoveryEmailPattern)
                } else {
                    stringResource(Res.string.password_subtitle)
                }
            )
        },
        modifier = modifier
    ) {
        if (state.isRecovering) {
            OutlinedTextField(
                value = state.recoveryCode,
                onValueChange = { onAction(PasswordScreenAction.OnRecoveryCodeChange(it)) },
                placeholder = { Text(stringResource(Res.string.password_recovery_code)) },
                singleLine = true,
                isError = state.error != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    onAction(PasswordScreenAction.OnSubmit)
                }),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = state.password,
                onValueChange = { onAction(PasswordScreenAction.OnPasswordChange(it)) },
                placeholder = { Text(stringResource(Res.string.password_placeholder)) },
                supportingText = state.hint.takeIf {
                    it.isNotEmpty()
                }?.let { { Text(stringResource(Res.string.password_hint, it)) } },
                singleLine = true,
                isError = state.error != null,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    onAction(PasswordScreenAction.OnSubmit)
                }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        AuthErrorText(state.error?.toUiText()?.asString())
        Button(
            onClick = { onAction(PasswordScreenAction.OnSubmit) },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.action_continue))
        }
        if (!state.isRecovering && state.hasRecoveryEmail) {
            TextButton(onClick = { onAction(PasswordScreenAction.OnForgotPassword) }) {
                Text(stringResource(Res.string.password_forgot))
            }
        }
    }
}
