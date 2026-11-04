package com.xbot.kotogram.feature.auth.data.extensions

import arrow.core.Either
import com.xbot.kotogram.core.tdlib.client.TdSender
import com.xbot.kotogram.core.tdlib.extensions.sendCatching
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import io.xbot.tdlib.api.Error
import io.xbot.tdlib.api.TdFunction
import io.xbot.tdlib.api.TdObject

/** Runs a TDLib request, reporting TDLib's error as an [AuthError]. */
internal suspend fun <R : TdObject> TdSender.request(
    function: TdFunction<R>
): Either<AuthError, R> = sendCatching(function).mapLeft(Error::toAuthError)
