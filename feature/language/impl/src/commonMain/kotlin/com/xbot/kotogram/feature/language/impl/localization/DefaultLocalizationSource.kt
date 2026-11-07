package com.xbot.kotogram.feature.language.impl.localization

import com.xbot.kotogram.core.resources.Localization
import com.xbot.kotogram.core.resources.LocalizationSource
import com.xbot.kotogram.feature.language.domain.repository.LanguageRepository
import com.xbot.kotogram.feature.language.impl.extensions.toLocalization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class DefaultLocalizationSource(languages: LanguageRepository, scope: CoroutineScope) : LocalizationSource {
    override val localization: StateFlow<Localization?> = languages.state
        .map { it.toLocalization() }
        .stateIn(scope, SharingStarted.Eagerly, languages.state.value.toLocalization())
}
