package com.xbot.kotogram.core.tdlib.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.xbot.kotogram.core.tdlib.account.AccountStorage
import com.xbot.kotogram.core.tdlib.account.DirectoryAccountStorage
import com.xbot.kotogram.core.tdlib.environment.TdlibEnvironment
import okio.Path.Companion.toPath

internal actual fun platformAccountStorage(environment: TdlibEnvironment): AccountStorage =
    DirectoryAccountStorage(
        environment = environment,
        preferences = PreferenceDataStoreFactory.createWithPath {
            "${environment.databaseDirectory}/accounts.preferences_pb".toPath()
        }
    )
