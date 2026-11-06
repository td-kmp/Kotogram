package com.xbot.kotogram.core.account

import com.xbot.kotogram.core.account.extensions.sendCatching
import io.xbot.tdlib.api.AuthorizationStateWaitTdlibParameters
import io.xbot.tdlib.api.OptionValueString
import io.xbot.tdlib.api.SetOption
import io.xbot.tdlib.api.UpdateLanguagePackStrings
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Points every account's TDLib at the shared language pack, as Telegram X and Unigram do.
internal class LanguagePackOptions(private val localization: Localization) : AccountUpdates {
    override fun start(scope: CoroutineScope, client: TdClient) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            client.updatesOf<UpdateLanguagePackStrings>().collect(localization::onStringsChanged)
        }
        scope.launch {
            // After the parameters: the first request starts the instance, and that stays AccountManager's.
            client.authorizationState.first {
                it != null &&
                    it !is AuthorizationStateWaitTdlibParameters
            }
            listOf(
                "language_pack_database_path" to localization.databasePath,
                "localization_target" to Localization.LOCALIZATION_TARGET,
                "language_pack_id" to localization.languagePackId
            ).forEach { (name, value) ->
                client.sendCatching(SetOption(name = name, value = OptionValueString(value)))
            }
        }
    }
}
