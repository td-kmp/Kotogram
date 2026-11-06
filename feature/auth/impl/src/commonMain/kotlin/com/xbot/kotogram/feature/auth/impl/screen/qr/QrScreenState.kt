package com.xbot.kotogram.feature.auth.impl.screen.qr

import com.xbot.kotogram.feature.auth.domain.model.AuthError

internal data class QrScreenState(
    val link: String? = null,
    val needsQrCode: Boolean = false,
    val error: AuthError? = null
)
