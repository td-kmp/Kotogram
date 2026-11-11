package com.xbot.kotogram.feature.auth.data.di

import com.xbot.kotogram.core.account.AccountScope
import com.xbot.kotogram.core.account.AccountUpdates
import com.xbot.kotogram.feature.auth.data.account.GeneratedPhotos
import com.xbot.kotogram.feature.auth.data.repository.DefaultAuthRepository
import com.xbot.kotogram.feature.auth.data.repository.DefaultCountryRepository
import com.xbot.kotogram.feature.auth.domain.LoginEntry
import com.xbot.kotogram.feature.auth.domain.model.LoginFlow
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.auth.domain.repository.CountryRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.scopedOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val authDataModule = module {
    single<LoginFlow> { platformLoginFlow }
    singleOf(::LoginEntry)
    scope(AccountScope) {
        scopedOf(::DefaultAuthRepository) { bind<AuthRepository>() }
        scopedOf(::DefaultCountryRepository) { bind<CountryRepository>() }
        scopedOf(::GeneratedPhotos) { bind<AccountUpdates>() }
    }
}
