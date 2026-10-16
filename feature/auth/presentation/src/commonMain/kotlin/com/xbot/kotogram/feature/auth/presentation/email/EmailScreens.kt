package com.xbot.kotogram.feature.auth.presentation.email

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
import com.xbot.kotogram.feature.auth.presentation.common.AuthErrorText
import com.xbot.kotogram.feature.auth.presentation.common.AuthScaffold
import com.xbot.kotogram.feature.auth.presentation.common.AuthSubtitle
import com.xbot.kotogram.feature.auth.presentation.common.asString
import com.xbot.kotogram.feature.auth.presentation.common.toUiText
import com.xbot.kotogram.feature.auth.presentation.resources.Res
import com.xbot.kotogram.feature.auth.presentation.resources.action_continue
import com.xbot.kotogram.feature.auth.presentation.resources.code_hint
import com.xbot.kotogram.feature.auth.presentation.resources.email_code_subtitle
import com.xbot.kotogram.feature.auth.presentation.resources.email_code_title
import com.xbot.kotogram.feature.auth.presentation.resources.email_placeholder
import com.xbot.kotogram.feature.auth.presentation.resources.email_subtitle
import com.xbot.kotogram.feature.auth.presentation.resources.email_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun EmailAddressRoute(viewModel: EmailAddressViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    EmailAddressScreen(state, viewModel::onEmailChange, viewModel::onSubmit)
}

@Composable
internal fun EmailAddressScreen(
    state: EmailAddressState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScaffold(
        title = stringResource(Res.string.email_title),
        subtitle = { AuthSubtitle(stringResource(Res.string.email_subtitle)) },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = { Text(stringResource(Res.string.email_placeholder)) },
            singleLine = true,
            isError = state.error != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            modifier = Modifier.fillMaxWidth()
        )
        AuthErrorText(state.error?.toUiText()?.asString())
        Button(
            onClick = onSubmit,
            enabled = state.email.isNotEmpty() && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.action_continue))
        }
    }
}

@Composable
internal fun EmailCodeRoute(viewModel: EmailCodeViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    EmailCodeScreen(state, viewModel::onCodeChange, viewModel::onSubmit)
}

@Composable
internal fun EmailCodeScreen(
    state: EmailCodeState,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
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
            onValueChange = onCodeChange,
            placeholder = { Text(stringResource(Res.string.code_hint)) },
            singleLine = true,
            isError = state.error != null,
            enabled = !state.isSubmitting,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            modifier = Modifier.fillMaxWidth()
        )
        AuthErrorText(state.error?.toUiText()?.asString())
        Button(
            onClick = onSubmit,
            enabled = state.code.isNotEmpty() && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.action_continue))
        }
    }
}
