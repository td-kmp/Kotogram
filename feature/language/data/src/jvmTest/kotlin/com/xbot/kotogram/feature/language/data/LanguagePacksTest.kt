package com.xbot.kotogram.feature.language.data

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LanguagePacksTest {
    @Test
    fun theFirstSupportedLanguageOfTheUsersListWins() {
        assertEquals("ru", languagePackIdOf(listOf("ru-RU", "en-US")))
        assertEquals("de", languagePackIdOf(listOf("uz-UZ", "de-DE", "ru")))
    }

    @Test
    fun aLanguageWithoutAPackIsEnglish() {
        assertEquals("en", languagePackIdOf(listOf("uz-UZ")))
        assertEquals("en", languagePackIdOf(emptyList()))
    }

    @Test
    fun chineseGoesByScriptAndThenByRegion() {
        assertEquals("zh-hans", languagePackIdOf(listOf("zh-Hans-CN")))
        assertEquals("zh-hant", languagePackIdOf(listOf("zh-Hant-HK")))
        assertEquals("zh-hant", languagePackIdOf(listOf("zh-TW")))
        assertEquals("zh-hans", languagePackIdOf(listOf("zh")))
    }

    @Test
    fun aRegionDoesNotMatterWhereThePackHasOne() {
        assertEquals("pt-br", languagePackIdOf(listOf("pt-PT")))
        assertEquals("pt-br", languagePackIdOf(listOf("pt_BR")))
    }

    @Test
    fun codesThePlatformsStillUseAreKnown() {
        assertEquals("he", languagePackIdOf(listOf("iw-IL")))
        assertEquals("nb", languagePackIdOf(listOf("no-NO")))
    }

    @Test
    fun aPackHasItsLanguageTag() {
        assertEquals("pt-BR", languageTagOf("pt-br"))
        assertEquals("zh-Hant", languageTagOf("zh-hant"))
        assertNull(languageTagOf("x-custom"))
    }

    @Test
    fun androidDeclaresTheSupportedLanguages() {
        val config = File(ROOT, "android-app/src/main/res/xml/locales_config.xml").readText()
        val declared = Regex("""android:name="([^"]+)"""").findAll(config).map { it.groupValues[1] }.toList()
        assertEquals(SupportedLanguageTags, declared)
    }

    @Test
    fun iosDeclaresTheSupportedLanguages() {
        val folders = File(ROOT, "ios-app/src").list().orEmpty()
            .filter { it.endsWith(LPROJ) }
            .map { it.removeSuffix(LPROJ) }
        assertEquals(SupportedLanguageTags.sorted(), folders.sorted())

        val project = File(ROOT, "ios-app/Kotogram.xcodeproj/project.pbxproj").readText()
        val regions = project.substringAfter("knownRegions = (").substringBefore(");")
            .split(',')
            .map { it.trim().trim('"') }
            .filter { it.isNotEmpty() && it != "Base" }
        assertEquals(SupportedLanguageTags, regions)
    }
}

private const val ROOT = "../../.."
private const val LPROJ = ".lproj"
