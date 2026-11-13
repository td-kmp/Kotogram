package com.xbot.kotogram.core.resources

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontWeight
import com.xbot.kotogram.core.resources.extensions.toAnnotatedString
import io.xbot.tdlib.api.FormattedText
import io.xbot.tdlib.api.TextEntity
import io.xbot.tdlib.api.TextEntityType
import io.xbot.tdlib.api.TextEntityTypeBold
import io.xbot.tdlib.api.TextEntityTypeEmailAddress
import io.xbot.tdlib.api.TextEntityTypeMention
import io.xbot.tdlib.api.TextEntityTypeTextUrl
import io.xbot.tdlib.api.TextEntityTypeUrl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FormattedTextTest {
    @Test
    fun boldStylesItsRange() {
        val text = formatted("Hello world", 6, 5, TextEntityTypeBold).toAnnotatedString()
        val bold = text.spanStyles.single()
        assertEquals(FontWeight.Bold, bold.item.fontWeight)
        assertEquals(6 until 11, bold.start until bold.end)
    }

    @Test
    fun aTextLinkOpensItsUrl() {
        val text = formatted("Terms of Service", 0, 16, TextEntityTypeTextUrl("https://telegram.org/tos"))
        assertEquals("https://telegram.org/tos", text.toAnnotatedString().url())
    }

    @Test
    fun aBareUrlGetsAScheme() {
        val text = formatted("see telegram.org", 4, 12, TextEntityTypeUrl)
        assertEquals("https://telegram.org", text.toAnnotatedString().url())
    }

    @Test
    fun anEmailAddressBecomesMailto() {
        assertEquals("mailto:a@b.co", formatted("a@b.co", 0, 6, TextEntityTypeEmailAddress).toAnnotatedString().url())
    }

    @Test
    fun aMentionCallsBackWithItsEntity() {
        val source = formatted("Ask @durov", 4, 6, TextEntityTypeMention)
        var clicked: TextEntity? = null
        val link = source.toAnnotatedString { clicked = it }.getLinkAnnotations(0, 10).single().item
        (link as LinkAnnotation.Clickable).linkInteractionListener?.onClick(link)
        assertEquals(source.entities.single(), clicked)
    }

    @Test
    fun anEntityPastTheEndIsCut() {
        val text = formatted("Hi", 1, 10, TextEntityTypeBold).toAnnotatedString()
        assertTrue(text.spanStyles.single().end == 2)
    }

    private fun formatted(
        text: String,
        offset: Int,
        length: Int,
        type: TextEntityType,
    ) = FormattedText(text, [TextEntity(offset, length, type)])

    private fun androidx.compose.ui.text.AnnotatedString.url() =
        (getLinkAnnotations(0, length).single().item as LinkAnnotation.Url).url
}
