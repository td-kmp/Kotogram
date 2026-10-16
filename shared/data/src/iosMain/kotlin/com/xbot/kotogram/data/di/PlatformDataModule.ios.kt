package com.xbot.kotogram.data.di

import com.xbot.kotogram.data.tdlib.TdlibEnvironment
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.UIKit.UIDevice

internal actual val platformDataModule: Module = module {
    single {
        val supportDir = NSSearchPathForDirectoriesInDomains(
            NSApplicationSupportDirectory,
            NSUserDomainMask,
            true
        )
            .first() as String
        val device = UIDevice.currentDevice
        TdlibEnvironment(
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
}
