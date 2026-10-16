package com.xbot.kotogram.feature.auth.presentation.phone

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.feature.auth.domain.model.Country
import com.xbot.kotogram.feature.auth.presentation.common.AuthErrorText
import com.xbot.kotogram.feature.auth.presentation.common.AuthScaffold
import com.xbot.kotogram.feature.auth.presentation.common.AuthSubtitle
import com.xbot.kotogram.feature.auth.presentation.common.asString
import com.xbot.kotogram.feature.auth.presentation.common.toUiText
import com.xbot.kotogram.feature.auth.presentation.resources.Res
import com.xbot.kotogram.feature.auth.presentation.resources.action_continue
import com.xbot.kotogram.feature.auth.presentation.resources.country_placeholder
import com.xbot.kotogram.feature.auth.presentation.resources.country_search
import com.xbot.kotogram.feature.auth.presentation.resources.login_by_passkey
import com.xbot.kotogram.feature.auth.presentation.resources.login_by_qr
import com.xbot.kotogram.feature.auth.presentation.resources.phone_hint
import com.xbot.kotogram.feature.auth.presentation.resources.phone_subtitle
import com.xbot.kotogram.feature.auth.presentation.resources.phone_title
import com.xbot.kotogram.feature.auth.presentation.resources.sync_contacts
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
internal fun PhoneRoute(
    canUseQr: Boolean,
    canUsePasskey: Boolean,
    onSubmitted: () -> Unit,
    onQrLogin: () -> Unit,
    onPasskeyLogin: () -> Unit,
    viewModel: PhoneViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { effect ->
        when (effect) {
            PhoneSideEffect.Submitted -> onSubmitted()
        }
    }
    PhoneScreen(
        state = state,
        canUseQr = canUseQr,
        canUsePasskey = canUsePasskey,
        onCallingCodeChange = viewModel::onCallingCodeChange,
        onNumberChange = viewModel::onNumberChange,
        onSyncContactsChange = viewModel::onSyncContactsChange,
        onCountryPickerVisibilityChange = viewModel::onCountryPickerVisibilityChange,
        onCountryQueryChange = viewModel::onCountryQueryChange,
        onCountrySelect = viewModel::onCountrySelect,
        onSubmit = viewModel::onSubmit,
        onQrLogin = onQrLogin,
        onPasskeyLogin = onPasskeyLogin
    )
}

@Composable
internal fun PhoneScreen(
    state: PhoneState,
    canUseQr: Boolean,
    canUsePasskey: Boolean,
    onCallingCodeChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onSyncContactsChange: (Boolean) -> Unit,
    onCountryPickerVisibilityChange: (Boolean) -> Unit,
    onCountryQueryChange: (String) -> Unit,
    onCountrySelect: (Country) -> Unit,
    onSubmit: () -> Unit,
    onQrLogin: () -> Unit,
    onPasskeyLogin: () -> Unit,
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
            modifier = Modifier.fillMaxWidth().clickable { onCountryPickerVisibilityChange(true) }
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = "+${state.callingCode}",
                onValueChange = onCallingCodeChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.width(96.dp)
            )
            OutlinedTextField(
                value = state.number,
                onValueChange = onNumberChange,
                placeholder = { Text(stringResource(Res.string.phone_hint)) },
                singleLine = true,
                isError = state.error != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier.weight(1f)
            )
        }
        AuthErrorText(state.error?.toUiText()?.asString())
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.sync_contacts), Modifier.weight(1f))
            Switch(checked = state.syncContacts, onCheckedChange = onSyncContactsChange)
        }
        Button(onClick = onSubmit, enabled = state.canSubmit, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.action_continue))
        }
        if (canUseQr) {
            TextButton(onClick = onQrLogin) {
                Text(stringResource(Res.string.login_by_qr))
            }
        }
        if (canUsePasskey) {
            TextButton(onClick = onPasskeyLogin) {
                Text(stringResource(Res.string.login_by_passkey))
            }
        }
    }

    if (state.isCountryPickerVisible) {
        CountryPickerDialog(
            query = state.countryQuery,
            countries = state.filteredCountries,
            onQueryChange = onCountryQueryChange,
            onSelect = onCountrySelect,
            onDismiss = { onCountryPickerVisibilityChange(false) }
        )
    }
}

@Composable
private fun CountryPickerDialog(
    query: String,
    countries: List<Country>,
    onQueryChange: (String) -> Unit,
    onSelect: (Country) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text(stringResource(Res.string.country_search)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                LazyColumn {
                    items(countries, key = { it.code }) { country ->
                        ListItem(
                            headlineContent = { Text("${country.flagEmoji} ${country.name}") },
                            trailingContent = {
                                Text("+${country.callingCodes.firstOrNull().orEmpty()}")
                            },
                            modifier = Modifier.clickable { onSelect(country) }
                        )
                    }
                }
            }
        }
    )
}
