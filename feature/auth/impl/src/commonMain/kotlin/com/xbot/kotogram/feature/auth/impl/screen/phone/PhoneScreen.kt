package com.xbot.kotogram.feature.auth.impl.screen.phone

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.xbot.kotogram.core.resources.country_placeholder
import com.xbot.kotogram.core.resources.login_by_qr
import com.xbot.kotogram.core.resources.phone_hint
import com.xbot.kotogram.core.resources.phone_subtitle
import com.xbot.kotogram.core.resources.phone_title
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.core.resources.sync_contacts
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.impl.extensions.localizedMessage
import io.xbot.tdlib.api.CountryInfo
import org.jetbrains.compose.resources.stringResource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
internal fun PhoneScreen(
    viewModel: PhoneViewModel,
    canUseQr: Boolean,
    onSubmitted: () -> Unit,
    onQrLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { effect ->
        when (effect) {
            PhoneScreenSideEffect.Submitted -> onSubmitted()
        }
    }
    PhoneScreenContent(
        state = state,
        canUseQr = canUseQr,
        onAction = viewModel::onAction,
        onQrLoginClick = onQrLoginClick,
        modifier = modifier
    )
}

@Composable
private fun PhoneScreenContent(
    state: PhoneScreenState,
    canUseQr: Boolean,
    onAction: (PhoneScreenAction) -> Unit,
    onQrLoginClick: () -> Unit,
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
                text = stringResource(Res.string.phone_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(Res.string.phone_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            ListItem(
                headlineContent = {
                    Text(
                        state.country?.let { "${it.flagEmoji} ${it.name}" }
                            ?: stringResource(Res.string.country_placeholder)
                    )
                },
                modifier = Modifier.fillMaxWidth().clickable {
                    onAction(PhoneScreenAction.OnCountryPickerVisibilityChange(true))
                }
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = "+${state.callingCode}",
                    onValueChange = { onAction(PhoneScreenAction.OnCallingCodeChange(it)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.width(96.dp)
                )
                OutlinedTextField(
                    value = state.number,
                    onValueChange = { onAction(PhoneScreenAction.OnNumberChange(it)) },
                    placeholder = { Text(stringResource(Res.string.phone_hint)) },
                    singleLine = true,
                    isError = state.error != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        onAction(PhoneScreenAction.OnSubmit)
                    }),
                    modifier = Modifier.weight(1f)
                )
            }
            state.error?.let { error ->
                Text(
                    text = stringResource(error.localizedMessage()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.sync_contacts), Modifier.weight(1f))
                Switch(
                    checked = state.syncContacts,
                    onCheckedChange = { onAction(PhoneScreenAction.OnSyncContactsChange(it)) }
                )
            }
            Button(
                onClick = { onAction(PhoneScreenAction.OnSubmit) },
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.action_continue))
            }
            if (canUseQr) {
                TextButton(onClick = onQrLoginClick) {
                    Text(stringResource(Res.string.login_by_qr))
                }
            }
        }
    }

    if (state.isCountryPickerVisible) {
        CountryPickerDialog(
            query = state.countryQuery,
            countries = state.filteredCountries,
            onQueryChange = { onAction(PhoneScreenAction.OnCountryQueryChange(it)) },
            onSelect = { onAction(PhoneScreenAction.OnCountrySelect(it)) },
            onDismiss = { onAction(PhoneScreenAction.OnCountryPickerVisibilityChange(false)) }
        )
    }
}

@KotogramPreview
@Composable
private fun PhoneScreenPreview(
    @PreviewParameter(PhoneScreenStateProvider::class) state: PhoneScreenState
) {
    PhoneScreenContent(
        state = state,
        canUseQr = true,
        onAction = {},
        onQrLoginClick = {}
    )
}

private class PhoneScreenStateProvider : PreviewParameterProvider<PhoneScreenState> {
    private val country = CountryInfo(
        countryCode = "UZ",
        name = "Uzbekistan",
        englishName = "Uzbekistan",
        flagEmoji = "\uD83C\uDDFA\uD83C\uDDFF",
        isHidden = false,
        callingCodes = listOf("998")
    )
    private val entered = PhoneScreenState(
        countries = listOf(country),
        country = country,
        callingCode = "998",
        number = "901234567",
        isAcceptingNumber = true
    )

    override val values = sequenceOf(
        PhoneScreenState(isAcceptingNumber = true),
        entered,
        entered.copy(isSubmitting = true),
        entered.copy(error = AuthError.InvalidPhoneNumber)
    )
}
