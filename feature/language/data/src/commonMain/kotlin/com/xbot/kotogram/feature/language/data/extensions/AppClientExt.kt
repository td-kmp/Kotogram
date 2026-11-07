package com.xbot.kotogram.feature.language.data.extensions

import arrow.core.Either
import arrow.core.raise.either
import com.xbot.kotogram.core.account.AppClient
import com.xbot.kotogram.feature.language.domain.model.LanguagePack
import io.xbot.tdlib.api.Error
import io.xbot.tdlib.api.GetLanguagePackStrings
import io.xbot.tdlib.api.LanguagePackInfo
import io.xbot.tdlib.api.LanguagePackStringValue

// TDLib answers from its database once a pack is whole there, and else downloads the pack and keeps it.
internal suspend fun AppClient.languagePack(info: LanguagePackInfo): Either<Error, LanguagePack> = either {
    val strings = strings(info.id).bind()
    val baseStrings = if (info.baseLanguagePackId.isEmpty()) emptyMap() else strings(info.baseLanguagePackId).bind()
    LanguagePack(info, strings, baseStrings)
}

private suspend fun AppClient.strings(languagePackId: String): Either<Error, Map<String, LanguagePackStringValue>> =
    send(GetLanguagePackStrings(languagePackId = languagePackId, keys = emptyList())).map { it.toMap() }
