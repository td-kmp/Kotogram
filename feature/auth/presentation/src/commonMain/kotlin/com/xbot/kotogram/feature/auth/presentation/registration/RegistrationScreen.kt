package com.xbot.kotogram.feature.auth.presentation.registration

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
import com.xbot.kotogram.feature.auth.presentation.common.AuthErrorText
import com.xbot.kotogram.feature.auth.presentation.common.AuthScaffold
import com.xbot.kotogram.feature.auth.presentation.common.AuthSubtitle
import com.xbot.kotogram.feature.auth.presentation.common.asString
import com.xbot.kotogram.feature.auth.presentation.common.toUiText
import com.xbot.kotogram.feature.auth.presentation.resources.Res
import com.xbot.kotogram.feature.auth.presentation.resources.action_back
import com.xbot.kotogram.feature.auth.presentation.resources.action_continue
import com.xbot.kotogram.feature.auth.presentation.resources.first_name
import com.xbot.kotogram.feature.auth.presentation.resources.last_name
import com.xbot.kotogram.feature.auth.presentation.resources.registration_subtitle
import com.xbot.kotogram.feature.auth.presentation.resources.registration_title
import com.xbot.kotogram.feature.auth.presentation.resources.tos_accept
import com.xbot.kotogram.feature.auth.presentation.resources.tos_title
import com.xbot.kotogram.feature.auth.presentation.resources.unsupported_subtitle
import com.xbot.kotogram.feature.auth.presentation.resources.unsupported_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun RegistrationRoute(viewModel: RegistrationViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    RegistrationScreen(
        state = state,
        onFirstNameChange = viewModel::onFirstNameChange,
        onLastNameChange = viewModel::onLastNameChange,
        onTermsVisibilityChange = viewModel::onTermsVisibilityChange,
        onSubmit = viewModel::onSubmit
    )
}

@Composable
internal fun RegistrationScreen(
    state: RegistrationState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onTermsVisibilityChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
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
            onValueChange = onFirstNameChange,
            placeholder = { Text(stringResource(Res.string.first_name)) },
            singleLine = true,
            keyboardOptions = nameOptions,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.lastName,
            onValueChange = onLastNameChange,
            placeholder = { Text(stringResource(Res.string.last_name)) },
            singleLine = true,
            keyboardOptions = nameOptions.copy(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
        AuthErrorText(state.error?.toUiText()?.asString())
        Button(onClick = onSubmit, enabled = state.canSubmit, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.action_continue))
        }
        TextButton(onClick = {
            onTermsVisibilityChange(true)
        }) { Text(stringResource(Res.string.tos_accept)) }
    }

    if (state.isTermsVisible) {
        AlertDialog(
            onDismissRequest = { onTermsVisibilityChange(false) },
            title = { Text(stringResource(Res.string.tos_title)) },
            text = { Text(state.termsOfService, Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = {
                TextButton(onClick = {
                    onTermsVisibilityChange(false)
                }) { Text(stringResource(Res.string.action_back)) }
            }
        )
    }
}

@Composable
internal fun UnsupportedScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    AuthScaffold(
        title = stringResource(Res.string.unsupported_title),
        subtitle = { AuthSubtitle(stringResource(Res.string.unsupported_subtitle)) },
        modifier = modifier
    ) {
        TextButton(onClick = onBack) { Text(stringResource(Res.string.action_back)) }
    }
}
