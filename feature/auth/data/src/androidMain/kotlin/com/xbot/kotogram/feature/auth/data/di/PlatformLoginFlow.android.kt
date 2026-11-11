package com.xbot.kotogram.feature.auth.data.di

import com.xbot.kotogram.feature.auth.domain.model.LoginFlow

internal actual val platformLoginFlow: LoginFlow = LoginFlow.PhoneNumberOnly
