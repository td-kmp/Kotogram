package com.xbot.kotogram.feature.auth.data.repository

import arrow.core.Either
import arrow.core.raise.either
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.core.tdlib.ClientHolder
import com.xbot.kotogram.feature.auth.data.mapper.request
import com.xbot.kotogram.feature.auth.data.mapper.toDomain
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.CountryInfo
import com.xbot.kotogram.feature.auth.domain.model.PhoneNumberInfo
import com.xbot.kotogram.feature.auth.domain.repository.CountryRepository
import io.xbot.tdlib.api.GetCountries
import io.xbot.tdlib.api.GetCountryCode
import io.xbot.tdlib.api.GetPhoneNumberInfo
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

@Scope(AccountId::class)
@Scoped
internal class DefaultCountryRepository(private val client: ClientHolder) : CountryRepository {

    override suspend fun getCountries(): Either<AuthError, List<CountryInfo>> = either {
        request { client.send(GetCountries) }.countries.map { it.toDomain() }
    }

    override suspend fun getCountryCode(): Either<AuthError, String> = either {
        request { client.send(GetCountryCode) }.text
    }

    override suspend fun getPhoneNumberInfo(
        phoneNumberPrefix: String
    ): Either<AuthError, PhoneNumberInfo> = either {
        request {
            client.send(GetPhoneNumberInfo(phoneNumberPrefix = phoneNumberPrefix))
        }.toDomain()
    }
}
