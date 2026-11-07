package com.xbot.kotogram.core.account

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

interface AppStorage {
    val tdlibDirectory: String

    val preferences: DataStore<Preferences>
}

internal class DefaultAppStorage(
    override val tdlibDirectory: String,
    override val preferences: DataStore<Preferences>
) : AppStorage
