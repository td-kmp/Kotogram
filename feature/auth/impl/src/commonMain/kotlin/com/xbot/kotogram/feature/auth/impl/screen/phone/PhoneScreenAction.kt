package com.xbot.kotogram.feature.auth.impl.screen.phone

import io.xbot.tdlib.api.CountryInfo

internal sealed interface PhoneScreenAction {
    data class OnCountryPickerVisibilityChange(
        val visible: Boolean,
    ) : PhoneScreenAction

    data class OnCountrySelect(
        val country: CountryInfo,
    ) : PhoneScreenAction

    data object OnSubmit : PhoneScreenAction
}
