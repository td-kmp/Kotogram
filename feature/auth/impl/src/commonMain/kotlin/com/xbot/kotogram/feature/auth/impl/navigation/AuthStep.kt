package com.xbot.kotogram.feature.auth.impl.navigation

// Not saved: the flow's stack follows TDLib's state.
internal sealed interface AuthStep {
    data object QrCode : AuthStep

    data object PhoneNumber : AuthStep

    data object Code : AuthStep

    data object Password : AuthStep

    data object EmailAddress : AuthStep

    data object EmailCode : AuthStep

    data object Registration : AuthStep

    data object PremiumPurchase : AuthStep
}
