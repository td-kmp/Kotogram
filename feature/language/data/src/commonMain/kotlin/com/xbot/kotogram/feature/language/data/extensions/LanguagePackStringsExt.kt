package com.xbot.kotogram.feature.language.data.extensions

import io.xbot.tdlib.api.LanguagePackStringValue
import io.xbot.tdlib.api.LanguagePackStringValueDeleted
import io.xbot.tdlib.api.LanguagePackStrings

internal fun LanguagePackStrings.toMap(): Map<String, LanguagePackStringValue> =
    strings.filterNot { it.value is LanguagePackStringValueDeleted }.associate { it.key to it.value }
