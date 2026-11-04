package com.xbot.kotogram.feature.auth.impl.extensions

import com.xbot.kotogram.core.navigation.Navigator
import com.xbot.kotogram.feature.auth.impl.navigation.AuthStepRoute

internal fun Navigator.clearSteps() {
    while (currentDestination is AuthStepRoute) navigateBack()
}
