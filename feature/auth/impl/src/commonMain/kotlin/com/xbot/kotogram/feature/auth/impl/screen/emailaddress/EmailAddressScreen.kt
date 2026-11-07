package com.xbot.kotogram.feature.auth.impl.screen.emailaddress

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
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.ProminentButton
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.email_enter_placeholder
import com.xbot.kotogram.core.resources.login_next
import com.xbot.kotogram.core.resources.loginemail_description
import com.xbot.kotogram.core.resources.loginemail_title
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun EmailAddressScreen(viewModel: EmailAddressViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.collectAsState()
    EmailAddressScreenContent(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@Composable
private fun EmailAddressScreenContent(
    state: EmailAddressScreenState,
    onAction: (EmailAddressScreenAction) -> Unit,
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
                text = stringResource(Res.string.loginemail_title),
                style = KotogramTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(Res.string.loginemail_description),
                style = KotogramTheme.typography.bodyMedium,
                color = KotogramTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            OutlinedTextField(
                value = state.email,
                onValueChange = { onAction(EmailAddressScreenAction.OnEmailChange(it)) },
                placeholder = { Text(stringResource(Res.string.email_enter_placeholder)) },
                singleLine = true,
                isError = state.error != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    onAction(EmailAddressScreenAction.OnSubmit)
                }),
                modifier = Modifier.fillMaxWidth()
            )
            state.error?.let { error ->
                Text(
                    text = stringResource(error.localizedMessage()),
                    color = KotogramTheme.colors.error,
                    style = KotogramTheme.typography.bodySmall
                )
            }
            ProminentButton(
                text = stringResource(Res.string.login_next),
                onClick = { onAction(EmailAddressScreenAction.OnSubmit) },
                state = ButtonState.of(enabled = state.email.isNotEmpty(), loading = state.isSubmitting),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@KotogramPreview
@Composable
private fun EmailAddressScreenPreview(
    @PreviewParameter(EmailAddressScreenStateProvider::class) state: EmailAddressScreenState
) {
    EmailAddressScreenContent(state = state, onAction = {})
}

private class EmailAddressScreenStateProvider : PreviewParameterProvider<EmailAddressScreenState> {
    override val values = sequenceOf(
        EmailAddressScreenState(),
        EmailAddressScreenState(email = "name@example", error = AuthError.InvalidEmail)
    )
}
