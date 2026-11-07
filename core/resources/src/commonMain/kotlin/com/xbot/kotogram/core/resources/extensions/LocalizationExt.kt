@file:Suppress("ForbiddenImport")

package com.xbot.kotogram.core.resources.extensions

import com.xbot.kotogram.core.resources.LanguagePackKeys
import com.xbot.kotogram.core.resources.Localization
import com.xbot.kotogram.core.resources.StringResource
import com.xbot.kotogram.core.resources.bundledStringEnvironment
import com.xbot.kotogram.core.resources.pluralForm
import io.xbot.tdlib.api.LanguagePackStringValue
import io.xbot.tdlib.api.LanguagePackStringValueOrdinary
import io.xbot.tdlib.api.LanguagePackStringValuePluralized
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource as ComposeStringResource
import org.jetbrains.compose.resources.getPluralString as getBundledPluralString
import org.jetbrains.compose.resources.getString as getBundledString

suspend fun Localization.getString(res: StringResource): String = when (res) {
    is StringResource.String -> res.text
    is StringResource.Text -> template(res.resource).formatTelegram(res.formatArgs)
    is StringResource.Plural -> template(res.resource, res.quantity).formatTelegram(res.formatArgs)
}

internal fun Localization.text(resource: ComposeStringResource): String? =
    (packString(resource.key) as? LanguagePackStringValueOrdinary)?.value

internal fun Localization.plural(resource: PluralStringResource, quantity: Int): String? {
    val value = packString(resource.key) as? LanguagePackStringValuePluralized
    val pack = language
    return if (value != null && pack != null) pluralForm(value, pack.pluralCode.ifEmpty { pack.id }, quantity) else null
}

private fun Localization.packString(resourceKey: String): LanguagePackStringValue? =
    LanguagePackKeys[resourceKey]?.let(::string)

private suspend fun Localization.template(resource: ComposeStringResource): String =
    text(resource) ?: getBundledString(bundledStringEnvironment, resource)

private suspend fun Localization.template(resource: PluralStringResource, quantity: Int): String =
    plural(resource, quantity) ?: getBundledPluralString(bundledStringEnvironment, resource, quantity)
