package com.xbot.kotogram.feature.language.data.di

import com.xbot.kotogram.core.account.AccountScope
import com.xbot.kotogram.core.account.AccountUpdates
import com.xbot.kotogram.core.account.AppCoroutineScope
import com.xbot.kotogram.core.account.AppStorage
import com.xbot.kotogram.core.account.TdlibOptions
import com.xbot.kotogram.feature.language.data.account.LanguageOptions
import com.xbot.kotogram.feature.language.data.account.LanguagePackUpdates
import com.xbot.kotogram.feature.language.data.account.SuggestedLanguagePack
import com.xbot.kotogram.feature.language.data.repository.DefaultLanguageRepository
import com.xbot.kotogram.feature.language.data.repository.DefaultLanguageSuggestionRepository
import com.xbot.kotogram.feature.language.data.store.ChosenLanguageStore
import com.xbot.kotogram.feature.language.data.store.DataStoreChosenLanguageStore
import com.xbot.kotogram.feature.language.domain.repository.LanguageRepository
import com.xbot.kotogram.feature.language.domain.repository.LanguageSuggestionRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.scopedOf
import org.koin.dsl.bind
import org.koin.dsl.module

val languageDataModule = module {
    single<ChosenLanguageStore> { DataStoreChosenLanguageStore(get<AppStorage>().preferences) }
    single {
        DefaultLanguageRepository(
            appScope = get(AppCoroutineScope),
            client = get(),
            store = get(),
            platform = platformLanguage(),
        )
    } bind LanguageRepository::class
    scope(AccountScope) {
        scopedOf(::LanguageOptions) { bind<TdlibOptions>() }
        scopedOf(::LanguagePackUpdates) { bind<AccountUpdates>() }
        scopedOf(::SuggestedLanguagePack) { bind<AccountUpdates>() }
        scopedOf(::DefaultLanguageSuggestionRepository) { bind<LanguageSuggestionRepository>() }
    }
}
