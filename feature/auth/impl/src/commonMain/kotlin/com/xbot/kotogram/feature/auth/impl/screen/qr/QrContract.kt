package com.xbot.kotogram.feature.auth.impl.screen.qr

import com.xbot.kotogram.feature.auth.domain.model.AuthError

internal data class QrScreenState(
    /** The tg://login link to render as a QR code; null until TDLib issues one. */
    val link: String? = null,
    /** TDLib waits for a phone number, so the visible QR screen asks for a code. */
    val needsQrCode: Boolean = false,
    val error: AuthError? = null
)

internal sealed interface QrScreenAction {
    data object OnRequestQrCode : QrScreenAction
}
