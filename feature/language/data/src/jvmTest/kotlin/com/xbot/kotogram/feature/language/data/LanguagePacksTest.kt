package com.xbot.kotogram.feature.language.data

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class LanguagePacksTest {
    @Test
    fun aDeclaredLanguageIsThePackOfItsTagInLowerCase() {
        assertEquals("zh-hans", languagePackIdOf("zh-Hans"))
        assertEquals("pt-br", languagePackIdOf("pt-BR"))
        assertEquals("ru", languagePackIdOf("ru"))
    }

    @Test
    fun androidAndIosDeclareTheSameLanguages() {
        val config = File(ROOT, "android-app/src/main/res/xml/locales_config.xml").readText()
        val android = Regex("""android:name="([^"]+)"""").findAll(config).map { it.groupValues[1] }.toList()

        val folders = File(ROOT, "ios-app/src").list().orEmpty()
            .filter { it.endsWith(LPROJ) }
            .map { it.removeSuffix(LPROJ) }
        assertEquals(android.sorted(), folders.sorted())

        val project = File(ROOT, "ios-app/Kotogram.xcodeproj/project.pbxproj").readText()
        val regions = project.substringAfter("knownRegions = (").substringBefore(");")
            .split(',')
            .map { it.trim().trim('"') }
            .filter { it.isNotEmpty() && it != "Base" }
        assertEquals(android, regions)
    }
}

private const val ROOT = "../../.."
private const val LPROJ = ".lproj"
