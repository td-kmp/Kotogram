package com.xbot.kotogram.core.designsystem.component

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.resources.FormattedTextStyles
import com.xbot.kotogram.core.resources.extensions.toAnnotatedString
import io.xbot.tdlib.api.FormattedText
import io.xbot.tdlib.api.TextEntity

@Composable
fun RichText(
    text: FormattedText,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    styles: FormattedTextStyles = RichTextDefaults.styles(),
    onEntityClick: ((TextEntity) -> Unit)? = null
) {
    val annotated = remember(text, styles, onEntityClick) { text.toAnnotatedString(styles, onEntityClick) }
    Text(text = annotated, modifier = modifier, color = color, textAlign = textAlign, style = style)
}

object RichTextDefaults {
    @Composable
    fun styles(): FormattedTextStyles {
        val colors = KotogramTheme.colors
        return remember(colors.primary, colors.surfaceVariant) {
            FormattedTextStyles(
                code = SpanStyle(fontFamily = FontFamily.Monospace, background = colors.surfaceVariant),
                spoiler = SpanStyle(background = colors.surfaceVariant),
                link = TextLinkStyles(style = SpanStyle(color = colors.primary))
            )
        }
    }
}
