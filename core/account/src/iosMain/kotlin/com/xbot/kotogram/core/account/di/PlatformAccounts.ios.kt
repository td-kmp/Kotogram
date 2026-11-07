package com.xbot.kotogram.core.account.di

import com.xbot.kotogram.core.account.DatabaseKeys
import com.xbot.kotogram.core.account.NoDatabaseKeys
import com.xbot.kotogram.core.account.TdlibConfig
import com.xbot.kotogram.core.account.accountsDirectoryName
import com.xbot.kotogram.core.account.tdlibConfigOf
import org.koin.core.scope.Scope
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.preferredLanguages
import platform.UIKit.UIDevice

internal actual fun Scope.platformAccountsRoot(): String {
    val supportDirectory = NSSearchPathForDirectoriesInDomains(
        NSApplicationSupportDirectory,
        NSUserDomainMask,
        true
    )
        .first() as String
    return "$supportDirectory/$accountsDirectoryName"
}

internal actual fun Scope.platformTdlibConfig(): TdlibConfig {
    val device = UIDevice.currentDevice
    return tdlibConfigOf(
        systemLanguageCode = NSLocale.preferredLanguages.firstOrNull() as? String ?: "en",
        deviceModel = device.model,
        systemVersion = "${device.systemName} ${device.systemVersion}",
        applicationVersion =
            NSBundle.mainBundle.objectForInfoDictionaryKey(
                "CFBundleShortVersionString"
            ) as? String
                ?: ""
    )
}

internal actual fun Scope.platformDatabaseKeys(): DatabaseKeys = NoDatabaseKeys
