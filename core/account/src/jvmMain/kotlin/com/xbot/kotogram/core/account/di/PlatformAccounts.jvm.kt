package com.xbot.kotogram.core.account.di

import com.xbot.kotogram.core.account.DatabaseKeys
import com.xbot.kotogram.core.account.NoDatabaseKeys
import com.xbot.kotogram.core.account.TdlibConfig
import com.xbot.kotogram.core.account.accountsDirectoryName
import com.xbot.kotogram.core.account.tdlibConfigOf
import java.io.File
import java.util.Locale
import org.koin.core.scope.Scope

internal actual fun Scope.platformAccountsRoot(): String =
    File(File(appDataDirectory(), "Kotogram"), accountsDirectoryName).absolutePath

internal actual fun Scope.platformTdlibConfig(): TdlibConfig = tdlibConfigOf(
    systemLanguageCode = Locale.getDefault().toLanguageTag(),
    deviceModel = "Desktop",
    systemVersion = "${System.getProperty("os.name")} ${System.getProperty("os.version")}",
    applicationVersion = System.getProperty("jpackage.app-version") ?: "dev"
)

internal actual fun Scope.platformDatabaseKeys(): DatabaseKeys = NoDatabaseKeys

private fun appDataDirectory(): File {
    val home = System.getProperty("user.home")
    val os = System.getProperty("os.name").lowercase()
    return when {
        "mac" in os -> File(home, "Library/Application Support")
        "win" in os -> File(System.getenv("APPDATA") ?: home)
        else -> File(System.getenv("XDG_DATA_HOME") ?: "$home/.local/share")
    }
}
