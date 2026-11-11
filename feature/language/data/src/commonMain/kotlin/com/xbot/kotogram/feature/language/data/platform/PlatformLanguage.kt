package com.xbot.kotogram.feature.language.data.platform

import kotlinx.coroutines.flow.Flow

internal interface PlatformLanguage {
    val languagePackId: Flow<String?>

    suspend fun set(languagePackId: String)
}
