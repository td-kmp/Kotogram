package com.xbot.kotogram.core.resources

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.PluralStringResource

@Immutable
sealed interface StringResource {
    data class String(val text: kotlin.String) : StringResource

    @Immutable
    class Text(
        val resource: org.jetbrains.compose.resources.StringResource,
        vararg val formatArgs: Any
    ) : StringResource {
        override fun equals(other: Any?): Boolean = other is Text &&
            resource == other.resource &&
            formatArgs.contentEquals(other.formatArgs)

        override fun hashCode(): Int = 31 * resource.hashCode() + formatArgs.contentHashCode()
    }

    @Immutable
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
