package com.xbot.kotogram.feature.auth.presentation.screen.phone

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.presentation.component.AuthErrorText
import com.xbot.kotogram.feature.auth.presentation.component.AuthScaffold
import com.xbot.kotogram.feature.auth.presentation.component.AuthSubtitle
import com.xbot.kotogram.feature.auth.presentation.component.asString
import com.xbot.kotogram.feature.auth.presentation.component.toUiText
import com.xbot.kotogram.feature.auth.presentation.resources.Res
import com.xbot.kotogram.feature.auth.presentation.resources.action_continue
import com.xbot.kotogram.feature.auth.presentation.resources.country_placeholder
import com.xbot.kotogram.feature.auth.presentation.resources.login_by_passkey
import com.xbot.kotogram.feature.auth.presentation.resources.login_by_qr
import com.xbot.kotogram.feature.auth.presentation.resources.phone_hint
import com.xbot.kotogram.feature.auth.presentation.resources.phone_subtitle
import com.xbot.kotogram.feature.auth.presentation.resources.phone_title
import com.xbot.kotogram.feature.auth.presentation.resources.sync_contacts
import io.xbot.tdlib.api.CountryInfo
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
internal fun PhoneScreen(
    canUseQr: Boolean,
    canUsePasskey: Boolean,
    onSubmitted: () -> Unit,
    onQrLoginClick: () -> Unit,
    onPasskeyLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhoneViewModel = koinViewModel()
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
        canUsePasskey = canUsePasskey,
        onAction = viewModel::onAction,
        onQrLoginClick = onQrLoginClick,
        onPasskeyLoginClick = onPasskeyLoginClick,
        modifier = modifier
    )
}

@Composable
private fun PhoneScreenContent(
    state: PhoneScreenState,
    canUseQr: Boolean,
    canUsePasskey: Boolean,
    onAction: (PhoneScreenAction) -> Unit,
    onQrLoginClick: () -> Unit,
    onPasskeyLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScaffold(
        title = stringResource(Res.string.phone_title),
        subtitle = { AuthSubtitle(stringResource(Res.string.phone_subtitle)) },
        modifier = modifier
    ) {
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
        AuthErrorText(state.error?.toUiText()?.asString())
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
        if (canUsePasskey) {
            TextButton(onClick = onPasskeyLoginClick) {
                Text(stringResource(Res.string.login_by_passkey))
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
        canUsePasskey = false,
        onAction = {},
        onQrLoginClick = {},
        onPasskeyLoginClick = {}
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
