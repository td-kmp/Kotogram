package com.xbot.kotogram.feature.language.domain.model

sealed interface LanguageState {
    data object Loading : LanguageState

    data object Bundled : LanguageState

    data class Loaded(val pack: LanguagePack) : LanguageState
}
