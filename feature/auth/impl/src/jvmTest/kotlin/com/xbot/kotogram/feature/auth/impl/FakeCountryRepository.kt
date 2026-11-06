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
    ): Either<AuthError, PhoneNumberInfo> = AuthError.Unknown("No info").left()
}
