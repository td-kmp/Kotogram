package com.xbot.kotogram.feature.auth.data.extensions

import arrow.core.Either
import com.xbot.kotogram.core.account.extensions.sendCatching
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import io.xbot.tdlib.api.Error
import io.xbot.tdlib.api.TdFunction
import io.xbot.tdlib.api.TdObject
import io.xbot.tdlib.client.TdClient

internal suspend fun <R : TdObject> TdClient.request(function: TdFunction<R>): Either<AuthError, R> =
    sendCatching(function).mapLeft(Error::toAuthError)
