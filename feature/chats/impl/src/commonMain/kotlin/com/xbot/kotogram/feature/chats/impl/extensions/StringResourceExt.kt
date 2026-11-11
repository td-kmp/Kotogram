package com.xbot.kotogram.feature.chats.impl.extensions

import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.resources.StringResource
import com.xbot.kotogram.core.resources.relative
import com.xbot.kotogram.core.resources.rememberDateFormats
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.chats.impl.extensions.DateArgument
import com.xbot.kotogram.feature.chats.impl.extensions.NameList

@Composable
internal fun StringResource.resolved(): String = when (val resource = this) {
    is StringResource.Text -> stringResource(
        resource.resource,
        *resource.formatArgs.map {
            it.resolvedArgument()
        }.toTypedArray(),
    )

    else -> stringResource(resource)
}

@Composable
private fun Any.resolvedArgument(): Any = when (val argument = this) {
    is StringResource -> argument.resolved()
    is NameList -> argument.names.map { it.resolvedArgument() }.joinToString(", ")
    is DateArgument -> rememberDateFormats().relative(argument.epochSeconds.toLong())
    else -> argument
}
