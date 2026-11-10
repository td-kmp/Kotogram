package com.xbot.kotogram.feature.auth.impl.di

import com.xbot.kotogram.core.account.AccountScope
import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.navigation.polymorphic
import com.xbot.kotogram.feature.auth.api.AuthRoute
import com.xbot.kotogram.feature.auth.impl.screen.auth.AuthScreen
import com.xbot.kotogram.feature.auth.impl.screen.auth.AuthViewModel
import com.xbot.kotogram.feature.auth.impl.screen.auth.LanguageSuggestionViewModel
import com.xbot.kotogram.feature.auth.impl.screen.code.CodeViewModel
import com.xbot.kotogram.feature.auth.impl.screen.emailaddress.EmailAddressViewModel
import com.xbot.kotogram.feature.auth.impl.screen.emailcode.EmailCodeViewModel
import com.xbot.kotogram.feature.auth.impl.screen.password.PasswordViewModel
import com.xbot.kotogram.feature.auth.impl.screen.phone.PhoneViewModel
import com.xbot.kotogram.feature.auth.impl.screen.qr.QrViewModel
import com.xbot.kotogram.feature.auth.impl.screen.registration.RegistrationViewModel
import kotlinx.serialization.modules.subclass
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val authImplModule = module {
    polymorphic<NavKey> {
        subclass(AuthRoute::class)
    }

    navigation<AuthRoute> { AuthScreen(viewModel = koinViewModel()) }

    scope(AccountScope) {
        viewModelOf(::AuthViewModel)
        viewModelOf(::LanguageSuggestionViewModel)
        viewModelOf(::PhoneViewModel)
        viewModelOf(::QrViewModel)
        viewModelOf(::CodeViewModel)
        viewModelOf(::PasswordViewModel)
        viewModelOf(::EmailAddressViewModel)
        viewModelOf(::EmailCodeViewModel)
        viewModelOf(::RegistrationViewModel)
    }
}
