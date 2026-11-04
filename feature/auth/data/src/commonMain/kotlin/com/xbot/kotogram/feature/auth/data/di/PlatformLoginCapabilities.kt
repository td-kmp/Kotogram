package com.xbot.kotogram.feature.auth.data.di

import com.xbot.kotogram.feature.auth.domain.repository.LoginCapabilities
import org.koin.core.annotation.Singleton

@Singleton
internal class PlatformLoginCapabilities : LoginCapabilities {
    override val supportsQrLogin: Boolean = platformSupportsQrLogin
}

internal expect val platformSupportsQrLogin: Boolean
