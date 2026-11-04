package com.xbot.kotogram.core.tdlib.di

import io.xbot.tdlib.NativeTdLib
import io.xbot.tdlib.TdLib

internal actual fun platformTdLib(): TdLib = NativeTdLib
