package com.xbot.kotogram.feature.auth.impl.screen.registration

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.action_back
import com.xbot.kotogram.core.resources.action_continue
import com.xbot.kotogram.core.resources.first_name
import com.xbot.kotogram.core.resources.last_name
import com.xbot.kotogram.core.resources.registration_subtitle
import com.xbot.kotogram.core.resources.registration_title
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.core.resources.tos_accept
import com.xbot.kotogram.core.resources.tos_title
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
import org.jetbrains.compose.resources.stringResource
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun RegistrationScreen(viewModel: RegistrationViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.collectAsState()
    RegistrationScreenContent(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@Composable
private fun RegistrationScreenContent(
    state: RegistrationScreenState,
    onAction: (RegistrationScreenAction) -> Unit,
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
                text = stringResource(Res.string.registration_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(Res.string.registration_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
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
            state.error?.let { error ->
                Text(
                    text = stringResource(error.localizedMessage()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
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
