package com.xbot.kotogram.feature.language.data.account

import com.xbot.kotogram.core.account.AppStorage
import com.xbot.kotogram.core.account.TdlibOptions
import com.xbot.kotogram.feature.language.data.LOCALIZATION_TARGET
import com.xbot.kotogram.feature.language.data.repository.DefaultLanguageRepository
import io.xbot.tdlib.api.OptionValue
import io.xbot.tdlib.api.OptionValueEmpty
import io.xbot.tdlib.api.OptionValueString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class LanguageOptions(
    storage: AppStorage,
    languages: DefaultLanguageRepository,
) : TdlibOptions {
    private val databasePath = "${storage.tdlibDirectory}/langpack"

    override val options: Flow<Map<String, OptionValue>> = languages.languagePackId.map { languagePackId ->
        mapOf(
            "localization_target" to OptionValueString(LOCALIZATION_TARGET),
            "language_pack_database_path" to OptionValueString(databasePath),
            // Workaround: TDLib keeps the last pack in its binlog, so English has to clear it.
            "language_pack_id" to if (languagePackId.isEmpty()) OptionValueEmpty else OptionValueString(languagePackId),
        )
    }
}
