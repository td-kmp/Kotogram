package com.xbot.kotogram.feature.auth.impl.screen.emailcode

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.xbot.kotogram.feature.auth.impl.extensions.toUiText
import com.xbot.kotogram.feature.auth.impl.resources.Res
import com.xbot.kotogram.feature.auth.impl.resources.action_continue
import com.xbot.kotogram.feature.auth.impl.resources.code_hint
import com.xbot.kotogram.feature.auth.impl.resources.email_code_subtitle
import com.xbot.kotogram.feature.auth.impl.resources.email_code_title
import org.jetbrains.compose.resources.stringResource
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun EmailCodeScreen(
    viewModel: EmailCodeViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.collectAsState()
    EmailCodeScreenContent(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@Composable
private fun EmailCodeScreenContent(
    state: EmailCodeScreenState,
    onAction: (EmailCodeScreenAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScaffold(
        title = stringResource(Res.string.email_code_title),
        subtitle = {
            AuthSubtitle(stringResource(Res.string.email_code_subtitle, state.emailPattern))
        },
        modifier = modifier
    ) {
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
        AuthErrorText(state.error?.toUiText()?.asString())
        Button(
            onClick = { onAction(EmailCodeScreenAction.OnSubmit) },
            enabled = state.code.isNotEmpty() && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.action_continue))
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
