package com.xbot.kotogram.catalog.pages

import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.component.QrCode

internal val QrCodePage = CatalogPage(
    id = "qr-code",
    section = CatalogSection.Components,
    name = "QrCode",
    description = "Telegram for macOS's QR code: rounded modules, a cutout for an icon, and the matrix animation " +
        "until a token comes. A new token fades in over the last.",
    examples = [
        CatalogExample("Login token") {
            QrCode(data = LOGIN_TOKEN, contentDescription = "Login QR code")
        },
    ],
)

private const val LOGIN_TOKEN = "tg://login?token=AQFJjO1qHocdoolqLtckd6dPS98k1HhnVDFilU_N_v6psw"
