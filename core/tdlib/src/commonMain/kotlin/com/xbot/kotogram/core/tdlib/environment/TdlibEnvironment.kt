package com.xbot.kotogram.core.tdlib.environment

/** Platform facts TDLib needs in setTdlibParameters. */
interface TdlibEnvironment {
    val databaseDirectory: String
    val filesDirectory: String
    val deviceModel: String
    val systemVersion: String
    val systemLanguageCode: String
    val applicationVersion: String
}

internal data class TdlibEnvironmentValues(
    override val databaseDirectory: String,
    override val filesDirectory: String,
    override val deviceModel: String,
    override val systemVersion: String,
    override val systemLanguageCode: String,
    override val applicationVersion: String
) : TdlibEnvironment
