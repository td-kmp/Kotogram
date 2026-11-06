package com.xbot.kotogram.core.account.di

import androidx.datastore.core.okio.WebLocalStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.PreferencesSerializer
import com.xbot.kotogram.core.account.AccountRegistryStore
import com.xbot.kotogram.core.account.AccountStorage
import com.xbot.kotogram.core.account.BrowserAccountStorage
import com.xbot.kotogram.core.account.DataStoreAccountRegistryStore
import com.xbot.kotogram.core.account.DatabaseKeys
import com.xbot.kotogram.core.account.NoDatabaseKeys
import com.xbot.kotogram.core.account.TdlibConfig
import com.xbot.kotogram.core.account.accountsDirectoryName
import com.xbot.kotogram.core.account.tdlibConfigOf
import io.xbot.tdlib.TdLib
import io.xbot.tdlib.WorkerTdLib
import kotlin.js.ExperimentalWasmJsInterop
import kotlinx.browser.window
import org.koin.core.scope.Scope
import org.w3c.dom.Worker

internal actual fun platformTdLib(): TdLib = WorkerTdLib(createWorker())

@OptIn(ExperimentalWasmJsInterop::class)
private fun createWorker(): Worker =
    js("""new Worker(new URL("tdlib-worker/worker.js", import.meta.url), { type: "module" })""")

internal actual fun Scope.platformTdlibConfig(): TdlibConfig = tdlibConfigOf(
    systemLanguageCode = window.navigator.language.substringBefore('-'),
    deviceModel = "Web",
    systemVersion = window.navigator.userAgent,
    applicationVersion = "web"
)

internal actual fun Scope.platformAccountStorage(): AccountStorage =
    BrowserAccountStorage("/tdlib/$accountsDirectoryName")

// Not createWithPath: on the web it means sessionStorage, and closing the tab would lose the accounts.
internal actual fun Scope.platformAccountRegistryStore(): AccountRegistryStore =
    DataStoreAccountRegistryStore(
        PreferenceDataStoreFactory.create(
            storage = WebLocalStorage(
                serializer = PreferencesSerializer,
                name = "kotogram/$accountsDirectoryName/registry"
            )
        )
    )

internal actual fun Scope.platformDatabaseKeys(): DatabaseKeys = NoDatabaseKeys
