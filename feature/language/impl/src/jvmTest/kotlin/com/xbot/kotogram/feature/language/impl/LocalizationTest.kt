package com.xbot.kotogram.feature.language.impl

import com.xbot.kotogram.core.resources.BundledLocalization
import com.xbot.kotogram.feature.language.domain.model.LanguagePack
import com.xbot.kotogram.feature.language.domain.model.LanguageState
import com.xbot.kotogram.feature.language.impl.extensions.toLocalization
import io.xbot.tdlib.api.LanguagePackInfo
import io.xbot.tdlib.api.LanguagePackStringValueOrdinary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocalizationTest {
    private val pack = LanguagePack(
        info = LanguagePackInfo(id = "x-ru", baseLanguagePackId = "ru", isRtl = false),
        strings = mapOf("A" to LanguagePackStringValueOrdinary("своё")),
        baseStrings = mapOf(
            "A" to LanguagePackStringValueOrdinary("базовое"),
            "B" to LanguagePackStringValueOrdinary("из базы"),
        ),
    )

    @Test
    fun aLoadedPackReadsItsOwnStringsThenItsBase() {
        val localization = LanguageState.Loaded(pack).toLocalization()
        assertEquals(pack.info, localization?.language)
        assertEquals(LanguagePackStringValueOrdinary("своё"), localization?.string("A"))
        assertEquals(LanguagePackStringValueOrdinary("из базы"), localization?.string("B"))
        assertNull(localization?.string("C"))
    }

    @Test
    fun englishIsTheBundleAndLoadingIsNothingYet() {
        assertEquals(BundledLocalization, LanguageState.Bundled.toLocalization())
        assertNull(LanguageState.Loading.toLocalization())
    }

    @Test
    fun theSamePackIsTheSameLocalization() =
        assertEquals(LanguageState.Loaded(pack).toLocalization(), LanguageState.Loaded(pack.copy()).toLocalization())
}
