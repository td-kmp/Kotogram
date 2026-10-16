package com.xbot.kotogram.feature.auth.presentation.password

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
import com.xbot.kotogram.feature.auth.presentation.common.AuthErrorText
import com.xbot.kotogram.feature.auth.presentation.common.AuthScaffold
import com.xbot.kotogram.feature.auth.presentation.common.AuthSubtitle
import com.xbot.kotogram.feature.auth.presentation.common.asString
import com.xbot.kotogram.feature.auth.presentation.common.toUiText
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
internal fun PasswordRoute(viewModel: PasswordViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    PasswordScreen(
        state = state,
        onPasswordChange = viewModel::onPasswordChange,
        onRecoveryCodeChange = viewModel::onRecoveryCodeChange,
        onSubmit = viewModel::onSubmit,
        onForgotPassword = viewModel::onForgotPassword
    )
}

@Composable
internal fun PasswordScreen(
    state: PasswordState,
    onPasswordChange: (String) -> Unit,
    onRecoveryCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
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
                onValueChange = onRecoveryCodeChange,
                placeholder = { Text(stringResource(Res.string.password_recovery_code)) },
                singleLine = true,
                isError = state.error != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
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
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        AuthErrorText(state.error?.toUiText()?.asString())
        Button(
            onClick = onSubmit,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.action_continue))
        }
        if (!state.isRecovering && state.hasRecoveryEmail) {
            TextButton(onClick = onForgotPassword) {
                Text(stringResource(Res.string.password_forgot))
            }
        }
    }
}
