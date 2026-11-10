package com.xbot.kotogram.feature.language.impl.localization

import com.xbot.kotogram.core.resources.Localization
import com.xbot.kotogram.feature.language.domain.model.LanguagePack
import io.xbot.tdlib.api.LanguagePackInfo
import io.xbot.tdlib.api.LanguagePackStringValue

internal data class PackLocalization(
    private val pack: LanguagePack,
) : Localization {
    override val language: LanguagePackInfo get() = pack.info

    override fun string(key: String): LanguagePackStringValue? = pack.string(key)
}
