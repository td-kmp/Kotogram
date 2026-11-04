package com.xbot.kotogram.feature.auth.presentation.screen.registration

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.presentation.component.AuthErrorText
import com.xbot.kotogram.feature.auth.presentation.component.AuthScaffold
import com.xbot.kotogram.feature.auth.presentation.component.AuthSubtitle
import com.xbot.kotogram.feature.auth.presentation.component.asString
import com.xbot.kotogram.feature.auth.presentation.component.toUiText
import com.xbot.kotogram.feature.auth.presentation.resources.Res
import com.xbot.kotogram.feature.auth.presentation.resources.action_back
import com.xbot.kotogram.feature.auth.presentation.resources.action_continue
import com.xbot.kotogram.feature.auth.presentation.resources.first_name
import com.xbot.kotogram.feature.auth.presentation.resources.last_name
import com.xbot.kotogram.feature.auth.presentation.resources.registration_subtitle
import com.xbot.kotogram.feature.auth.presentation.resources.registration_title
import com.xbot.kotogram.feature.auth.presentation.resources.tos_accept
import com.xbot.kotogram.feature.auth.presentation.resources.tos_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun RegistrationScreen(
    modifier: Modifier = Modifier,
    viewModel: RegistrationViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    RegistrationScreenContent(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@Composable
private fun RegistrationScreenContent(
    state: RegistrationScreenState,
    onAction: (RegistrationScreenAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScaffold(
        title = stringResource(Res.string.registration_title),
        subtitle = { AuthSubtitle(stringResource(Res.string.registration_subtitle)) },
        modifier = modifier
    ) {
        val nameOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        OutlinedTextField(
            value = state.firstName,
            onValueChange = { onAction(RegistrationScreenAction.OnFirstNameChange(it)) },
            placeholder = { Text(stringResource(Res.string.first_name)) },
            singleLine = true,
            keyboardOptions = nameOptions,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.lastName,
            onValueChange = { onAction(RegistrationScreenAction.OnLastNameChange(it)) },
            placeholder = { Text(stringResource(Res.string.last_name)) },
            singleLine = true,
            keyboardOptions = nameOptions.copy(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
        AuthErrorText(state.error?.toUiText()?.asString())
        Button(
            onClick = { onAction(RegistrationScreenAction.OnSubmit) },
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.action_continue))
        }
        TextButton(onClick = {
            onAction(RegistrationScreenAction.OnTermsVisibilityChange(true))
        }) { Text(stringResource(Res.string.tos_accept)) }
    }

    if (state.isTermsVisible) {
        AlertDialog(
            onDismissRequest = {
                onAction(RegistrationScreenAction.OnTermsVisibilityChange(false))
            },
            title = { Text(stringResource(Res.string.tos_title)) },
            text = { Text(state.termsOfService, Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = {
                TextButton(onClick = {
                    onAction(RegistrationScreenAction.OnTermsVisibilityChange(false))
                }) { Text(stringResource(Res.string.action_back)) }
            }
        )
    }
}

@KotogramPreview
@Composable
private fun RegistrationScreenPreview(
    @PreviewParameter(RegistrationScreenStateProvider::class) state: RegistrationScreenState
) {
    RegistrationScreenContent(state = state, onAction = {})
}

private class RegistrationScreenStateProvider : PreviewParameterProvider<RegistrationScreenState> {
    override val values = sequenceOf(
        RegistrationScreenState(),
        RegistrationScreenState(firstName = "Ivan", lastName = "Petrov"),
        RegistrationScreenState(firstName = " ", error = AuthError.InvalidName)
    )
}
