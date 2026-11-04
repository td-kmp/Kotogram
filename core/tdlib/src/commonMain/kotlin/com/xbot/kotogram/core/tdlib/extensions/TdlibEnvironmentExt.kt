package com.xbot.kotogram.core.tdlib.extensions

import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.core.tdlib.TelegramApiConfig
import com.xbot.kotogram.core.tdlib.environment.TdlibEnvironment
import io.xbot.tdlib.api.SetTdlibParameters

internal fun TdlibEnvironment.databaseDirectoryOf(id: AccountId) =
    "$databaseDirectory/${id.directoryName}"

internal fun TdlibEnvironment.filesDirectoryOf(id: AccountId) =
    "$filesDirectory/${id.directoryName}"

internal fun TdlibEnvironment.parametersOf(id: AccountId): SetTdlibParameters {
    check(TelegramApiConfig.API_ID != 0 && TelegramApiConfig.API_HASH.isNotEmpty()) {
        "Telegram API credentials are missing: set telegram.api.id and telegram.api.hash in local.properties"
    }
    return SetTdlibParameters(
        useTestDc = TelegramApiConfig.USE_TEST_DC,
        databaseDirectory = databaseDirectoryOf(id),
        filesDirectory = filesDirectoryOf(id),
        useFileDatabase = true,
        useChatInfoDatabase = true,
        useMessageDatabase = true,
        useSecretChats = true,
        apiId = TelegramApiConfig.API_ID,
        apiHash = TelegramApiConfig.API_HASH,
        systemLanguageCode = systemLanguageCode,
        deviceModel = deviceModel,
        systemVersion = systemVersion,
        applicationVersion = applicationVersion
    )
}
