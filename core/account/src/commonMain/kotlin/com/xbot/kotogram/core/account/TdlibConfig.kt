package com.xbot.kotogram.core.account

import io.xbot.tdlib.api.SetTdlibParameters

data class TdlibConfig(
    val apiId: Int,
    val apiHash: String,
    val useTestDc: Boolean,
    val systemLanguageCode: String,
    val deviceModel: String,
    val systemVersion: String,
    val applicationVersion: String
)

internal fun tdlibConfigOf(
    systemLanguageCode: String,
    deviceModel: String,
    systemVersion: String,
    applicationVersion: String
): TdlibConfig {
    check(TelegramApiConfig.API_ID != 0 && TelegramApiConfig.API_HASH.isNotEmpty()) {
        "Telegram API credentials are missing: set telegram.api.id and telegram.api.hash in local.properties"
    }
    return TdlibConfig(
        apiId = TelegramApiConfig.API_ID,
        apiHash = TelegramApiConfig.API_HASH,
        useTestDc = TelegramApiConfig.USE_TEST_DC,
        systemLanguageCode = systemLanguageCode,
        deviceModel = deviceModel,
        systemVersion = systemVersion,
        applicationVersion = applicationVersion
    )
}

internal fun TdlibConfig.parameters(
    id: AccountId,
    storage: AccountStorage,
    databaseKey: ByteArray
): SetTdlibParameters = SetTdlibParameters(
    useTestDc = useTestDc,
    databaseDirectory = storage.databaseDirectory(id),
    filesDirectory = storage.filesDirectory(id),
    databaseEncryptionKey = databaseKey,
    useFileDatabase = true,
    useChatInfoDatabase = true,
    useMessageDatabase = true,
    useSecretChats = true,
    apiId = apiId,
    apiHash = apiHash,
    systemLanguageCode = systemLanguageCode,
    deviceModel = deviceModel,
    systemVersion = systemVersion,
    applicationVersion = applicationVersion
)
