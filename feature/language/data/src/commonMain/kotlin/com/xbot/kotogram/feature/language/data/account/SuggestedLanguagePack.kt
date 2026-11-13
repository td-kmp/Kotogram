package com.xbot.kotogram.feature.language.data.account

import com.xbot.kotogram.core.account.AccountUpdates
import io.xbot.tdlib.api.OptionValueString
import io.xbot.tdlib.api.UpdateOption
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

internal class SuggestedLanguagePack : AccountUpdates {
    val languagePackId: StateFlow<String>
        field = MutableStateFlow("")

    override fun start(
        scope: CoroutineScope,
        client: TdClient,
    ) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            client.updatesOf<UpdateOption>()
                .filter { it.name == SUGGESTED_LANGUAGE_PACK_ID }
                .collect { languagePackId.value = (it.value as? OptionValueString)?.value.orEmpty() }
        }
    }

    private companion object {
        const val SUGGESTED_LANGUAGE_PACK_ID = "suggested_language_pack_id"
    }
}
