package com.xbot.kotogram.feature.auth.domain.repository

import arrow.core.Either
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import io.xbot.tdlib.api.AuthorizationState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authorizationState: Flow<AuthorizationState>

    // No account is signed in on the device yet.
    val isFirstSignIn: Flow<Boolean>

    suspend fun setAuthenticationPhoneNumber(phoneNumber: String): Either<AuthError, Unit>

    suspend fun requestQrCodeAuthentication(): Either<AuthError, Unit>

    // Starts over in a new account: TDLib takes no phone number during QR confirmation, nor a QR code past a step.
    fun restartSignIn()

    suspend fun checkAuthenticationCode(code: String): Either<AuthError, Unit>

    suspend fun resendAuthenticationCode(): Either<AuthError, Unit>

    suspend fun setAuthenticationEmailAddress(emailAddress: String): Either<AuthError, Unit>

    suspend fun checkAuthenticationEmailCode(code: String): Either<AuthError, Unit>

    suspend fun checkAuthenticationPassword(password: String): Either<AuthError, Unit>

    suspend fun requestAuthenticationPasswordRecovery(): Either<AuthError, Unit>

    suspend fun recoverAuthenticationPassword(recoveryCode: String): Either<AuthError, Unit>

    suspend fun registerUser(firstName: String, lastName: String): Either<AuthError, Unit>
}
