package com.xbot.kotogram.feature.auth.data.extensions

import io.xbot.tdlib.api.CountryInfo
import io.xbot.tdlib.api.PhoneNumberInfo
import kotlin.test.Test
import kotlin.test.assertEquals

class PhoneNumberInfoExtTest {
    @Test
    fun aTestNumberBelongsToTheTestCountry() {
        val info = tdlibAnswerWithoutCountry("9996621234").orTestNumber("9996621234")
        assertEquals("Test", info.country?.name)
        assertEquals("999", info.countryCallingCode)
        assertEquals("6621 2 34", info.formattedPhoneNumber)
    }

    @Test
    fun theTestCodeAloneExpectsSevenDigits() {
        assertEquals("---- - --", tdlibAnswerWithoutCountry("999").orTestNumber("999").formattedPhoneNumber)
    }

    @Test
    fun aPartOfATestNumberKeepsThePlacesOfTheRest() {
        assertEquals("66-- - --", tdlibAnswerWithoutCountry("99966").orTestNumber("99966").formattedPhoneNumber)
        assertEquals("6621 2 --", tdlibAnswerWithoutCountry("99966212").orTestNumber("99966212").formattedPhoneNumber)
    }

    @Test
    fun digitsPastThePatternAreKept() {
        assertEquals(
            "6621 2 345",
            tdlibAnswerWithoutCountry("99966212345").orTestNumber("99966212345").formattedPhoneNumber,
        )
    }

    @Test
    fun aNumberTdlibKnowsStaysAsItIs() {
        val uzbekistan = CountryInfo(
            countryCode = "UZ",
            name = "Uzbekistan",
            englishName = "Uzbekistan",
            flagEmoji = "",
            isHidden = false,
            callingCodes = ["998"],
        )
        val info = PhoneNumberInfo(uzbekistan, countryCallingCode = "998", formattedPhoneNumber = "90 --- -- --")
        assertEquals(info, info.orTestNumber("99890"))
    }

    @Test
    fun anotherNumberWithoutACountryStaysAsItIs() {
        val info = tdlibAnswerWithoutCountry("2025550123")
        assertEquals(info, info.orTestNumber("2025550123"))
    }

    private fun tdlibAnswerWithoutCountry(digits: String) = PhoneNumberInfo(formattedPhoneNumber = digits)
}
