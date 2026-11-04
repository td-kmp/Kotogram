package com.xbot.kotogram.feature.auth.impl.screen.phone

import com.xbot.kotogram.feature.auth.domain.model.AuthError
import io.xbot.tdlib.api.CountryInfo

internal data class PhoneScreenState(
    val countries: List<CountryInfo> = emptyList(),
    val country: CountryInfo? = null,
    /** Calling code digits without "+". */
    val callingCode: String = "",
    /** National number digits. */
    val number: String = "",
    val syncContacts: Boolean = true,
    /** TDLib takes a phone number now: it isn't restarting or waiting for a QR scan. */
    val isAcceptingNumber: Boolean = false,
    val isCountryPickerVisible: Boolean = false,
    val countryQuery: String = "",
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
) {
    val canSubmit: Boolean
        get() = isAcceptingNumber && !isSubmitting && callingCode.isNotEmpty() && hasFullNumber

    private val hasFullNumber: Boolean get() = number.length >= MIN_NUMBER_LENGTH

    val filteredCountries: List<CountryInfo>
        get() = if (countryQuery.isBlank()) {
            countries
        } else {
            countries.filter { country ->
                country.name.contains(countryQuery, ignoreCase = true) ||
                    country.englishName.contains(countryQuery, ignoreCase = true) ||
                    country.callingCodes.any { it.startsWith(countryQuery.removePrefix("+")) }
            }
        }

    private companion object {
        const val MIN_NUMBER_LENGTH = 5
    }
}

internal sealed interface PhoneScreenAction {
    data class OnCallingCodeChange(val value: String) : PhoneScreenAction

    data class OnNumberChange(val value: String) : PhoneScreenAction

    data class OnSyncContactsChange(val enabled: Boolean) : PhoneScreenAction

    data class OnCountryPickerVisibilityChange(val visible: Boolean) : PhoneScreenAction

    data class OnCountryQueryChange(val query: String) : PhoneScreenAction

    data class OnCountrySelect(val country: CountryInfo) : PhoneScreenAction

    data object OnSubmit : PhoneScreenAction
}

internal sealed interface PhoneScreenSideEffect {
    data object Submitted : PhoneScreenSideEffect
}
