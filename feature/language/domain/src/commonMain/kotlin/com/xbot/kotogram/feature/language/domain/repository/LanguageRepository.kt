package com.xbot.kotogram.feature.language.domain.repository

import arrow.core.Either
import com.xbot.kotogram.feature.language.domain.model.LanguageError
import com.xbot.kotogram.feature.language.domain.model.LanguageState
import kotlinx.coroutines.flow.StateFlow

interface LanguageRepository {
    val state: StateFlow<LanguageState>

    suspend fun apply(languagePackId: String): Either<LanguageError, Unit>
}
