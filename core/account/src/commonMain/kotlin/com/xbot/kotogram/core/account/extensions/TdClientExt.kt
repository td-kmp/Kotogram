package com.xbot.kotogram.core.account.extensions

import arrow.core.Either
import io.xbot.tdlib.TdLibUnavailableException
import io.xbot.tdlib.api.Error
import io.xbot.tdlib.api.TdFunction
import io.xbot.tdlib.api.TdObject
import io.xbot.tdlib.client.TdClient
import io.xbot.tdlib.client.TdException
import kotlinx.serialization.SerializationException

private const val UNDECODABLE_ANSWER = -1
private const val UNAVAILABLE = -2

suspend fun <R : TdObject> TdClient.sendCatching(function: TdFunction<R>): Either<Error, R> = try {
    Either.Right(send(function))
} catch (e: TdException) {
    Either.Left(e.error)
} catch (e: SerializationException) {
    Either.Left(Error(code = UNDECODABLE_ANSWER, message = e.message.orEmpty()))
} catch (e: TdLibUnavailableException) {
    Either.Left(Error(code = UNAVAILABLE, message = e.message.orEmpty()))
}
