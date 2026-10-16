package com.xbot.kotogram.feature.auth.domain.repository

import com.xbot.kotogram.feature.auth.domain.model.Country
import com.xbot.kotogram.feature.auth.domain.model.PhoneNumberInfo

interface CountryRepository {
    suspend fun countries(): List<Country>

    /** The country Telegram guesses from the user's IP, as an ISO code. */
    suspend fun detectedCountryCode(): String?

    suspend fun phoneNumberInfo(phoneNumberPrefix: String): PhoneNumberInfo
}
