package com.xbot.kotogram.feature.auth.data.mapper

import com.xbot.kotogram.feature.auth.domain.model.Country
import com.xbot.kotogram.feature.auth.domain.model.PhoneNumberInfo
import io.xbot.tdlib.api.CountryInfo
import io.xbot.tdlib.api.PhoneNumberInfo as TdPhoneNumberInfo

internal fun CountryInfo.toDomain() = Country(
    code = countryCode,
    name = name,
    englishName = englishName,
    flagEmoji = flagEmoji,
    callingCodes = callingCodes
)

internal fun TdPhoneNumberInfo.toDomain() = PhoneNumberInfo(
    country = country?.toDomain(),
    callingCode = countryCallingCode,
    formattedNumber = formattedPhoneNumber
)
