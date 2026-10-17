package com.xbot.kotogram.core.tdlib

import java.io.File
import java.util.Locale
import org.koin.core.scope.Scope

internal actual fun platformTdlibEnvironment(scope: Scope): TdlibEnvironmentValues {
    val dataDir = File(appDataDirectory(), "Kotogram")
    return TdlibEnvironmentValues(
        databaseDirectory = File(dataDir, "tdlib").absolutePath,
        filesDirectory = File(dataDir, "tdlib-files").absolutePath,
        deviceModel = "Desktop",
        systemVersion = "${System.getProperty("os.name")} ${System.getProperty("os.version")}",
        systemLanguageCode = Locale.getDefault().language,
        applicationVersion = System.getProperty("jpackage.app-version") ?: "dev"
    )
}

private fun appDataDirectory(): File {
    val home = System.getProperty("user.home")
    val os = System.getProperty("os.name").lowercase()
    return when {
        "mac" in os -> File(home, "Library/Application Support")
        "win" in os -> File(System.getenv("APPDATA") ?: home)
        else -> File(System.getenv("XDG_DATA_HOME") ?: "$home/.local/share")
    }
}
