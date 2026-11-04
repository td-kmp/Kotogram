package com.xbot.kotogram.shared.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.includes
import org.koin.plugin.module.dsl.startKoin

@Module
@Configuration
@ComponentScan("com.xbot.kotogram.shared")
class AppModule

/** Every `@Configuration` module on the classpath is included automatically. */
@KoinApplication
internal object KotogramApp

/**
 * Starts Koin once per process; platform shells add their own configuration (Android context).
 * `stopKoin()` closes every account scope, which closes its TDLib session.
 */
fun initKoin(config: KoinConfiguration? = null) {
    startKoin<KotogramApp> {
        logger(KermitKoinLogger(Logger.withTag("Koin")))
        includes(config)
    }
}
