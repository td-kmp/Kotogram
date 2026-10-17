package com.xbot.kotogram.feature.auth.data.mapper

import com.xbot.kotogram.feature.auth.domain.model.CountryInfo
import com.xbot.kotogram.feature.auth.domain.model.PhoneNumberInfo
import io.xbot.tdlib.api.CountryInfo as TdCountryInfo
import io.xbot.tdlib.api.PhoneNumberInfo as TdPhoneNumberInfo

internal fun TdCountryInfo.toDomain() = CountryInfo(
    countryCode = countryCode,
    name = name,
    englishName = englishName,
    flagEmoji = flagEmoji,
    isHidden = isHidden,
    callingCodes = callingCodes
)

internal fun TdPhoneNumberInfo.toDomain() = PhoneNumberInfo(
    country = country?.toDomain(),
    countryCallingCode = countryCallingCode,
    formattedPhoneNumber = formattedPhoneNumber,
    isAnonymous = isAnonymous
)
