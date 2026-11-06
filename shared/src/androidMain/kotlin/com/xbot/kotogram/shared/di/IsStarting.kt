package com.xbot.kotogram.shared.di

import com.xbot.kotogram.core.account.AccountManager
import com.xbot.kotogram.shared.extensions.toRoute
import org.koin.mp.KoinPlatform

// Holds the system splash until TDLib reports where the shown account stands.
fun isStarting(): Boolean {
    val active = KoinPlatform.getKoin().get<AccountManager>().shown.value.active ?: return true
    return active.authorizationState.value.toRoute() == null
}
