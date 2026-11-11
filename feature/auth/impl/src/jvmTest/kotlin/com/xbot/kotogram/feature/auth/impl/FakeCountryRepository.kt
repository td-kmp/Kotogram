package com.xbot.kotogram.feature.auth.impl

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.repository.CountryRepository
import io.xbot.tdlib.api.CountryInfo
import io.xbot.tdlib.api.PhoneNumberInfo

internal class FakeCountryRepository(
    private val countries: List<CountryInfo>,
) : CountryRepository {
    override suspend fun getCountries(): Either<AuthError, List<CountryInfo>> = countries.right()

    override suspend fun getCountryCode(): Either<AuthError, String> = countries.first().countryCode.right()

    // Adapted from TDLib's CountryInfoManager::get_phone_number_info_object: its answer for a number of no country.
    override suspend fun getPhoneNumberInfo(phoneNumberPrefix: String): Either<AuthError, PhoneNumberInfo> {
        val country = countries.firstOrNull { country -> country.callingCodes.any(phoneNumberPrefix::startsWith) }
        if (country == null) {
            val startsACode = countries.any { it.callingCodes.any { code -> code.startsWith(phoneNumberPrefix) } }
            return PhoneNumberInfo(
                country = null,
                countryCallingCode = if (startsACode) phoneNumberPrefix else "",
                formattedPhoneNumber = if (startsACode) "" else phoneNumberPrefix,
            ).right()
        }
        val callingCode = country.callingCodes.first(phoneNumberPrefix::startsWith)
        return PhoneNumberInfo(
            country = country,
            countryCallingCode = callingCode,
            formattedPhoneNumber = phoneNumberPrefix.removePrefix(callingCode),
        ).right()
    }
}
