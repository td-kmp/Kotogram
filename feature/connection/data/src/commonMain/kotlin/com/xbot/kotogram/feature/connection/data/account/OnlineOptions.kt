package com.xbot.kotogram.feature.connection.data.account

import com.xbot.kotogram.core.account.AccountContext
import com.xbot.kotogram.core.account.AccountManager
import com.xbot.kotogram.core.account.TdlibOptions
import com.xbot.kotogram.feature.connection.data.repository.DefaultConnectionRepository
import io.xbot.tdlib.api.OptionValue
import io.xbot.tdlib.api.OptionValueBoolean
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal class OnlineOptions(
    connection: DefaultConnectionRepository,
    accounts: AccountManager,
    account: AccountContext,
) : TdlibOptions {
    override val options: Flow<Map<String, OptionValue>> =
        combine(connection.inForeground, accounts.shown) { inForeground, shown ->
            val isShown = (shown.adding ?: shown.active)?.id == account.id
            mapOf(ONLINE to OptionValueBoolean(inForeground && isShown))
        }
}

private const val ONLINE = "online"
