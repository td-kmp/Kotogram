package com.xbot.kotogram.feature.auth.api

import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.navigation.Navigator
import kotlinx.serialization.Serializable

/** The login flow of a TDLib instance; its steps follow TDLib's authorization state. */
@Serializable
data object AuthRoute : NavKey

fun Navigator.navigateToAuth() {
    navigate(AuthRoute)
}
