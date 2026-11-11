package com.xbot.kotogram.feature.language.impl.extensions

import com.xbot.kotogram.core.resources.BundledLocalization
import com.xbot.kotogram.core.resources.Localization
import com.xbot.kotogram.feature.language.domain.model.LanguageState
import com.xbot.kotogram.feature.language.impl.localization.PackLocalization

internal fun LanguageState.toLocalization(): Localization? = when (this) {
    LanguageState.Loading -> null
    LanguageState.Bundled -> BundledLocalization
    is LanguageState.Loaded -> PackLocalization(pack)
}
