package com.xbot.kotogram.feature.auth.domain.repository

/** What the platform lets the user log in with, independent of the account. */
interface LoginCapabilities {
    /** QR login exists only where the user can scan with another device (desktop), as in the official apps. */
    val supportsQrLogin: Boolean
}
