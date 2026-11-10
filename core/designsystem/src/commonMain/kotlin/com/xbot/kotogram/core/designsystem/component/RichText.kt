package com.xbot.kotogram.core.designsystem.component

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import com.xbot.kotogram.core.resources.FormattedTextStyles
import com.xbot.kotogram.core.resources.extensions.toAnnotatedString
import io.xbot.tdlib.api.FormattedText
import io.xbot.tdlib.api.TextEntity

@Composable
fun RichText(
    text: FormattedText,
    modifier: Modifier = Modifier,
    onEntityClick: ((TextEntity) -> Unit)? = null,
    style: RichTextStyle = RichTextStyle,
) {
    @Suppress("NAME_SHADOWING")
    val style = rememberStyle(::RichTextStyleScope, RichTextStyle.Default then style)
    val annotated = remember(text, style, onEntityClick) {
        text.toAnnotatedString(style.formattedTextStyles, onEntityClick)
    }
    Text(text = annotated, modifier = modifier, style = LocalTextStyle.current.merge(style.textStyle))
}

fun interface RichTextStyle : ComponentStyle<RichTextStyleScope> {
    companion object : RichTextStyle {
        override fun RichTextStyleScope.applyStyle() {}

        val Default = RichTextStyle {
            formattedTextStyles(
                FormattedTextStyles(
                    code = SpanStyle(fontFamily = FontFamily.Monospace, background = theme.colors.surfaceVariant),
                    spoiler = SpanStyle(background = theme.colors.surfaceVariant),
                    link = TextLinkStyles(style = SpanStyle(color = theme.colors.primary)),
                ),
            )
        }
    }
}

infix fun RichTextStyle.then(other: RichTextStyle): RichTextStyle = when {
    other === RichTextStyle -> this

    this === RichTextStyle -> other

    else -> RichTextStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

class RichTextStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<RichTextStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var textStyle: TextStyle = TextStyle.Default
        private set

    var formattedTextStyles: FormattedTextStyles = FormattedTextStyles()
        private set

    fun textStyle(style: TextStyle) {
        textStyle = style
    }

    fun formattedTextStyles(styles: FormattedTextStyles) {
        formattedTextStyles = styles
    }
}
