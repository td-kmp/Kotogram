package com.xbot.kotogram.feature.language.domain.model

sealed interface LanguageError {
    data class Unknown(
        val message: String,
    ) : LanguageError
}
