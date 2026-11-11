package com.xbot.kotogram.feature.language.data.platform

import kotlinx.coroutines.flow.Flow

internal interface PlatformLanguage {
    val languagePackId: Flow<String?>

    fun sync(languagePackId: String)
}
