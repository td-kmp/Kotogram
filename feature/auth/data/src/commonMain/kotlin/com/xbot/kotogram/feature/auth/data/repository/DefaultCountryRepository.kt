package com.xbot.kotogram.feature.auth.data.repository

import arrow.core.Either
import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.core.tdlib.client.ClientHolder
import com.xbot.kotogram.feature.auth.data.extensions.request
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.repository.CountryRepository
import io.xbot.tdlib.api.CountryInfo
import io.xbot.tdlib.api.GetCountries
import io.xbot.tdlib.api.GetCountryCode
import io.xbot.tdlib.api.GetPhoneNumberInfo
import io.xbot.tdlib.api.PhoneNumberInfo
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

@Scope(AccountId::class)
@Scoped
internal class DefaultCountryRepository(private val client: ClientHolder) : CountryRepository {

    override suspend fun getCountries(): Either<AuthError, List<CountryInfo>> =
        client.request(GetCountries).map { it.countries }

    override suspend fun getCountryCode(): Either<AuthError, String> =
        client.request(GetCountryCode).map { it.text }

    override suspend fun getPhoneNumberInfo(
        phoneNumberPrefix: String
    ): Either<AuthError, PhoneNumberInfo> =
        client.request(GetPhoneNumberInfo(phoneNumberPrefix = phoneNumberPrefix))
}
