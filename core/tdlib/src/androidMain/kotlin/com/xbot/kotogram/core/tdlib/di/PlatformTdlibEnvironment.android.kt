package com.xbot.kotogram.core.tdlib.di

import android.os.Build
import com.xbot.kotogram.core.tdlib.environment.TdlibEnvironmentValues
import java.io.File
import java.util.Locale
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope

internal actual fun platformTdlibEnvironment(scope: Scope): TdlibEnvironmentValues {
    val context = scope.androidContext()
    return TdlibEnvironmentValues(
        databaseDirectory = File(context.filesDir, "tdlib").absolutePath,
        filesDirectory = File(context.filesDir, "tdlib-files").absolutePath,
        deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
        systemVersion = "Android ${Build.VERSION.RELEASE}",
        systemLanguageCode = Locale.getDefault().language,
        applicationVersion =
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
    )
}
