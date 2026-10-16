package com.xbot.kotogram.feature.auth.data.repository

import com.xbot.kotogram.data.tdlib.TelegramClient
import com.xbot.kotogram.feature.auth.data.mapper.tdCall
import com.xbot.kotogram.feature.auth.data.mapper.toDomain
import com.xbot.kotogram.feature.auth.domain.model.Country
import com.xbot.kotogram.feature.auth.domain.model.PhoneNumberInfo
import com.xbot.kotogram.feature.auth.domain.repository.CountryRepository
import io.xbot.tdlib.api.GetCountries
import io.xbot.tdlib.api.GetCountryCode
import io.xbot.tdlib.api.GetPhoneNumberInfo
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class TdCountryRepository(private val client: TelegramClient) : CountryRepository {

    private val mutex = Mutex()
    private var cachedCountries: List<Country>? = null

    override suspend fun countries(): List<Country> = mutex.withLock {
        cachedCountries ?: tdCall { client.send(GetCountries) }
            .countries
            .filterNot { it.isHidden }
            .map { it.toDomain() }
            .sortedBy { it.name }
            .also { cachedCountries = it }
    }

    override suspend fun detectedCountryCode(): String? = runCatching {
        tdCall { client.send(GetCountryCode) }.text
    }.getOrNull()?.takeIf { it.isNotBlank() }

    override suspend fun phoneNumberInfo(phoneNumberPrefix: String): PhoneNumberInfo =
        tdCall { client.send(GetPhoneNumberInfo(phoneNumberPrefix = phoneNumberPrefix)) }.toDomain()
}
