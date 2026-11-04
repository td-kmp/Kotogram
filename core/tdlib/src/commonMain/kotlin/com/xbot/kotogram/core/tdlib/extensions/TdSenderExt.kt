package com.xbot.kotogram.core.tdlib.extensions

import arrow.core.Either
import com.xbot.kotogram.core.tdlib.client.TdSender
import io.xbot.tdlib.api.Error
import io.xbot.tdlib.api.TdFunction
import io.xbot.tdlib.api.TdObject
import io.xbot.tdlib.client.TdException
import kotlinx.serialization.SerializationException

private const val UNDECODABLE_ANSWER = -1

/** [TdSender.send] as a value. The library surfaces an answer it can't decode as an exception. */
suspend fun <R : TdObject> TdSender.sendCatching(function: TdFunction<R>): Either<Error, R> = try {
    Either.Right(send(function))
} catch (e: TdException) {
    Either.Left(e.error)
} catch (e: SerializationException) {
    Either.Left(Error(code = UNDECODABLE_ANSWER, message = e.message.orEmpty()))
}
