package com.xbot.kotogram.feature.auth.presentation.navigation

import com.xbot.kotogram.core.navigation.NavKey
import kotlinx.serialization.Serializable

/**
 * Phone number entry on top of the QR entry (desktop only). Elsewhere
 * [com.xbot.kotogram.feature.auth.api.AuthRoute] is the phone entry.
 */
@Serializable
internal data object PhoneNumberRoute : NavKey

/** A login step after the entry screens; each one matches a TDLib authorization state. */
@Serializable
internal sealed interface AuthStepRoute : NavKey

@Serializable
internal data object CodeRoute : AuthStepRoute

@Serializable
internal data object PasswordRoute : AuthStepRoute

@Serializable
internal data object EmailAddressRoute : AuthStepRoute

@Serializable
internal data object EmailCodeRoute : AuthStepRoute

@Serializable
internal data object RegistrationRoute : AuthStepRoute

@Serializable
internal data object PremiumPurchaseRoute : AuthStepRoute
