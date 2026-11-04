package com.xbot.kotogram.core.tdlib.di

import com.xbot.kotogram.core.tdlib.account.AccountStorage
import com.xbot.kotogram.core.tdlib.environment.TdlibEnvironment
import com.xbot.kotogram.core.tdlib.extensions.forDataCenters
import io.xbot.tdlib.TdLib
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import org.koin.core.scope.Scope

// Functions return interfaces: the compiler plugin checks a concrete return type's constructor.
@Module
@Configuration
@ComponentScan("com.xbot.kotogram.core.tdlib")
class CoreTdlibModule {
    // TdReceiver runs one loop per TdLib object, so this hands out the library's own object: a
    // wrapper created twice would start a second loop that steals the first one's events.
    @Singleton
    internal fun tdLib(): TdLib = platformTdLib()

    @Singleton
    internal fun tdlibEnvironment(scope: Scope): TdlibEnvironment =
        platformTdlibEnvironment(scope).forDataCenters()

    @Singleton
    internal fun accountStorage(environment: TdlibEnvironment): AccountStorage =
        platformAccountStorage(environment)
}
