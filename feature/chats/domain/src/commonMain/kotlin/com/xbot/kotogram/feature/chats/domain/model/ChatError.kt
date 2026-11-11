package com.xbot.kotogram.feature.chats.domain.model

sealed interface ChatError {
    data class Unknown(
        val message: String,
    ) : ChatError
}
