package com.xbot.kotogram.core.account.di

import com.xbot.kotogram.core.account.AccountStorage
import com.xbot.kotogram.core.account.AppStorage
import com.xbot.kotogram.core.account.DatabaseKeys
import com.xbot.kotogram.core.account.TdlibConfig
import io.xbot.tdlib.TdLib
import org.koin.core.scope.Scope

internal expect fun platformTdLib(): TdLib

internal expect fun Scope.platformTdlibConfig(): TdlibConfig

internal expect fun Scope.platformAccountStorage(): AccountStorage

internal expect fun Scope.platformAppStorage(): AppStorage

internal expect fun Scope.platformDatabaseKeys(): DatabaseKeys
