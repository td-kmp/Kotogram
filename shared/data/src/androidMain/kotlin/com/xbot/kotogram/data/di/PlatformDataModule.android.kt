package com.xbot.kotogram.data.di

import android.content.Context
import android.os.Build
import com.xbot.kotogram.data.tdlib.TdlibEnvironment
import java.io.File
import java.util.Locale
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformDataModule: Module = module {
    single {
        val context = get<Context>()
        TdlibEnvironment(
            databaseDirectory = File(context.filesDir, "tdlib").absolutePath,
            filesDirectory = File(context.filesDir, "tdlib-files").absolutePath,
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            systemVersion = "Android ${Build.VERSION.RELEASE}",
            systemLanguageCode = Locale.getDefault().language,
            applicationVersion =
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        )
    }
}
