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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.ButtonState
import com.xbot.kotogram.core.designsystem.component.ProminentButton
import com.xbot.kotogram.core.designsystem.component.TextButton
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.annotatedStringResource
import com.xbot.kotogram.core.resources.login_new_register_footer
import com.xbot.kotogram.core.resources.login_new_register_header
import com.xbot.kotogram.core.resources.login_new_register_info
import com.xbot.kotogram.core.resources.login_new_register_next
import com.xbot.kotogram.core.resources.login_register_firstname_placeholder
import com.xbot.kotogram.core.resources.login_register_lastname_placeholder
import com.xbot.kotogram.core.resources.navigation_back
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.core.resources.termsofservice_title
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
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
                text = stringResource(Res.string.login_new_register_header),
                style = KotogramTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(Res.string.login_new_register_info),
                style = KotogramTheme.typography.bodyMedium,
                color = KotogramTheme.colors.onSurfaceVariant,
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
                placeholder = { Text(stringResource(Res.string.login_register_firstname_placeholder)) },
                singleLine = true,
                keyboardOptions = nameOptions,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.lastName,
                onValueChange = { onAction(RegistrationScreenAction.OnLastNameChange(it)) },
                placeholder = { Text(stringResource(Res.string.login_register_lastname_placeholder)) },
                singleLine = true,
                keyboardOptions = nameOptions.copy(imeAction = ImeAction.Done),
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
                text = stringResource(Res.string.login_new_register_next),
                onClick = { onAction(RegistrationScreenAction.OnSubmit) },
                state = ButtonState.of(enabled = state.canSubmit, loading = state.isSubmitting),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = annotatedStringResource(
                    Res.string.login_new_register_footer,
                    linkStyles = TextLinkStyles(SpanStyle(color = KotogramTheme.colors.primary)),
                    onLinkClick = { onAction(RegistrationScreenAction.OnTermsVisibilityChange(true)) }
                ),
                style = KotogramTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }

    if (state.isTermsVisible) {
        AlertDialog(
            onDismissRequest = {
                onAction(RegistrationScreenAction.OnTermsVisibilityChange(false))
            },
            title = { Text(stringResource(Res.string.termsofservice_title)) },
            text = { Text(state.termsOfService, Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = {
                TextButton(onClick = {
                    onAction(RegistrationScreenAction.OnTermsVisibilityChange(false))
                }) { Text(stringResource(Res.string.navigation_back)) }
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
