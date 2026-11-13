package com.xbot.kotogram.feature.auth.data.extensions

import io.xbot.tdlib.api.CountryInfo
import io.xbot.tdlib.api.PhoneNumberInfo

internal fun PhoneNumberInfo.orTestNumber(digits: String): PhoneNumberInfo =
    if (country != null || !digits.startsWith(TEST_CALLING_CODE)) {
        this
    } else {
        copy(
            country = TestCountry,
            countryCallingCode = TEST_CALLING_CODE,
            formattedPhoneNumber = fill(TEST_PATTERN, digits.removePrefix(TEST_CALLING_CODE)),
        )
    }

private fun fill(
    pattern: String,
    number: String,
): String = buildString {
    var slot = 0
    for (digit in number) {
        while (slot < pattern.length && pattern[slot] != DIGIT_SLOT) append(pattern[slot++])
        append(digit)
        slot++
    }
    while (slot < pattern.length) append(if (pattern[slot++] == DIGIT_SLOT) EXPECTED_DIGIT else ' ')
}

private val TestCountry = CountryInfo(
    countryCode = "TG",
    name = "Test",
    englishName = "Test",
    flagEmoji = "🛰️",
    isHidden = false,
    callingCodes = [TEST_CALLING_CODE],
)

private const val TEST_CALLING_CODE = "999"
private const val TEST_PATTERN = "XXXX X XX"
private const val DIGIT_SLOT = 'X'
private const val EXPECTED_DIGIT = '-'
