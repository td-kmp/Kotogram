package com.xbot.kotogram.shared.di

import com.xbot.kotogram.core.account.AccountManager
import com.xbot.kotogram.core.resources.LocalizationSource
import com.xbot.kotogram.shared.extensions.toRoute
import org.koin.mp.KoinPlatform

// Holds the system splash until the chosen language is read and TDLib reports where the shown account stands.
fun isStarting(): Boolean {
    val koin = KoinPlatform.getKoin()
    val active = koin.get<AccountManager>().shown.value.active
    return koin.get<LocalizationSource>().localization.value == null ||
        active == null ||
        active.authorizationState.value.toRoute() == null
}
