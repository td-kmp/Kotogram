package com.xbot.kotogram.core.account

import io.xbot.tdlib.api.OptionValue
import kotlinx.coroutines.flow.Flow

// An account's options from one owner. The first ones go before setTdlibParameters, so they must come at once.
interface TdlibOptions {
    val options: Flow<Map<String, OptionValue>>
}
