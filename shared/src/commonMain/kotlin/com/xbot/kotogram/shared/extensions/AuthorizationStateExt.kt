package com.xbot.kotogram.shared.extensions

import com.xbot.kotogram.core.account.extensions.isLoginStep
import com.xbot.kotogram.core.navigation.NavKey
import com.xbot.kotogram.feature.auth.api.AuthRoute
import com.xbot.kotogram.feature.chats.api.ChatListRoute
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateReady

internal fun AuthorizationState?.toRoute(): NavKey? = when {
    this == AuthorizationStateReady -> ChatListRoute
    isLoginStep -> AuthRoute
    else -> null
}
