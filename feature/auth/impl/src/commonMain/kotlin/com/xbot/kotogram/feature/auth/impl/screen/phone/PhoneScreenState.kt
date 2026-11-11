package com.xbot.kotogram.feature.auth.impl.screen.phone

import com.xbot.kotogram.feature.auth.domain.model.AuthError
import io.xbot.tdlib.api.CountryInfo

internal data class PhoneScreenState(
    val countries: List<CountryInfo> = emptyList(),
    val country: CountryInfo? = null,
    val callingCode: String = "",
    val number: String = "",
    val numberPattern: String = "",
    val isAcceptingNumber: Boolean = false,
    val isCountryPickerVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: AuthError? = null,
) {
    val canSubmit: Boolean
        get() = isAcceptingNumber && !isSubmitting && callingCode.isNotEmpty() && number.length >= MIN_NUMBER_LENGTH

    private companion object {
        const val MIN_NUMBER_LENGTH = 5
    }
}
