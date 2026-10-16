package com.xbot.kotogram.feature.auth.data.repository

import com.xbot.kotogram.data.tdlib.TelegramClient
import com.xbot.kotogram.feature.auth.data.mapper.tdCall
import com.xbot.kotogram.feature.auth.domain.model.PasskeyCredential
import com.xbot.kotogram.feature.auth.domain.repository.PasskeyRepository
import io.xbot.tdlib.api.CheckAuthenticationPasskey
import io.xbot.tdlib.api.GetAuthenticationPasskeyParameters

internal class TdPasskeyRepository(private val client: TelegramClient) : PasskeyRepository {

    override suspend fun requestJson(): String = tdCall {
        client.send(GetAuthenticationPasskeyParameters).text
    }

    override suspend fun checkCredential(credential: PasskeyCredential) {
        tdCall {
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
