package com.xbot.kotogram.core.tdlib.di

import com.xbot.kotogram.core.tdlib.environment.TdlibEnvironmentValues
import org.koin.core.scope.Scope

/** Android reads its Context from [scope]; no other platform looks at it. */
internal expect fun platformTdlibEnvironment(scope: Scope): TdlibEnvironmentValues
