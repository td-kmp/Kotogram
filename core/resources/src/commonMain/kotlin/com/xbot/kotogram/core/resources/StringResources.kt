@file:Suppress("ForbiddenImport")

package com.xbot.kotogram.core.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.TextLinkStyles
import com.xbot.kotogram.core.resources.extensions.formatTelegram
import com.xbot.kotogram.core.resources.extensions.plural
import com.xbot.kotogram.core.resources.extensions.text
import com.xbot.kotogram.core.resources.extensions.toTelegramText
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource as ComposeStringResource
import org.jetbrains.compose.resources.pluralStringResource as bundledPluralStringResource
import org.jetbrains.compose.resources.stringResource as bundledStringResource

@Composable
fun stringResource(
    resource: ComposeStringResource,
    vararg formatArgs: Any,
): String = template(resource).formatTelegram(formatArgs)

@Composable
fun pluralStringResource(
    resource: PluralStringResource,
    quantity: Int,
    vararg formatArgs: Any,
): String = template(resource, quantity).formatTelegram(formatArgs)

@Composable
fun annotatedStringResource(
    resource: ComposeStringResource,
    vararg formatArgs: Any,
    linkStyles: TextLinkStyles? = null,
    onLinkClick: (url: String) -> Unit = {},
): AnnotatedString = template(resource).annotate(formatArgs, linkStyles, onLinkClick)

@Composable
fun stringResource(res: StringResource): String = when (res) {
    is StringResource.String -> res.text
    is StringResource.Text -> template(res.resource).formatTelegram(res.formatArgs)
    is StringResource.Plural -> template(res.resource, res.quantity).formatTelegram(res.formatArgs)
}

@Composable
fun annotatedStringResource(
    res: StringResource,
    linkStyles: TextLinkStyles? = null,
    onLinkClick: (url: String) -> Unit = {},
): AnnotatedString = when (res) {
    is StringResource.String -> AnnotatedString(res.text)

    is StringResource.Text -> template(res.resource).annotate(res.formatArgs, linkStyles, onLinkClick)

    is StringResource.Plural ->
        template(res.resource, res.quantity).annotate(res.formatArgs, linkStyles, onLinkClick)
}

@Composable
private fun template(resource: ComposeStringResource): String =
    LocalLocalization.current.text(resource) ?: bundledStringResource(resource)

@Composable
private fun template(
    resource: PluralStringResource,
    quantity: Int,
): String = LocalLocalization.current.plural(resource, quantity) ?: bundledPluralStringResource(resource, quantity)

@Composable
private fun String.annotate(
    args: Array<out Any>,
    linkStyles: TextLinkStyles?,
    onLinkClick: (String) -> Unit,
): AnnotatedString {
    val currentOnLinkClick by rememberUpdatedState(onLinkClick)
    val listener = remember {
        LinkInteractionListener { link -> (link as? LinkAnnotation.Clickable)?.let { currentOnLinkClick(it.tag) } }
    }
    return toTelegramText(args) { url ->
        if ("://" in url) LinkAnnotation.Url(url, linkStyles) else LinkAnnotation.Clickable(url, linkStyles, listener)
    }
}
