package com.xbot.kotogram

import android.app.Application
import com.xbot.kotogram.shared.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinConfiguration

class KotogramApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(koinConfiguration { androidContext(this@KotogramApplication) })
    }
}
