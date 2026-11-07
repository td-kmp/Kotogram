package com.xbot.kotogram.feature.auth.impl

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.repository.CountryRepository
import io.xbot.tdlib.api.CountryInfo
import io.xbot.tdlib.api.PhoneNumberInfo

internal class FakeCountryRepository(private val countries: List<CountryInfo>) : CountryRepository {
    override suspend fun getCountries(): Either<AuthError, List<CountryInfo>> = countries.right()

    override suspend fun getCountryCode(): Either<AuthError, String> =
        countries.first().countryCode.right()

    override suspend fun getPhoneNumberInfo(
        phoneNumberPrefix: String
    ): Either<AuthError, PhoneNumberInfo> {
        val country = countries.firstOrNull { country -> country.callingCodes.any(phoneNumberPrefix::startsWith) }
            ?: return AuthError.Unknown("No info").left()
        val callingCode = country.callingCodes.first(phoneNumberPrefix::startsWith)
        return PhoneNumberInfo(
            country = country,
            countryCallingCode = callingCode,
            formattedPhoneNumber = phoneNumberPrefix.removePrefix(callingCode)
        ).right()
    }
}
