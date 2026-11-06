package com.xbot.kotogram.core.resources

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun stringResource(res: StringResource): String = when (res) {
    is StringResource.String -> res.text
    is StringResource.Text -> stringResource(res.resource, *res.formatArgs)
    is StringResource.Plural -> pluralStringResource(res.resource, res.quantity, *res.formatArgs)
}

suspend fun getString(res: StringResource): String = when (res) {
    is StringResource.String -> res.text
    is StringResource.Text -> getString(res.resource, *res.formatArgs)
    is StringResource.Plural -> getPluralString(res.resource, res.quantity, *res.formatArgs)
}

sealed interface StringResource {
    data class String(val text: kotlin.String) : StringResource

    class Text(
        val resource: org.jetbrains.compose.resources.StringResource,
        vararg val formatArgs: Any
    ) : StringResource {
        override fun equals(other: Any?): Boolean = other is Text &&
            resource == other.resource &&
            formatArgs.contentEquals(other.formatArgs)

        override fun hashCode(): Int = 31 * resource.hashCode() + formatArgs.contentHashCode()
    }

    class Plural(
        val resource: PluralStringResource,
        val quantity: Int,
        vararg val formatArgs: Any
    ) : StringResource {
        override fun equals(other: Any?): Boolean = other is Plural &&
            resource == other.resource &&
            quantity == other.quantity &&
            formatArgs.contentEquals(other.formatArgs)

        override fun hashCode(): Int =
            31 * (31 * resource.hashCode() + quantity) + formatArgs.contentHashCode()
    }
}
