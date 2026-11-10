package com.xbot.kotogram.feature.language.data.extensions

import com.xbot.kotogram.feature.language.domain.model.LanguagePack
import io.xbot.tdlib.api.LanguagePackString
import io.xbot.tdlib.api.LanguagePackStringValue
import io.xbot.tdlib.api.LanguagePackStringValueDeleted
import io.xbot.tdlib.api.UpdateLanguagePackStrings

internal fun LanguagePack.withChanges(update: UpdateLanguagePackStrings): LanguagePack = when (update.languagePackId) {
    info.id -> copy(strings = strings.withChanges(update.strings))
    info.baseLanguagePackId -> copy(baseStrings = baseStrings.withChanges(update.strings))
    else -> this
}

private fun Map<String, LanguagePackStringValue>.withChanges(changes: List<LanguagePackString>) = toMutableMap().apply {
    changes.forEach { if (it.value is LanguagePackStringValueDeleted) remove(it.key) else put(it.key, it.value) }
}
