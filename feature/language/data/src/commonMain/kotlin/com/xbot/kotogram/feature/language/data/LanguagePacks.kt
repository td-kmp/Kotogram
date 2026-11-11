package com.xbot.kotogram.feature.language.data

internal const val LOCALIZATION_TARGET = "macos"

internal const val BUNDLED_LANGUAGE_PACK_ID = "en"

internal val SupportedLanguageTags = listOf(
    "en", "be", "ca", "zh-Hans", "zh-Hant", "hr", "cs", "nl", "fi", "fr", "de", "he", "it",
    "kk", "ko", "ms", "nb", "pl", "pt-BR", "ro", "ru", "sr", "es", "tr", "uk",
)

internal fun languagePackIdOf(preferredLanguageTags: List<String>): String =
    preferredLanguageTags.firstNotNullOfOrNull(::supportedTagOf)?.lowercase() ?: BUNDLED_LANGUAGE_PACK_ID

internal fun languageTagOf(languagePackId: String): String? =
    SupportedLanguageTags.firstOrNull { it.equals(languagePackId, ignoreCase = true) }

private fun supportedTagOf(languageTag: String): String? {
    val subtags = languageTag.replace('_', '-').split('-')
    val language = subtags.first().lowercase().let { LegacyLanguageCodes[it] ?: it }
    val candidates = SupportedLanguageTags.filter { it.substringBefore('-') == language }
    if (candidates.size <= 1) return candidates.firstOrNull()
    val script = subtags.firstOrNull { it.length == SCRIPT_LENGTH }
        ?: if (subtags.any { it.uppercase() in TraditionalChineseRegions }) TRADITIONAL_SCRIPT else SIMPLIFIED_SCRIPT
    return candidates.firstOrNull { it.substringAfter('-').equals(script, ignoreCase = true) }
}

private val LegacyLanguageCodes = mapOf("iw" to "he", "no" to "nb")
private val TraditionalChineseRegions = setOf("TW", "HK", "MO")
private const val SCRIPT_LENGTH = 4
private const val SIMPLIFIED_SCRIPT = "Hans"
private const val TRADITIONAL_SCRIPT = "Hant"
