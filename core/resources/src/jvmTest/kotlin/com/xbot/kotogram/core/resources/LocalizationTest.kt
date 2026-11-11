package com.xbot.kotogram.core.resources

import com.xbot.kotogram.core.resources.extensions.getString
import com.xbot.kotogram.core.resources.extensions.plural
import com.xbot.kotogram.core.resources.extensions.text
import io.xbot.tdlib.api.LanguagePackInfo
import io.xbot.tdlib.api.LanguagePackStringValue
import io.xbot.tdlib.api.LanguagePackStringValueOrdinary
import io.xbot.tdlib.api.LanguagePackStringValuePluralized
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocalizationTest {
    private val russian = object : Localization {
        override val language = LanguagePackInfo(id = "ru", pluralCode = "ru")

        override fun string(key: String): LanguagePackStringValue? = when (key) {
            "Login.QR.Title" -> LanguagePackStringValueOrdinary("Вход по QR-коду")

            "Chat.GroupCall.Members" -> LanguagePackStringValuePluralized(
                oneValue = "%d участник",
                fewValue = "%d участника",
                otherValue = "%d участников",
            )

            "Communities.Members" -> LanguagePackStringValueOrdinary("участники")

            else -> null
        }
    }

    @Test
    fun aResourceIsFoundByItsPackKey() = assertEquals("Вход по QR-коду", russian.text(Res.string.login_qr_title))

    @Test
    fun aPluralTakesThePackLanguagesForm() = assertEquals(
        listOf("%d участник", "%d участника", "%d участников"),
        listOf(1, 3, 5).map { russian.plural(Res.plurals.chat_groupcall_members, it) },
    )

    @Test
    fun aStringThePackLacksIsLeftToTheBundle() {
        assertNull(russian.text(Res.string.about_description))
        assertNull(BundledLocalization.text(Res.string.login_qr_title))
    }

    @Test
    fun aValueOfAnotherKindIsLeftToTheBundle() = assertNull(russian.plural(Res.plurals.communities_members, 1))

    @Test
    fun outsideCompositionThePackComesBeforeTheBundle() = runBlocking {
        val title = StringResource.Text(Res.string.login_qr_title)
        assertEquals("Вход по QR-коду", russian.getString(title))
        assertEquals("Log in to Telegram by QR Code", BundledLocalization.getString(title))
        val members = listOf(1, 21).map { StringResource.Plural(Res.plurals.chat_groupcall_members, it, it) }
        assertEquals(listOf("1 participant", "21 participants"), members.map { BundledLocalization.getString(it) })
    }
}
