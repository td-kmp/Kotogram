package com.xbot.kotogram.core.account.di

import android.os.Build
import com.xbot.kotogram.core.account.AndroidDatabaseKeys
import com.xbot.kotogram.core.account.DatabaseKeys
import com.xbot.kotogram.core.account.TdlibConfig
import com.xbot.kotogram.core.account.accountsDirectoryName
import com.xbot.kotogram.core.account.tdlibConfigOf
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope
import java.io.File
import java.util.Locale

internal actual fun Scope.platformAccountsRoot(): String =
    File(androidContext().filesDir, accountsDirectoryName).absolutePath

internal actual fun Scope.platformTdlibConfig(): TdlibConfig {
    val context = androidContext()
    return tdlibConfigOf(
        systemLanguageCode = Locale.getDefault().toLanguageTag(),
        deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
        systemVersion = "Android ${Build.VERSION.RELEASE}",
        applicationVersion =
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "",
    )
}

internal actual fun Scope.platformDatabaseKeys(): DatabaseKeys = AndroidDatabaseKeys(androidContext())
