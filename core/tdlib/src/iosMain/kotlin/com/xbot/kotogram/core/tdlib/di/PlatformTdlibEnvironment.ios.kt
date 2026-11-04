package com.xbot.kotogram.core.tdlib.di

import com.xbot.kotogram.core.tdlib.environment.TdlibEnvironmentValues
import org.koin.core.scope.Scope
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.UIKit.UIDevice

internal actual fun platformTdlibEnvironment(scope: Scope): TdlibEnvironmentValues {
    val supportDir = NSSearchPathForDirectoriesInDomains(
        NSApplicationSupportDirectory,
        NSUserDomainMask,
        true
    )
        .first() as String
    val device = UIDevice.currentDevice
    return TdlibEnvironmentValues(
        databaseDirectory = "$supportDir/tdlib",
        filesDirectory = "$supportDir/tdlib-files",
        deviceModel = device.model,
        systemVersion = "${device.systemName} ${device.systemVersion}",
        systemLanguageCode = NSLocale.currentLocale.languageCode,
        applicationVersion =
            NSBundle.mainBundle.objectForInfoDictionaryKey(
                "CFBundleShortVersionString"
            ) as? String
                ?: ""
    )
}
