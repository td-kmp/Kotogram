package com.xbot.kotogram.feature.auth.impl.screen.auth

internal sealed interface LanguageSuggestionAction {
    data object OnContinueClick : LanguageSuggestionAction
}
