package com.xbot.kotogram.core.data.client

import io.xbot.tdlib.TdLib
import org.koin.core.annotation.Singleton

internal interface TdLibProvider {
    val tdLib: TdLib
}

// TdReceiver runs one loop per TdLib object, so this must stay one object per process: a second
// wrapper would start a second loop that steals the first one's events. Tests bind a fake provider.
@Singleton
internal class PlatformTdLibProvider : TdLibProvider {
    override val tdLib: TdLib = platformTdLib()
}

internal expect fun platformTdLib(): TdLib

/** For closing TDLib from a scope callback: waits where the platform can, the web can't block. */
internal expect fun runAtExit(block: suspend () -> Unit)
