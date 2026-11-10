package com.xbot.kotogram.feature.auth.domain.model

import io.xbot.tdlib.api.CountryInfo

data class EnteredPhoneNumber(
    val country: CountryInfo?,
    val callingCode: String,
    val number: String,
) {
    val international: String get() = "+$callingCode$number"
}
