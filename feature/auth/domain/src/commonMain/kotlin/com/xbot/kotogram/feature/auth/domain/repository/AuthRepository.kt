package com.xbot.kotogram.feature.auth.domain.repository

import arrow.core.Either
import com.xbot.kotogram.core.domain.model.AuthorizationState
import com.xbot.kotogram.feature.auth.domain.model.AuthError
import kotlinx.coroutines.flow.Flow

/** TDLib's authorization functions for the account in scope, named like them. */
interface AuthRepository {
    val authorizationState: Flow<AuthorizationState>

    /** From QR confirmation too: TDLib refuses a number there, so it leaves QR login first. */
    suspend fun setAuthenticationPhoneNumber(phoneNumber: String): Either<AuthError, Unit>

    suspend fun requestQrCodeAuthentication(): Either<AuthError, Unit>

    suspend fun checkAuthenticationCode(code: String): Either<AuthError, Unit>

    suspend fun resendAuthenticationCode(): Either<AuthError, Unit>

    suspend fun setAuthenticationEmailAddress(emailAddress: String): Either<AuthError, Unit>

    suspend fun checkAuthenticationEmailCode(code: String): Either<AuthError, Unit>

    suspend fun checkAuthenticationPassword(password: String): Either<AuthError, Unit>

    suspend fun requestAuthenticationPasswordRecovery(): Either<AuthError, Unit>

    suspend fun recoverAuthenticationPassword(recoveryCode: String): Either<AuthError, Unit>

    suspend fun registerUser(firstName: String, lastName: String): Either<AuthError, Unit>

    suspend fun logOut(): Either<AuthError, Unit>
}
