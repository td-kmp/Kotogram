package com.xbot.kotogram.feature.language.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.xbot.kotogram.core.account.AppClient
import io.xbot.tdlib.api.Error
import io.xbot.tdlib.api.GetLanguagePackInfo
import io.xbot.tdlib.api.GetLanguagePackStrings
import io.xbot.tdlib.api.LanguagePackInfo
import io.xbot.tdlib.api.LanguagePackString
import io.xbot.tdlib.api.LanguagePackStrings
import io.xbot.tdlib.api.TdFunction
import io.xbot.tdlib.api.TdObject
import kotlinx.coroutines.CompletableDeferred

internal class FakeAppClient(
    var infos: Map<String, LanguagePackInfo> = emptyMap(),
    var packs: Map<String, List<LanguagePackString>> = emptyMap()
) : AppClient {
    val requests = mutableListOf<TdFunction<*>>()

    // Holds every answer until completed, as TDLib before its instance starts.
    var started = CompletableDeferred(Unit)

    @Suppress("UNCHECKED_CAST")
    override suspend fun <R : TdObject> send(function: TdFunction<R>): Either<Error, R> {
        requests += function
        started.await()
        val answer: TdObject? = when (function) {
            is GetLanguagePackInfo -> infos[function.languagePackId]
            is GetLanguagePackStrings -> packs[function.languagePackId]?.let(::LanguagePackStrings)
            else -> error("Unexpected $function")
        }
        return answer?.let { (it as R).right() } ?: Error(code = 404, message = "Not Found").left()
    }
}
