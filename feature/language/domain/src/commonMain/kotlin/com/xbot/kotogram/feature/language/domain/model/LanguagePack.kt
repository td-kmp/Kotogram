package com.xbot.kotogram.feature.language.domain.model

import io.xbot.tdlib.api.LanguagePackInfo
import io.xbot.tdlib.api.LanguagePackStringValue

data class LanguagePack(
    val info: LanguagePackInfo,
    val strings: Map<String, LanguagePackStringValue>,
    val baseStrings: Map<String, LanguagePackStringValue> = emptyMap(),
) {
    fun string(key: String): LanguagePackStringValue? = strings[key] ?: baseStrings[key]
}
