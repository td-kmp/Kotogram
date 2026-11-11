package com.xbot.kotogram.feature.language.data.account

import com.xbot.kotogram.core.account.AccountUpdates
import com.xbot.kotogram.feature.language.data.repository.DefaultLanguageRepository
import io.xbot.tdlib.api.UpdateLanguagePackStrings
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

internal class LanguagePackUpdates(
    private val languages: DefaultLanguageRepository,
) : AccountUpdates {
    override fun start(
        scope: CoroutineScope,
        client: TdClient,
    ) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            client.updatesOf<UpdateLanguagePackStrings>().collect { languages.onStringsChanged(it) }
        }
    }
}
