package com.xbot.kotogram.feature.auth.domain.model

data class Country(
    /** ISO 3166-1 alpha-2 code. */
    val code: String,
    val name: String,
    val englishName: String,
    val flagEmoji: String,
    val callingCodes: List<String>
)

data class PhoneNumberInfo(
    val country: Country?,
    val callingCode: String,
    /** The number without the calling code, with spaces/dashes as the country formats it. */
    val formattedNumber: String
)
