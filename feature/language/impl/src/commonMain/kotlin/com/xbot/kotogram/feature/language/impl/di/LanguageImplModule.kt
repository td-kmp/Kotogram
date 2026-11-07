package com.xbot.kotogram.feature.language.impl.di

import com.xbot.kotogram.core.account.AppCoroutineScope
import com.xbot.kotogram.core.resources.LocalizationSource
import com.xbot.kotogram.feature.language.impl.localization.DefaultLocalizationSource
import org.koin.dsl.module

val languageImplModule = module {
    single<LocalizationSource> { DefaultLocalizationSource(languages = get(), scope = get(AppCoroutineScope)) }
}
