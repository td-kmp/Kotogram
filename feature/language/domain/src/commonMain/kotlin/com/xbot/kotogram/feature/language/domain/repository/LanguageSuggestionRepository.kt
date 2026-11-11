package com.xbot.kotogram.feature.language.domain.repository

import com.xbot.kotogram.feature.language.domain.model.LanguageSuggestion
import kotlinx.coroutines.flow.Flow

interface LanguageSuggestionRepository {
    val suggestion: Flow<LanguageSuggestion?>
}
