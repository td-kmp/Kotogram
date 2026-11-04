package com.xbot.kotogram.core.tdlib.di

import androidx.datastore.core.okio.WebLocalStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.PreferencesSerializer
import com.xbot.kotogram.core.tdlib.account.AccountStorage
import com.xbot.kotogram.core.tdlib.account.BrowserAccountStorage
import com.xbot.kotogram.core.tdlib.environment.TdlibEnvironment

// Not createWithPath: on the web it means sessionStorage, and closing the tab would lose the accounts.
internal actual fun platformAccountStorage(environment: TdlibEnvironment): AccountStorage =
    BrowserAccountStorage(
        PreferenceDataStoreFactory.create(
            storage = WebLocalStorage(
                serializer = PreferencesSerializer,
                name = "kotogram${environment.databaseDirectory}/accounts"
            )
        )
    )
