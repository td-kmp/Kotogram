package com.xbot.kotogram.feature.auth.presentation.di

import com.xbot.kotogram.feature.auth.domain.usecase.GetLoginMethodsUseCase
import com.xbot.kotogram.feature.auth.domain.usecase.LoginWithPasskeyUseCase
import com.xbot.kotogram.feature.auth.domain.usecase.ObserveAuthStateUseCase
import com.xbot.kotogram.feature.auth.presentation.code.CodeViewModel
import com.xbot.kotogram.feature.auth.presentation.email.EmailAddressViewModel
import com.xbot.kotogram.feature.auth.presentation.email.EmailCodeViewModel
import com.xbot.kotogram.feature.auth.presentation.flow.AuthFlowViewModel
import com.xbot.kotogram.feature.auth.presentation.password.PasswordViewModel
import com.xbot.kotogram.feature.auth.presentation.phone.PhoneViewModel
import com.xbot.kotogram.feature.auth.presentation.qr.QrViewModel
import com.xbot.kotogram.feature.auth.presentation.registration.RegistrationViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authPresentationModule: Module = module {
    factoryOf(::ObserveAuthStateUseCase)
    factoryOf(::LoginWithPasskeyUseCase)
    factory { GetLoginMethodsUseCase(supportsQrLogin, get()) }

    viewModel { AuthFlowViewModel(get(), get(), get(), get(), supportsQrLogin) }
    viewModelOf(::QrViewModel)
    viewModelOf(::PhoneViewModel)
    viewModelOf(::CodeViewModel)
    viewModelOf(::PasswordViewModel)
    viewModelOf(::EmailAddressViewModel)
    viewModelOf(::EmailCodeViewModel)
    viewModelOf(::RegistrationViewModel)
}

/** QR login exists only on desktop, where a phone can scan the screen. */
internal expect val supportsQrLogin: Boolean
