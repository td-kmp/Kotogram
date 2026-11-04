package com.xbot.kotogram.core.data.environment

import kotlinx.browser.window
import org.koin.core.scope.Scope

// The worker keeps /tdlib in IndexedDB; anything outside it is gone with the worker.
internal actual fun platformTdlibEnvironment(scope: Scope) = TdlibEnvironmentValues(
    databaseDirectory = "/tdlib/db",
    filesDirectory = "/tdlib/files",
    deviceModel = "Web",
    systemVersion = window.navigator.userAgent,
    systemLanguageCode = window.navigator.language.substringBefore('-'),
    applicationVersion = "web"
)
