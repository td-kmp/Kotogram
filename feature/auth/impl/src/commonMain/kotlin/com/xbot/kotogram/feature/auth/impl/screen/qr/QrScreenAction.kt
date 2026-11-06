package com.xbot.kotogram.feature.auth.impl.screen.qr

internal sealed interface QrScreenAction {
    data object OnRequestQrCode : QrScreenAction
}
