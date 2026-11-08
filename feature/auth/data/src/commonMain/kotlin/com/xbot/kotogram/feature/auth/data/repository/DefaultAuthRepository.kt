package com.xbot.kotogram.feature.auth.data.repository

import arrow.core.Either
import com.xbot.kotogram.core.account.AccountContext
import com.xbot.kotogram.core.account.AccountManager
import com.xbot.kotogram.core.account.extensions.sendCatching
import com.xbot.kotogram.feature.auth.data.account.GeneratedPhotos
import com.xbot.kotogram.feature.auth.data.extensions.request
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import com.xbot.kotogram.feature.auth.domain.repository.AuthRepository
import io.xbot.tdlib.api.AuthorizationState
import io.xbot.tdlib.api.AuthorizationStateReady
import io.xbot.tdlib.api.CheckAuthenticationCode
import io.xbot.tdlib.api.CheckAuthenticationEmailCode
import io.xbot.tdlib.api.CheckAuthenticationPassword
import io.xbot.tdlib.api.EmailAddressAuthenticationCode
import io.xbot.tdlib.api.InputChatPhotoStatic
import io.xbot.tdlib.api.RecoverAuthenticationPassword
import io.xbot.tdlib.api.RegisterUser
import io.xbot.tdlib.api.RequestAuthenticationPasswordRecovery
import io.xbot.tdlib.api.RequestQrCodeAuthentication
import io.xbot.tdlib.api.ResendAuthenticationCode
import io.xbot.tdlib.api.ResendCodeReasonUserRequest
import io.xbot.tdlib.api.SetAuthenticationEmailAddress
import io.xbot.tdlib.api.SetAuthenticationPhoneNumber
import io.xbot.tdlib.api.SetProfilePhoto
import io.xbot.tdlib.api.TdFunction
import io.xbot.tdlib.api.TdObject
import io.xbot.tdlib.client.TdClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// One method per TDLib authorization function.
@Suppress("TooManyFunctions")
internal class DefaultAuthRepository(
    private val context: AccountContext,
    private val client: TdClient,
    private val accounts: AccountManager,
    private val photos: GeneratedPhotos
) : AuthRepository {

    override val authorizationState: Flow<AuthorizationState> =
        client.authorizationState.filterNotNull()

    override val isFirstSignIn: Flow<Boolean> = accounts.registry
        .map { registry -> registry.signedIn.none { it.id != context.id } }
        .distinctUntilChanged()

    override suspend fun setAuthenticationPhoneNumber(phoneNumber: String) =
        call(SetAuthenticationPhoneNumber(phoneNumber = phoneNumber))

    override suspend fun requestQrCodeAuthentication(): Either<AuthError, Unit> {
        val otherUserIds = accounts.registry.value.signedIn
            .filter { it.id != context.id }
            .mapNotNull { it.userId }
        return call(RequestQrCodeAuthentication(otherUserIds = otherUserIds))
    }

    override fun restartSignIn() = accounts.restartSignIn(context.id)

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

    override suspend fun registerUser(firstName: String, lastName: String, photo: ByteArray?) =
        call(RegisterUser(firstName = firstName, lastName = lastName)).onRight {
            if (photo != null) setPhotoWhenReady(photo)
        }

    // Telegram for iOS sets the photo the same way, once signed up. On the account's scope: the sign-in's screens
    // go away with Ready.
    private fun setPhotoWhenReady(photo: ByteArray) {
        context.scope.launch {
            client.authorizationState.first { it is AuthorizationStateReady }
            client.sendCatching(SetProfilePhoto(photo = InputChatPhotoStatic(photos.add(photo)), isPublic = false))
        }
    }

    private suspend fun call(function: TdFunction<TdObject>): Either<AuthError, Unit> =
        client.request(function).map {}
}
