package com.xbot.kotogram.core.tdlib.di

import com.xbot.kotogram.core.tdlib.environment.TdlibEnvironmentValues
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
