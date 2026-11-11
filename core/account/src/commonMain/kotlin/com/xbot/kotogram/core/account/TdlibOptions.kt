package com.xbot.kotogram.core.account

import io.xbot.tdlib.api.OptionValue
import kotlinx.coroutines.flow.Flow

interface TdlibOptions {
    val options: Flow<Map<String, OptionValue>>
}
