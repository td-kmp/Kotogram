package com.xbot.kotogram.feature.auth.data.di

import com.xbot.kotogram.feature.auth.domain.model.LoginFlow

// The client's call, as in the official apps: TDLib drops the server's qr_login_code (docs/design/auth.md).
internal expect val platformLoginFlow: LoginFlow
