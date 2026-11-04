package com.xbot.kotogram.core.tdlib.di

import com.xbot.kotogram.core.tdlib.account.AccountStorage
import com.xbot.kotogram.core.tdlib.environment.TdlibEnvironment

internal expect fun platformAccountStorage(environment: TdlibEnvironment): AccountStorage
