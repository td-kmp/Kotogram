package com.xbot.kotogram.feature.auth.api

// Not saved: the flow's stack follows TDLib's state.
sealed interface AuthStep {
    data object QrCode : AuthStep

    data object PhoneNumber : AuthStep

    data object Code : AuthStep

    data object Password : AuthStep

    data object EmailAddress : AuthStep

    data object EmailCode : AuthStep

    data object Registration : AuthStep

    data object PremiumPurchase : AuthStep
}
