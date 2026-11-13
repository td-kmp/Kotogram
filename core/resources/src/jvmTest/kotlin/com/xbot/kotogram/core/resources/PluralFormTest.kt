package com.xbot.kotogram.core.resources

import io.xbot.tdlib.api.LanguagePackStringValuePluralized
import kotlin.test.Test
import kotlin.test.assertEquals

class PluralFormTest {
    private val allForms = LanguagePackStringValuePluralized(
        zeroValue = "zero",
        oneValue = "one",
        twoValue = "two",
        fewValue = "few",
        manyValue = "many",
        otherValue = "other",
    )

    private fun forms(
        pluralCode: String,
        vararg quantities: Int,
    ) = quantities.map { pluralForm(allForms, pluralCode, it) }

    @Test
    fun englishHasOneAndOther() = assertEquals(["other", "one", "other", "other"], forms("en", 0, 1, 2, 21))

    @Test
    fun russianFollowsTheLastDigits() =
        assertEquals(["one", "few", "many", "one", "many"], forms("ru", 1, 22, 25, 101, 111))

    @Test
    fun arabicHasAllSix() =
        assertEquals(["zero", "one", "two", "few", "many", "other"], forms("ar", 0, 1, 2, 5, 11, 100))

    @Test
    fun aRegionWithRulesOfItsOwnKeepsThem() =
        assertEquals(["one", "other"], [pluralForm(allForms, "pt-br", 0), pluralForm(allForms, "pt-pt", 0)])

    @Test
    fun anEmptyFormFallsToOther() = assertEquals(
        "other",
        pluralForm(allForms.copy(manyValue = ""), "ru", 25),
    )

    @Test
    fun anUnknownLanguageTakesOther() = assertEquals("other", pluralForm(allForms, "tlh", 1))
}
