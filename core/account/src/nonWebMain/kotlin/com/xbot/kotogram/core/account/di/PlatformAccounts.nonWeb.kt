package com.xbot.kotogram.core.account.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.xbot.kotogram.core.account.AccountStorage
import com.xbot.kotogram.core.account.AppStorage
import com.xbot.kotogram.core.account.DefaultAppStorage
import com.xbot.kotogram.core.account.DirectoryAccountStorage
import io.xbot.tdlib.NativeTdLib
import io.xbot.tdlib.TdLib
import okio.Path.Companion.toPath
import org.koin.core.scope.Scope

internal expect fun Scope.platformAccountsRoot(): String

internal actual fun platformTdLib(): TdLib = NativeTdLib

internal actual fun Scope.platformAccountStorage(): AccountStorage =
    DirectoryAccountStorage(platformAccountsRoot())

internal actual fun Scope.platformAppStorage(): AppStorage {
    val root = platformAccountsRoot()
    return DefaultAppStorage(
        tdlibDirectory = root,
        preferences = PreferenceDataStoreFactory.createWithPath { "$root/registry.preferences_pb".toPath() }
    )
}
