package com.xbot.kotogram.feature.auth.data.repository

import arrow.core.Either
import arrow.core.raise.either
import com.xbot.kotogram.core.data.client.ClientHolder
import com.xbot.kotogram.core.domain.model.AccountId
import com.xbot.kotogram.feature.auth.data.mapper.request
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.model.PasskeyCredential
import com.xbot.kotogram.feature.auth.domain.repository.PasskeyRepository
import io.xbot.tdlib.api.CheckAuthenticationPasskey
import io.xbot.tdlib.api.GetAuthenticationPasskeyParameters
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

@Scope(AccountId::class)
@Scoped
internal class DefaultPasskeyRepository(private val client: ClientHolder) : PasskeyRepository {

    override suspend fun getAuthenticationPasskeyParameters(): Either<AuthError, String> = either {
        request { client.send(GetAuthenticationPasskeyParameters) }.text
    }

    override suspend fun checkAuthenticationPasskey(
        credential: PasskeyCredential
    ): Either<AuthError, Unit> = either {
        request {
            client.send(
                CheckAuthenticationPasskey(
                    credentialId = credential.credentialId,
                    clientData = credential.clientDataJson,
                    authenticatorData = credential.authenticatorData,
                    signature = credential.signature,
                    userHandle = credential.userHandle
                )
            )
        }
    }
}
