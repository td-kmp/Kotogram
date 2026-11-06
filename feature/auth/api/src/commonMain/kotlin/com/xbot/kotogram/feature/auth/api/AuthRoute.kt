package com.xbot.kotogram.feature.auth.api

import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object AuthRoute : NavKey

fun Navigator.navigateToAuth() {
    navigate(AuthRoute)
}
