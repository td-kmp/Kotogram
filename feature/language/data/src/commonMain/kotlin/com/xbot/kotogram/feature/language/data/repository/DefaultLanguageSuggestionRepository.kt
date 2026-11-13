package com.xbot.kotogram.feature.language.data.repository

import com.xbot.kotogram.core.account.extensions.sendCatching
import com.xbot.kotogram.feature.language.data.BUNDLED_LANGUAGE_PACK_ID
import com.xbot.kotogram.feature.language.data.account.SuggestedLanguagePack
import com.xbot.kotogram.feature.language.domain.model.LanguageSuggestion
import com.xbot.kotogram.feature.language.domain.repository.LanguageSuggestionRepository
import io.xbot.tdlib.api.GetLanguagePackStrings
import io.xbot.tdlib.api.LanguagePackStringValueOrdinary
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest

internal class DefaultLanguageSuggestionRepository(
    suggested: SuggestedLanguagePack,
    languages: DefaultLanguageRepository,
    private val client: TdClient,
) : LanguageSuggestionRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val suggestion: Flow<LanguageSuggestion?> =
        combine(suggested.languagePackId, languages.languagePackId) { suggestedId, chosenId ->
            suggestedId.takeIf { it.isNotEmpty() && it != chosenId.ifEmpty { BUNDLED_LANGUAGE_PACK_ID } }
        }
            .distinctUntilChanged()
            .mapLatest { languagePackId -> languagePackId?.let { suggestionOf(it) } }

    private suspend fun suggestionOf(languagePackId: String): LanguageSuggestion? {
        val strings = client.sendCatching(
            GetLanguagePackStrings(languagePackId = languagePackId, keys = [CONTINUE_KEY]),
        ).getOrNull() ?: return null
        val text = strings.strings.firstOrNull { it.key == CONTINUE_KEY }?.value as? LanguagePackStringValueOrdinary
        return text?.let { LanguageSuggestion(languagePackId, it.value) }
    }

    private companion object {
        const val CONTINUE_KEY = "Login.ContinueOnLanguage"
    }
}
