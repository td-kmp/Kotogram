package com.xbot.kotogram.feature.auth.impl

import arrow.core.Either
import arrow.core.right
import com.xbot.kotogram.feature.language.domain.model.LanguageError
import com.xbot.kotogram.feature.language.domain.model.LanguageState
import com.xbot.kotogram.feature.language.domain.model.LanguageSuggestion
import com.xbot.kotogram.feature.language.domain.repository.LanguageRepository
import com.xbot.kotogram.feature.language.domain.repository.LanguageSuggestionRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeLanguageRepository : LanguageRepository {
    override val state = MutableStateFlow<LanguageState>(LanguageState.Bundled)

    val applied = mutableListOf<String>()

    var switched = CompletableDeferred(Unit)

    override suspend fun apply(languagePackId: String): Either<LanguageError, Unit> {
        applied += languagePackId
        switched.await()
        return Unit.right()
    }
}

internal class FakeLanguageSuggestionRepository : LanguageSuggestionRepository {
    override val suggestion = MutableStateFlow<LanguageSuggestion?>(null)
}
