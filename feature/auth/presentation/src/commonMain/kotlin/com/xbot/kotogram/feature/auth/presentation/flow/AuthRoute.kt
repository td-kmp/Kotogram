package com.xbot.kotogram.feature.auth.presentation.flow

/** Screens of the login flow; the back stack is derived from TDLib's authorization state. */
sealed interface AuthRoute {
    data object Loading : AuthRoute

    data object Qr : AuthRoute

    data object Phone : AuthRoute

    data object Code : AuthRoute

    data object EmailAddress : AuthRoute

    data object EmailCode : AuthRoute

    data object Password : AuthRoute

    data object Registration : AuthRoute

    data object Unsupported : AuthRoute
}
