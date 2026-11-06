package com.xbot.kotogram.feature.auth.impl.screen.phone

import io.xbot.tdlib.api.CountryInfo

internal sealed interface PhoneScreenAction {
    data class OnCallingCodeChange(val value: String) : PhoneScreenAction

    data class OnNumberChange(val value: String) : PhoneScreenAction

    data class OnSyncContactsChange(val enabled: Boolean) : PhoneScreenAction

    data class OnCountryPickerVisibilityChange(val visible: Boolean) : PhoneScreenAction

    data class OnCountryQueryChange(val query: String) : PhoneScreenAction

    data class OnCountrySelect(val country: CountryInfo) : PhoneScreenAction

    data object OnSubmit : PhoneScreenAction
}
