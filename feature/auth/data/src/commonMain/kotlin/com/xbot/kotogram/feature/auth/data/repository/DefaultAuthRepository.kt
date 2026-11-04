package com.xbot.kotogram.feature.auth.data.repository

import arrow.core.Either
import arrow.core.raise.either
import com.xbot.kotogram.core.model.AccountId
import com.xbot.kotogram.core.tdlib.account.AccountRepository
import com.xbot.kotogram.core.tdlib.client.ClientHolder
import com.xbot.kotogram.feature.auth.data.extensions.request
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateWaitOtherDeviceConfirmation
import io.xbot.tdlib.api.AuthorizationStateWaitPhoneNumber
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
import io.xbot.tdlib.api.TdFunction
import io.xbot.tdlib.api.TdObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

// One method per TDLib authorization function.
@Suppress("TooManyFunctions")
@Scope(AccountId::class)
@Scoped
internal class DefaultAuthRepository(
    @Provided private val accountId: AccountId,
    private val client: ClientHolder,
    private val accounts: AccountRepository
) : AuthRepository {

    override val authorizationState: Flow<AuthorizationState> =
        client.authorizationState.filterNotNull()

    override suspend fun setAuthenticationPhoneNumber(
        phoneNumber: String
    ): Either<AuthError, Unit> = either {
        if (client.authorizationState.value is AuthorizationStateWaitOtherDeviceConfirmation) {
            // TDLib leaves QR login only through a log out; the holder starts a fresh instance after it.
            client.request(LogOut).bind()
            client.authorizationState.first { it is AuthorizationStateWaitPhoneNumber }
        }
        client.request(SetAuthenticationPhoneNumber(phoneNumber = phoneNumber)).bind()
    }

    override suspend fun requestQrCodeAuthentication(): Either<AuthError, Unit> {
        val otherUserIds = accounts.accounts.value.filter {
            it.id != accountId
        }.mapNotNull { it.userId }
        return call(RequestQrCodeAuthentication(otherUserIds = otherUserIds))
    }

    override suspend fun checkAuthenticationCode(code: String) =
        call(CheckAuthenticationCode(code = code))

    override suspend fun resendAuthenticationCode() =
        call(ResendAuthenticationCode(reason = ResendCodeReasonUserRequest))

    override suspend fun setAuthenticationEmailAddress(emailAddress: String) =
        call(SetAuthenticationEmailAddress(emailAddress = emailAddress))

    override suspend fun checkAuthenticationEmailCode(code: String) =
        call(CheckAuthenticationEmailCode(code = EmailAddressAuthenticationCode(code = code)))

    override suspend fun checkAuthenticationPassword(password: String) =
        call(CheckAuthenticationPassword(password = password))

    override suspend fun requestAuthenticationPasswordRecovery() =
        call(RequestAuthenticationPasswordRecovery)

    override suspend fun recoverAuthenticationPassword(recoveryCode: String) =
        call(RecoverAuthenticationPassword(recoveryCode = recoveryCode))

    override suspend fun registerUser(firstName: String, lastName: String) =
        call(RegisterUser(firstName = firstName, lastName = lastName))

    override suspend fun logOut() = call(LogOut)

    private suspend fun call(function: TdFunction<TdObject>): Either<AuthError, Unit> =
        client.request(function).map {}
}
