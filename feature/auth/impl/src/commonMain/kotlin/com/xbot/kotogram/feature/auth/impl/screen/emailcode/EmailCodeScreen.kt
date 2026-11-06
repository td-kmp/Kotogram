package com.xbot.kotogram.feature.auth.impl.screen.emailcode

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
import com.xbot.kotogram.core.resources.email_code_subtitle
import com.xbot.kotogram.core.resources.email_code_title
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun EmailCodeScreen(viewModel: EmailCodeViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.collectAsState()
    EmailCodeScreenContent(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@Composable
private fun EmailCodeScreenContent(
    state: EmailCodeScreenState,
    onAction: (EmailCodeScreenAction) -> Unit,
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
                text = stringResource(Res.string.email_code_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(Res.string.email_code_subtitle, state.emailPattern),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            OutlinedTextField(
                value = state.code,
                onValueChange = { onAction(EmailCodeScreenAction.OnCodeChange(it)) },
                placeholder = { Text(stringResource(Res.string.code_hint)) },
                singleLine = true,
                isError = state.error != null,
                enabled = !state.isSubmitting,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    onAction(EmailCodeScreenAction.OnSubmit)
                }),
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
                onClick = { onAction(EmailCodeScreenAction.OnSubmit) },
                enabled = state.code.isNotEmpty() && !state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.action_continue))
            }
        }
    }
}

@KotogramPreview
@Composable
private fun EmailCodeScreenPreview(
    @PreviewParameter(EmailCodeScreenStateProvider::class) state: EmailCodeScreenState
) {
    EmailCodeScreenContent(state = state, onAction = {})
}

private class EmailCodeScreenStateProvider : PreviewParameterProvider<EmailCodeScreenState> {
    private val sent = EmailCodeScreenState(emailPattern = "j*****@g****.com", codeLength = 6)

    override val values = sequenceOf(
        sent,
        sent.copy(code = "123456", error = AuthError.InvalidCode)
    )
}
