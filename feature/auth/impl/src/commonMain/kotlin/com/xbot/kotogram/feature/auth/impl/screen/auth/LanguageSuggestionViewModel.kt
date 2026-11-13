package com.xbot.kotogram.feature.auth.impl.screen.auth

import androidx.lifecycle.ViewModel
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.language.domain.repository.LanguageRepository
import com.xbot.kotogram.feature.language.domain.repository.LanguageSuggestionRepository
import kotlinx.coroutines.flow.combine
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

internal class LanguageSuggestionViewModel(
    private val languageSuggestionRepository: LanguageSuggestionRepository,
    private val languageRepository: LanguageRepository,
    private val authRepository: AuthRepository,
) : ViewModel(),
    OrbitContainerHost<LanguageSuggestionState, LanguageSuggestionState, Nothing> {

    override val container = orbitContainer<LanguageSuggestionState, Nothing>(LanguageSuggestionState()) {
        combine(languageSuggestionRepository.suggestion, authRepository.isFirstSignIn) { suggestion, isFirst ->
            suggestion.takeIf { isFirst }
        }.collect { suggestion -> reduce { state.copy(suggestion = suggestion) } }
    }

    fun onAction(action: LanguageSuggestionAction) {
        when (action) {
            OnContinueClick -> onContinueClick()
        }
    }

    private fun onContinueClick() = intent {
        val suggestion = state.suggestion ?: return@intent
        if (state.isSwitching) return@intent
        reduce { state.copy(isSwitching = true) }
        languageRepository.apply(suggestion.languagePackId)
        reduce { state.copy(isSwitching = false) }
    }
}
