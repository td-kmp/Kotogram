package com.xbot.kotogram.feature.auth.data.di

import com.xbot.kotogram.feature.auth.data.repository.TdAuthRepository
import com.xbot.kotogram.feature.auth.data.repository.TdCountryRepository
import com.xbot.kotogram.feature.auth.data.repository.TdPasskeyRepository
import com.xbot.kotogram.feature.auth.data.repository.UnavailablePasskeyAuthenticator
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import com.xbot.kotogram.feature.auth.domain.repository.CountryRepository
import com.xbot.kotogram.feature.auth.domain.repository.PasskeyAuthenticator
import com.xbot.kotogram.feature.auth.domain.repository.PasskeyRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authDataModule: Module = module {
    singleOf(::TdAuthRepository) bind AuthRepository::class
    singleOf(::TdCountryRepository) bind CountryRepository::class
    singleOf(::TdPasskeyRepository) bind PasskeyRepository::class
    singleOf(::UnavailablePasskeyAuthenticator) bind PasskeyAuthenticator::class
}
