package com.xbot.kotogram.feature.auth.presentation.phone

import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.Country

data class PhoneState(
    val countries: List<Country> = emptyList(),
    val country: Country? = null,
    /** Calling code digits without "+". */
    val callingCode: String = "",
    /** National number digits. */
    val number: String = "",
    val syncContacts: Boolean = true,
    val isCountryPickerVisible: Boolean = false,
    val countryQuery: String = "",
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
) {
    val canSubmit: Boolean get() = callingCode.isNotEmpty() && number.length >= MIN_NUMBER_LENGTH &&
        !isSubmitting

    val filteredCountries: List<Country>
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

sealed interface PhoneSideEffect {
    data object Submitted : PhoneSideEffect
}
