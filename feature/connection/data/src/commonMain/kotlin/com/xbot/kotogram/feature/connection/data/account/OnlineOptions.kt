package com.xbot.kotogram.feature.connection.data.account

import co.touchlab.kermit.Logger
import com.xbot.kotogram.core.account.AccountContext
import com.xbot.kotogram.core.account.AccountManager
import com.xbot.kotogram.core.account.TdlibOptions
import com.xbot.kotogram.feature.connection.data.repository.DefaultConnectionRepository
import io.xbot.tdlib.api.OptionValue
import io.xbot.tdlib.api.OptionValueBoolean
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class OnlineOptions(
    connection: DefaultConnectionRepository,
    accounts: AccountManager,
    account: AccountContext,
) : TdlibOptions {
    private val logger = Logger.withTag("OnlineOptions")

    override val options: Flow<Map<String, OptionValue>> =
        combine(connection.inForeground, accounts.shown) { inForeground, shown ->
            inForeground && (shown.adding ?: shown.active)?.id == account.id
        }
            .distinctUntilChanged()
            .onEach { logger.i { "Account ${account.id}: online = $it" } }
            .map { mapOf(ONLINE to OptionValueBoolean(it)) }
}

private const val ONLINE = "online"
