package com.xbot.kotogram.feature.auth.domain.repository

import arrow.core.Either
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import io.xbot.tdlib.api.CountryInfo
import io.xbot.tdlib.api.PhoneNumberInfo

interface CountryRepository {
    suspend fun getCountries(): Either<AuthError, List<CountryInfo>>

    suspend fun getCountryCode(): Either<AuthError, String>

    suspend fun getPhoneNumberInfo(phoneNumberPrefix: String): Either<AuthError, PhoneNumberInfo>
}
