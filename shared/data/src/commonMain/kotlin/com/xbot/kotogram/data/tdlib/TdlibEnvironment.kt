package com.xbot.kotogram.data.tdlib

/** Platform facts TDLib needs in setTdlibParameters. */
data class TdlibEnvironment(
    val databaseDirectory: String,
    val filesDirectory: String,
    val deviceModel: String,
    val systemVersion: String,
    val systemLanguageCode: String,
    val applicationVersion: String
)
