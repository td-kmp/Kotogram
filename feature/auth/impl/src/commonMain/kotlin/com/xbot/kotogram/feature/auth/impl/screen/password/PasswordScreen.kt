package com.xbot.kotogram.feature.auth.impl.screen.password

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.login_header_password
import com.xbot.kotogram.core.resources.login_new_password_info
import com.xbot.kotogram.core.resources.login_next
import com.xbot.kotogram.core.resources.login_passwordforgot
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.core.resources.twostepauth_enterpasswordhint
import com.xbot.kotogram.core.resources.twostepauth_enterpasswordpassword
import com.xbot.kotogram.core.resources.twostepauth_recoverycode
import com.xbot.kotogram.core.resources.twostepauth_recoverysent
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun PasswordScreen(viewModel: PasswordViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.collectAsState()
    PasswordScreenContent(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@Composable
private fun PasswordScreenContent(
    state: PasswordScreenState,
    onAction: (PasswordScreenAction) -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = stringResource(Res.string.login_header_password),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (state.isRecovering) {
                    stringResource(Res.string.twostepauth_recoverysent, state.recoveryEmailPattern)
                } else {
                    stringResource(Res.string.login_new_password_info)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (state.isRecovering) {
                OutlinedTextField(
                    value = state.recoveryCode,
                    onValueChange = { onAction(PasswordScreenAction.OnRecoveryCodeChange(it)) },
                    placeholder = { Text(stringResource(Res.string.twostepauth_recoverycode)) },
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
                    placeholder = { Text(stringResource(Res.string.twostepauth_enterpasswordpassword)) },
                    supportingText = state.hint.takeIf { it.isNotEmpty() }
                        ?.let { { Text(stringResource(Res.string.twostepauth_enterpasswordhint, it)) } },
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
            state.error?.let { error ->
                Text(
                    text = stringResource(error.localizedMessage()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = { onAction(PasswordScreenAction.OnSubmit) },
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.login_next))
            }
            if (!state.isRecovering && state.hasRecoveryEmail) {
                TextButton(onClick = { onAction(PasswordScreenAction.OnForgotPassword) }) {
                    Text(stringResource(Res.string.login_passwordforgot))
                }
            }
        }
    }
}

@KotogramPreview
@Composable
private fun PasswordScreenPreview(
    @PreviewParameter(PasswordScreenStateProvider::class) state: PasswordScreenState
) {
    PasswordScreenContent(state = state, onAction = {})
}

private class PasswordScreenStateProvider : PreviewParameterProvider<PasswordScreenState> {
    private val protected = PasswordScreenState(
        hint = "first pet",
        hasRecoveryEmail = true,
        recoveryEmailPattern = "j*****@g****.com"
    )

    override val values = sequenceOf(
        protected,
        protected.copy(password = "password", error = AuthError.InvalidPassword),
        protected.copy(isRecovering = true, recoveryCode = "123")
    )
}
