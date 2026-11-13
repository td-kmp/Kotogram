package com.xbot.kotogram.core.resources

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontWeight
import com.xbot.kotogram.core.resources.extensions.formatTelegram
import com.xbot.kotogram.core.resources.extensions.toTelegramText
import kotlin.test.Test
import kotlin.test.assertEquals

class TelegramTextTest {
    @Test
    fun positionalArgumentsGoByTheirNumber() = assertEquals(
        "Bob invited Alice",
        "%2\$@ invited %1\$@".formatTelegram(["Alice", "Bob"]),
    )

    @Test
    fun bareArgumentsGoInOrder() = assertEquals(
        "3 of 5",
        "%d of %d".formatTelegram([3, 5]),
    )

    @Test
    fun boldMarksAndEscapedPercentsAreDropped() = assertEquals(
        "Go to Settings, 100%",
        "Go to **Settings**, 100%%".formatTelegram([]),
    )

    @Test
    fun linksKeepTheirText() = assertEquals(
        "Get the code on Fragment",
        "Get the code on [Fragment](%1\$@)".formatTelegram(["https://fragment.com"]),
    )

    @Test
    fun aMissingArgumentKeepsItsPlaceholder() = assertEquals(
        "Hello %@",
        "Hello %@".formatTelegram([]),
    )

    @Test
    fun boldCoversItsArgument() {
        val text = "to your phone **%@**".toTelegramText(["+1 234"])
        val bold = text.spanStyles.single()
        assertEquals("+1 234", text.text.substring(bold.start, bold.end))
        assertEquals(FontWeight.Bold, bold.item.fontWeight)
    }

    @Test
    fun anArgumentIsNeverMarkup() = assertEquals(
        "**a** [b](c)",
        "%@".formatTelegram(["**a** [b](c)"]),
    )

    @Test
    fun aLinkGetsItsUrlWithArguments() {
        val text = "on [Fragment](%1\$@) · [Edit]()".toTelegramText(["https://fragment.com"]) {
            LinkAnnotation.Clickable(it, linkInteractionListener = null)
        }
        val links = text.getLinkAnnotations(0, text.length)
        assertEquals(["https://fragment.com", ""], links.map { (it.item as LinkAnnotation.Clickable).tag })
        assertEquals("Edit", text.text.substring(links[1].start, links[1].end))
    }
}
