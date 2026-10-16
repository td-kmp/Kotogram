package com.xbot.kotogram

import android.app.Application
import com.xbot.kotogram.ui.di.initKoin
import org.koin.android.ext.koin.androidContext

class KotogramApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@KotogramApplication) }
    }
}
