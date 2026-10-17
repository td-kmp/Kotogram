package com.xbot.kotogram.feature.auth.domain.model

/** TDLib's `countryInfo`, field for field. */
data class CountryInfo(
    /** ISO 3166-1 alpha-2 code. */
    val countryCode: String,
    val name: String,
    val englishName: String,
    val flagEmoji: String,
    val isHidden: Boolean,
    val callingCodes: List<String>
)

/** TDLib's `phoneNumberInfo`, field for field. */
data class PhoneNumberInfo(
    val country: CountryInfo?,
    val countryCallingCode: String,
    /** The number without the calling code, formatted the way the country writes it. */
    val formattedPhoneNumber: String,
    val isAnonymous: Boolean
)
