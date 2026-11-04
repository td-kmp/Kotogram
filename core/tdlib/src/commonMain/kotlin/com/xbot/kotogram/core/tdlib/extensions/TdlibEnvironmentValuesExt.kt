package com.xbot.kotogram.core.tdlib.extensions

import com.xbot.kotogram.core.tdlib.TelegramApiConfig
import com.xbot.kotogram.core.tdlib.environment.TdlibEnvironmentValues

// A database created on the production servers can't be used on the test ones, so they live apart.
internal fun TdlibEnvironmentValues.forDataCenters(): TdlibEnvironmentValues =
    if (TelegramApiConfig.USE_TEST_DC) {
        copy(databaseDirectory = "$databaseDirectory/test", filesDirectory = "$filesDirectory/test")
    } else {
        this
    }
