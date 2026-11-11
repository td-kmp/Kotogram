package com.xbot.kotogram.feature.language.data

import com.xbot.kotogram.feature.language.data.platform.PlatformLanguage
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakePlatformLanguage(
    chosenInSystem: String? = null,
) : PlatformLanguage {
    override val languagePackId = MutableStateFlow(chosenInSystem)

    val set = mutableListOf<String>()

    override suspend fun set(languagePackId: String) {
        set += languagePackId
    }
}
