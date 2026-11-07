package com.xbot.kotogram.feature.auth.impl.screen.auth

import com.xbot.kotogram.feature.language.domain.model.LanguageSuggestion

internal data class LanguageSuggestionState(
    val suggestion: LanguageSuggestion? = null,
    val isSwitching: Boolean = false
)
