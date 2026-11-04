package com.xbot.kotogram.core.data.environment

import com.xbot.kotogram.core.data.TelegramApiConfig
import com.xbot.kotogram.core.domain.model.AccountId
import org.koin.core.annotation.Singleton
import org.koin.core.scope.Scope

/** Platform facts TDLib needs in setTdlibParameters. */
interface TdlibEnvironment {
    val databaseDirectory: String
    val filesDirectory: String
    val deviceModel: String
    val systemVersion: String
    val systemLanguageCode: String
    val applicationVersion: String
}

// A class, not a provider function: the compiler plugin validates function definitions across
// modules against the returned type's constructor (KOIN-D001 on TdlibEnvironment's parameters).
@Singleton
internal class PlatformTdlibEnvironment(scope: Scope) :
    TdlibEnvironment by platformTdlibEnvironment(scope).forDataCenters()

/** Android reads its Context from [scope]. */
internal expect fun platformTdlibEnvironment(scope: Scope): TdlibEnvironmentValues

// A database created on the production servers can't be used on the test ones, so they live apart.
private fun TdlibEnvironmentValues.forDataCenters(): TdlibEnvironmentValues =
    if (TelegramApiConfig.USE_TEST_DC) {
        copy(databaseDirectory = "$databaseDirectory/test", filesDirectory = "$filesDirectory/test")
    } else {
        this
    }

internal data class TdlibEnvironmentValues(
    override val databaseDirectory: String,
    override val filesDirectory: String,
    override val deviceModel: String,
    override val systemVersion: String,
    override val systemLanguageCode: String,
    override val applicationVersion: String
) : TdlibEnvironment

// An account is the TDLib database in its directory; its Koin scope has the same name.
internal const val ACCOUNT_PREFIX = "account-"

internal val AccountId.directoryName: String get() = "$ACCOUNT_PREFIX$value"

internal fun TdlibEnvironment.databaseDirectoryOf(id: AccountId) =
    "$databaseDirectory/${id.directoryName}"

internal fun TdlibEnvironment.filesDirectoryOf(id: AccountId) =
    "$filesDirectory/${id.directoryName}"
