package com.xbot.kotogram.feature.language.data.extensions

import com.xbot.kotogram.feature.language.data.LOCALIZATION_TARGET
import io.xbot.tdlib.api.LanguagePackInfo
import io.xbot.tdlib.api.UpdateLanguagePackStrings

internal fun UpdateLanguagePackStrings.isFor(info: LanguagePackInfo): Boolean =
    localizationTarget == LOCALIZATION_TARGET &&
        (languagePackId == info.id || languagePackId == info.baseLanguagePackId)
