package com.xbot.kotogram.feature.auth.data.repository

import com.xbot.kotogram.data.tdlib.TelegramClient
import com.xbot.kotogram.feature.auth.data.mapper.tdCall
import com.xbot.kotogram.feature.auth.data.mapper.toDomain
import com.xbot.kotogram.feature.auth.domain.model.AuthState
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import io.xbot.tdlib.api.CheckAuthenticationCode
import io.xbot.tdlib.api.CheckAuthenticationEmailCode
import io.xbot.tdlib.api.CheckAuthenticationPassword
import io.xbot.tdlib.api.EmailAddressAuthenticationCode
import io.xbot.tdlib.api.LogOut
import io.xbot.tdlib.api.RecoverAuthenticationPassword
import io.xbot.tdlib.api.RegisterUser
import io.xbot.tdlib.api.RequestAuthenticationPasswordRecovery
import io.xbot.tdlib.api.RequestQrCodeAuthentication
import io.xbot.tdlib.api.ResendAuthenticationCode
import io.xbot.tdlib.api.ResendCodeReasonUserRequest
import io.xbot.tdlib.api.SetAuthenticationEmailAddress
import io.xbot.tdlib.api.SetAuthenticationPhoneNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class TdAuthRepository(private val client: TelegramClient) : AuthRepository {

    override val authState: Flow<AuthState> = client.authorizationState.map { it.toDomain() }

    override suspend fun setPhoneNumber(phoneNumber: String) {
        tdCall { client.send(SetAuthenticationPhoneNumber(phoneNumber = phoneNumber)) }
    }

    override suspend fun requestQrCode() {
        tdCall { client.send(RequestQrCodeAuthentication()) }
    }

    override suspend fun checkCode(code: String) {
        tdCall { client.send(CheckAuthenticationCode(code = code)) }
    }

    override suspend fun resendCode() {
        tdCall { client.send(ResendAuthenticationCode(reason = ResendCodeReasonUserRequest)) }
    }

    override suspend fun setEmailAddress(email: String) {
        tdCall { client.send(SetAuthenticationEmailAddress(emailAddress = email)) }
    }

    override suspend fun checkEmailCode(code: String) {
        tdCall {
            client.send(
                CheckAuthenticationEmailCode(code = EmailAddressAuthenticationCode(code = code))
            )
        }
    }

    override suspend fun checkPassword(password: String) {
        tdCall { client.send(CheckAuthenticationPassword(password = password)) }
    }

    override suspend fun requestPasswordRecovery() {
        tdCall { client.send(RequestAuthenticationPasswordRecovery) }
    }

    override suspend fun recoverPassword(recoveryCode: String) {
        tdCall { client.send(RecoverAuthenticationPassword(recoveryCode = recoveryCode)) }
    }

    override suspend fun register(firstName: String, lastName: String) {
        tdCall { client.send(RegisterUser(firstName = firstName, lastName = lastName)) }
    }

    override suspend fun logOut() {
        tdCall { client.send(LogOut) }
    }
}
