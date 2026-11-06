package com.xbot.kotogram.feature.auth.impl.screen.emailaddress

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
import com.xbot.kotogram.feature.auth.impl.resources.email_placeholder
import com.xbot.kotogram.feature.auth.impl.resources.email_subtitle
import com.xbot.kotogram.feature.auth.impl.resources.email_title
import org.jetbrains.compose.resources.stringResource
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
    AuthScaffold(
        title = stringResource(Res.string.email_title),
        subtitle = { AuthSubtitle(stringResource(Res.string.email_subtitle)) },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = state.email,
            onValueChange = { onAction(EmailAddressScreenAction.OnEmailChange(it)) },
            placeholder = { Text(stringResource(Res.string.email_placeholder)) },
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
        AuthErrorText(state.error?.toUiText()?.asString())
        Button(
            onClick = { onAction(EmailAddressScreenAction.OnSubmit) },
            enabled = state.email.isNotEmpty() && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.action_continue))
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
