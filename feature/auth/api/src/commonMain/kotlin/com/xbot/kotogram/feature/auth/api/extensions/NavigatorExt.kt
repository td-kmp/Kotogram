package com.xbot.kotogram.feature.auth.api.extensions

import com.xbot.kotogram.core.navigation.Navigator
import com.xbot.kotogram.feature.auth.api.AuthRoute

fun Navigator.navigateToAuth() {
    navigate(AuthRoute)
}
